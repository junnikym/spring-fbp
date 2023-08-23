package org.junnikym.springfbp

import org.junnikym.springfbp.common.DetectedUnmanagedClass
import org.junnikym.springfbp.filter.BeanManagingTargetFilter
import org.junnikym.springfbp.filter.IgnoreManage
import org.objectweb.asm.*
import org.springframework.cglib.core.Constants.ASM_API
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
@IgnoreManage
class UnmanagedClassDetector(
        private val beanManagingTargetFilter: BeanManagingTargetFilter,
) {

    fun detect(clazz: Class<*>) {
        val inputStream = ClassLoader
                .getSystemClassLoader()
                .getResourceAsStream(clazz.name.replace('.', '/') + ".class")

        val classReader = ClassReader(inputStream)
        val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
        val detectedClassSet = mutableSetOf<DetectedUnmanagedClass>()

        val bytecodePrinterClassVisitor = UnmanagedClassVisitor(
                clazz = clazz,
                classVisitor = classWriter,
                beanManagingTargetFilter = beanManagingTargetFilter,
                detectedClassSet = detectedClassSet
        )
        classReader.accept(bytecodePrinterClassVisitor, ClassReader.EXPAND_FRAMES)

        println(detectedClassSet)
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

    override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
        if (opcode == Opcodes.INVOKESPECIAL && name == "<init>")
            doWhenConstructor(owner)

        super.visitMethodInsn(opcode, owner, name, desc, itf)
    }

    private fun doWhenConstructor(generatedClassName: String) {
        val generatedClass = Class.forName(generatedClassName.replace('/', '.'))
        val isManagingTarget = beanManagingTargetFilter.isManageTarget(generatedClass)
        println("in MethodVisitor = $generatedClass")
        if(!isManagingTarget)
            return

        DetectedUnmanagedClass(
                from = clazz,
                methodName = methodName,
                generated = generatedClass
        ).let(detectedClassSet::add)
    }

}