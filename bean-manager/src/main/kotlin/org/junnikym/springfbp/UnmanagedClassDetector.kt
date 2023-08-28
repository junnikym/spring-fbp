package org.junnikym.springfbp

import org.junnikym.springfbp.common.DetectedUnmanagedClass
import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.junnikym.springfbp.filter.IgnoreManage
import org.objectweb.asm.*
import org.springframework.cglib.core.Constants.ASM_API
import org.springframework.stereotype.Component
import org.springframework.util.ClassUtils

@Component
@IgnoreManage
class UnmanagedClassDetector(
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) {

    fun detect(clazz: Class<*>): Collection<DetectedUnmanagedClass> {
        val classResourcePath = ClassUtils.convertClassNameToResourcePath(clazz.name)
        val inputStream = ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream("$classResourcePath.class")

        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(classReader, 0)
        val detectedClassSet = mutableSetOf<DetectedUnmanagedClass>()

        val unmanagedClassVisitor = UnmanagedClassVisitor(
                clazz = clazz,
                classVisitor = classWriter,
                beanManagingTargetFilter = beanManagingTargetFilter,
                detectedClassSet = detectedClassSet
        )
        classReader.accept(unmanagedClassVisitor, 0)

        return detectedClassSet
    }

}

private class UnmanagedClassVisitor(
        private val clazz: Class<*>,
        classVisitor: ClassVisitor,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
        private val detectedClassSet: MutableSet<DetectedUnmanagedClass>,
) : ClassVisitor(ASM_API, classVisitor) {

    override fun visitMethod(access: Int, name: String, desc: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        val methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions)

        return UnmanagedClassMethodVisitor(
                clazz = clazz,
                methodName = name,
                methodVisitor = methodVisitor,
                beanManagingTargetFilter = beanManagingTargetFilter,
                detectedClassSet = detectedClassSet
        )
    }

}


private class UnmanagedClassMethodVisitor(
        private val clazz: Class<*>,
        private val methodName: String,
        methodVisitor: MethodVisitor,
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
        private val detectedClassSet: MutableSet<DetectedUnmanagedClass>,
) : MethodVisitor(ASM_API, methodVisitor) {

    private val staticGetterExecutions = mutableSetOf<Class<*>>()

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        when(opcode) {
            Opcodes.GETSTATIC-> doWhenStaticFieldGetExecuted(name, descriptor)
            Opcodes.PUTFIELD-> doWhenAssignmentExecuted(owner, descriptor)
        }

        super.visitFieldInsn(opcode, owner, name, descriptor)
    }

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        when(opcode) {
            Opcodes.INVOKEVIRTUAL-> doWhenMethodExecuted(owner, name, desc)
            Opcodes.INVOKESTATIC-> doWhenMethodExecuted(owner, name, desc)
            Opcodes.INVOKESPECIAL-> doWhenConstructorExecuted(owner, name, desc)
        }

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    private fun doWhenConstructorExecuted(
            methodOwner: String,
            methodName: String,
            methodDescriptor: String,
    ) {
        if(methodName != "<init>")
            return

        val generatedClass = getClassFromPath(methodOwner)
        if(!beanManagingTargetFilter.isManageTarget(generatedClass))
            return

        detectedUnmanagedClassOf(
                generatedClass,
                methodDescriptor,
                DetectedUnmanagedClass.GeneratorType.Constructor,
                generatedClass,
        ).let(detectedClassSet::add)
    }

    private fun doWhenMethodExecuted(
            methodOwner: String,
            methodName: String,
            methodDescriptor: String,
    ) {
        val factoryClass = getClassFromPath(methodOwner)
        val methodType = Type.getMethodType(methodDescriptor)
        val methodArgTypes = methodType.argumentTypes.map { TypeClassFactory.of(it.className) }
        val method = factoryClass.getDeclaredMethod(methodName, *methodArgTypes.toTypedArray())

        if(method.returnType.name != methodType.returnType.className)
            return
        if(method.returnType.name == "void")
            return
        if(!beanManagingTargetFilter.isManageTarget(method.returnType))
            return

        detectedUnmanagedClassOf(
                method.returnType,
                methodDescriptor,
                DetectedUnmanagedClass.GeneratorType.Method,
                factoryClass,
        ).let(detectedClassSet::add)
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

        staticGetterExecutions.add(fieldClass)
    }

    private fun doWhenAssignmentExecuted(fieldOwner: String?, fieldDescriptor: String?) {
        if(fieldDescriptor.isNullOrBlank() || fieldOwner.isNullOrBlank())
            return

        val fieldTypeName = Type.getType(fieldDescriptor)
        val fieldClass = Class.forName(fieldTypeName.className)

        val assignFromStatic = staticGetterExecutions
                .find(fieldClass::isAssignableFrom)
                ?.let { staticGetterExecutions.remove(it); it }
                ?.javaClass

        val assignedClass = assignFromStatic ?: fieldClass
        if(!beanManagingTargetFilter.isManageTarget(assignedClass))
            return

        val ownerClass = getClassFromPath(fieldOwner)

        detectedUnmanagedClassOf(
                fieldClass,
                fieldDescriptor,
                DetectedUnmanagedClass.GeneratorType.Field,
                ownerClass
        ).let(detectedClassSet::add)
    }

    private fun detectedUnmanagedClassOf(
            generatedClass: Class<*>,
            generatorDesc: String,
            generatorType: DetectedUnmanagedClass.GeneratorType,
            generatorOwner: Class<*>,
    ): DetectedUnmanagedClass {

        return DetectedUnmanagedClass(
                fromClass = this.clazz,
                methodName = this.methodName,
                generatedClass = generatedClass,
                generatorDesc = generatorDesc,
                generatorType = generatorType,
                generatorOwner = generatorOwner,
                isInterface = generatedClass.isInterface,
        )
    }

    private fun getClassFromPath(path: String): Class<*> {
        val className = ClassUtils.convertResourcePathToClassName(path)
        return Class.forName(className)
    }

}