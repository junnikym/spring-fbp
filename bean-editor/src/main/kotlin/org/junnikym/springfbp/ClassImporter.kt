package org.junnikym.springfbp

import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import org.springframework.stereotype.Component
import java.io.File
import java.net.URLClassLoader
import javax.tools.ToolProvider

@Component
class ClassImporter {

    private fun compile(vararg files: File): Map<File, List<Class<*>>> {
        val javaFiles = mutableListOf<File>()
        val kotlinFiles = mutableListOf<File>()

        files.forEach {
            when(it.extension) {
                "java"-> javaFiles.add(it)
                "kt" -> kotlinFiles.add(it)
                else -> IncompatibleFileException(it)
            }
        }

        return javaFiles.associateWith { compileJava(it) } + kotlinFiles.associateWith { compileKotlin(it) }
    }

        val outDir = File("out/java")
    private fun compileJava(file: File): List<Class<*>> {
        val compilerOptions = arrayOf("-classpath", System.getProperty("java.class.path"), "-d", outDir.absolutePath)

        val exitCode = ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, *compilerOptions, file.absolutePath)
        if (exitCode != 0)
            throw ImportFileCompileException(file)

        return findClasses(outDir)
    }

        val outDir = File("out/kotlin")
    private fun compileKotlin(file: File): List<Class<*>> {
        val arguments = K2JVMCompilerArguments().apply {
            freeArgs = listOf(file.absolutePath)
            classpath = System.getProperty("java.class.path")
            destination = outDir.absolutePath
        }
        val collector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, arguments.verbose)

        val exitCode = K2JVMCompiler().exec(collector, Services.EMPTY, arguments)
        if (exitCode != ExitCode.OK)
            throw ImportFileCompileException(file)

        return findClasses(outDir)
    }

    private fun findClasses(dir: File): List<Class<*>> {
        val classLoader = URLClassLoader(arrayOf(dir.toURI().toURL()))
        return findClassFiles(dir)
                .map { filepathToClassname(dir, it) }
                .map { classLoader.loadClass(it) }
    }

    private fun findClassFiles(dir: File): Collection<File> {
        return dir.listFiles().fold(mutableListOf()) { fileList, it->
            if(it.isDirectory)
                fileList.addAll(findClassFiles(it))
            if(it.extension == "class")
                fileList.add(it)

            fileList
        }
    }

    private fun filepathToClassname(outDir: File, classFile: File): String {
        return classFile.path
                .substring(outDir.path.length)
                .let {
                    if(it[0] == '/')
                        it.substring(1)
                    else
                        it
                }
                .let { it.replace(".class", "") }
                .let { it.replace("/", ".") }
    }

}