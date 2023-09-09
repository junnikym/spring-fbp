package org.junnikym.springfbp

import org.junnikym.springfbp.common.BeanDependencyNode
import org.junnikym.springfbp.common.DetectedUnmanagedClass
import org.junnikym.springfbp.factory.BeanDependencyLinkFactory
import org.junnikym.springfbp.factory.BeanDependencyNodeFactory
import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.junnikym.springfbp.filter.IgnoreManage
import org.objectweb.asm.*
import org.springframework.cglib.core.Constants.ASM_API
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

@Component
@IgnoreManage
class UnmanagedClassDetector(
        private val beanDependencyLinkFactory: BeanDependencyLinkFactory,
        private val beanDependencyNodeFactory: BeanDependencyNodeFactory,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) {

    fun detectInFactory(): Collection<DetectedUnmanagedClass> {
        return beanDependencyNodeFactory.getAll()
                .asSequence()
                .mapNotNull(BeanDependencyNode::clazz)
                .map(::detect)
                .flatten().toList()
                .filter { beanDependencyLinkFactory.isLinked(it.fromClass, it.generatedClass).not() }
    }

    fun detect(clazz: Class<*>): Collection<DetectedUnmanagedClass> {
        val classResourcePath = ClassUtils.convertClassNameToResourcePath(clazz.name)
        val inputStream = ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream("$classResourcePath.class")

        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(classReader, 0)
        val detectedClasses = mutableListOf<DetectedUnmanagedClass>()
        val unmanagedClassVisitor = UnmanagedClassVisitor(
                clazz = clazz,
                classVisitor = classWriter,
                beanManagingTargetFilter = beanManagingTargetFilter,
                detectedClasses = detectedClasses
        )
        classReader.accept(unmanagedClassVisitor, 0)

        filteringDuplicateInDetectedClasses(detectedClasses)

        return detectedClasses
    }

    private fun filteringDuplicateInDetectedClasses(detectedClasses: MutableCollection<DetectedUnmanagedClass>) {
        val classesByMethodName = detectedClasses.groupBy { it.methodName }
        classesByMethodName.keys.forEach { key->
            val classesInMethod = classesByMethodName[key]
            classesInMethod
                    ?.filter { it.generatorName == null }
                    ?.filter { it.generatedClass.isInterface }
                    ?.filter { interfaceClass->
                        classesInMethod.any { it.generatedClass.isAssignableFrom(interfaceClass.generatedClass) }
                    }
                    ?.forEach (detectedClasses::remove)
        }
    }

}

private class UnmanagedClassVisitor(
        private val clazz: Class<*>,
        classVisitor: ClassVisitor,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
        private val detectedClasses: MutableCollection<DetectedUnmanagedClass>,
) : ClassVisitor(ASM_API, classVisitor) {

    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        val methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)

        return UnmanagedClassMethodVisitor(
                clazz = clazz,
                methodName = name,
                methodVisitor = methodVisitor,
                beanManagingTargetFilter = beanManagingTargetFilter,
                detectedClasses = detectedClasses
        )
    }

}


