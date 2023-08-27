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

        val bytecodePrinterClassVisitor = UnmanagedClassVisitor(
                clazz = clazz,
                classVisitor = classWriter,
                beanManagingTargetFilter = beanManagingTargetFilter,
                detectedClassSet = detectedClassSet
        )
        classReader.accept(bytecodePrinterClassVisitor, 0)

        return detectedClassSet
    }

    private inner class UnmanagedClassVisitor(
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



    private inner class UnmanagedClassMethodVisitor(
            private val clazz: Class<*>,
            private val methodName: String,
            methodVisitor: MethodVisitor,
            private val beanManagingTargetFilter: BeanManagingTargetFilter,
            private val detectedClassSet: MutableSet<DetectedUnmanagedClass>,
    ) : MethodVisitor(ASM_API, methodVisitor) {

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {

            when(opcode) {
                Opcodes.INVOKESTATIC-> doWhenFactoryMethodExecuted(owner, name, desc)
                Opcodes.INVOKESPECIAL-> doWhenConstructorExecuted(owner, name)
                else -> null
            }?.let(detectedClassSet::add)

            super.visitMethodInsn(opcode, owner, name, desc, itf)
        }

        private fun doWhenConstructorExecuted(
                generatedClassResourcePath: String,
                methodName: String,
        ): DetectedUnmanagedClass? {
            if(methodName != "<init>")
                return null

            val generatedClass = getClassFromPath(generatedClassResourcePath)
            if(!beanManagingTargetFilter.isManageTarget(generatedClass))
                return null

            val location = when(methodName) {
                "<init>" -> DetectedUnmanagedClass.Location.Constructor
                else     -> DetectedUnmanagedClass.Location.InMethod
            }

            return detectedUnmanagedClassOf(generatedClass = generatedClass, location = location, )
        }

        private fun doWhenFactoryMethodExecuted(
                methodOwner: String,
                methodName: String,
                methodDescriptor: String,
        ): DetectedUnmanagedClass? {
            val factoryClass = getClassFromPath(methodOwner)
            val methodType = Type.getMethodType(methodDescriptor)
            val methodArgTypes = methodType.argumentTypes.map { TypeClassFactory.of(it.className) }
            val method = factoryClass.getDeclaredMethod(methodName, *methodArgTypes.toTypedArray())

            if(method.returnType.name != methodType.returnType.className)
                return null
            if(method.returnType.name == "void")
                return null
            if(!beanManagingTargetFilter.isManageTarget(method.returnType))
                return null

            val location = when(methodName) {
                "<init>" -> DetectedUnmanagedClass.Location.Constructor
                else     -> DetectedUnmanagedClass.Location.FactoryMethod
            }

            return detectedUnmanagedClassOf(generatedClass = method.returnType, location = location, )
        }
        private fun detectedUnmanagedClassOf(
                generatedClass: Class<*>,
                location: DetectedUnmanagedClass.Location
        ): DetectedUnmanagedClass {
            return DetectedUnmanagedClass(
                    from = clazz,
                    location = location,
                    methodName = methodName,
                    generated = generatedClass,
                    isInterface = generatedClass.isInterface
            )
        }

        private fun getClassFromPath(path: String): Class<*> {
            val className = ClassUtils.convertResourcePathToClassName(path)
            return Class.forName(className)
        }


    }

}