private class UnmanagedClassMethodVisitor(
        private val clazz: Class<*>,
        private val methodName: String,
        methodVisitor: MethodVisitor,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
        private val detectedClasses: MutableCollection<DetectedUnmanagedClass>,
) : MethodVisitor(ASM_API, methodVisitor) {

    // key: return type, value: name of static field
    private val staticGetterExecutions = mutableMapOf<Class<*>, String>()

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        when(opcode) {
            Opcodes.GETSTATIC-> doWhenStaticFieldGetExecuted(name, descriptor)
            Opcodes.PUTFIELD-> doWhenAssignmentExecuted(owner, name, descriptor)
        }

        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        when(opcode) {
            Opcodes.INVOKEVIRTUAL-> doWhenMethodExecuted(owner, name, desc)
            Opcodes.INVOKESTATIC-> doWhenMethodExecuted(owner, name, desc)
            Opcodes.INVOKESPECIAL-> {
                if(name == "<init>") doWhenConstructorExecuted(owner, name, desc)
                else doWhenMethodExecuted(owner, name, desc)
            }
        }

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    private fun doWhenConstructorExecuted(
            methodOwner: String,
            methodName: String,
            methodDescriptor: String,
    ) {
        val generatedClass = getClassFromPath(methodOwner)
        if(!beanManagingTargetFilter.isManageTarget(generatedClass))
            return

        detectedUnmanagedClassOf(
                generatedClass,
                methodName,
                methodDescriptor,
                DetectedUnmanagedClass.GeneratorType.Constructor,
                generatedClass,
        ).let(detectedClasses::add)
    }

    private fun doWhenMethodExecuted(
            methodOwner: String,
            methodName: String,
            methodDescriptor: String,
    ) {
        val factoryClass = getClassFromPath(methodOwner)
        val methodType = Type.getMethodType(methodDescriptor)

        val methodArgTypes = try {
            methodType.argumentTypes.map { TypeClassFactory.of(it.className) }
        } catch (e: ClassNotFoundException) { return }

        val method = try {
            factoryClass.getDeclaredMethod(methodName, *methodArgTypes.toTypedArray())
        } catch (e: NoSuchMethodException) { return }

        if(method.returnType.name != methodType.returnType.className)
            return
        if(method.returnType.name == "void")
            return
        if(method.returnType.isArray || method.returnType.isPrimitive)
            return
        if(!beanManagingTargetFilter.isManageTarget(method.returnType))
            return

        detectedUnmanagedClassOf(
                method.returnType,
                methodName,
                methodDescriptor,
                DetectedUnmanagedClass.GeneratorType.Method,
                factoryClass,
        ).let(detectedClasses::add)
    }

    private fun doWhenStaticFieldGetExecuted(
            fieldName: String?,
            fieldDescriptor: String?,
    ) {
        if(fieldDescriptor.isNullOrBlank() || fieldName.isNullOrBlank())
            return

        if(fieldName == "Companion")
            return

        val fieldTypeName = Type.getType(fieldDescriptor).className
        val fieldClass = Class.forName(fieldTypeName)
        if(!beanManagingTargetFilter.isManageTarget(fieldClass))
            return

        staticGetterExecutions[fieldClass] = fieldName
    }

    private fun doWhenAssignmentExecuted(fieldOwner: String?, fieldName: String?, fieldDescriptor: String?) {
        if(fieldDescriptor.isNullOrBlank() || fieldOwner.isNullOrBlank() || fieldName.isNullOrBlank())
            return

        val fieldTypeName = Type.getType(fieldDescriptor)
        val fieldClass = Class.forName(fieldTypeName.className)

        val assignFieldClass = staticGetterExecutions.keys.find(fieldClass::isAssignableFrom)
        val assignFieldName = staticGetterExecutions[assignFieldClass]
        staticGetterExecutions.remove(assignFieldClass)

        val assignedClass = assignFieldClass ?: fieldClass
        if(!beanManagingTargetFilter.isManageTarget(assignedClass))
            return

        val generator = when(assignFieldName) {
            null -> DetectedUnmanagedClass.GeneratorType.Field
            else -> DetectedUnmanagedClass.GeneratorType.Factory
        }

        detectedUnmanagedClassOf(
                fieldClass,
                assignFieldName,
                fieldDescriptor,
                generator,
                getClassFromPath(fieldOwner)
        ).let(detectedClasses::add)
    }

    private fun detectedUnmanagedClassOf(
            generatedClass: Class<*>,
            generatorName: String?,
            generatorDesc: String,
            generatorType: DetectedUnmanagedClass.GeneratorType,
            generatorOwner: Class<*>,
    ): DetectedUnmanagedClass {

        return DetectedUnmanagedClass(
                fromClass = this.clazz,
                methodName = this.methodName,
                generatedClass = generatedClass,
                generatorName = generatorName,
                generatorDesc = generatorDesc,
                generatorType = generatorType,
                generatorOwner = generatorOwner,
        )
    }

    private fun getClassFromPath(path: String): Class<*> {
        val className = ClassUtils.convertResourcePathToClassName(path)
        return Class.forName(className)
    }

}