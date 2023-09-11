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

    fun compile(vararg files: File): List<Class<*>> {
        val javaFiles = mutableListOf<File>()
        val kotlinFiles = mutableListOf<File>()

        files.forEach {
            when(it.extension) {
                "java"-> javaFiles.add(it)
                "kt" -> kotlinFiles.add(it)
                else -> IncompatibleFileException(it)
            }
        }

        return compileJava(javaFiles) + compileKotlin(kotlinFiles)
    }

    private fun compileJava(files: List<File>): List<Class<*>> {
        val outDir = File("out/java")
        val compilerOptions = arrayOf("-classpath", System.getProperty("java.class.path"), "-d", outDir.absolutePath)

        val exitCode = ToolProvider.getSystemJavaCompiler()
                .run(null, null, null, *compilerOptions, *files.map { it.absolutePath }.toTypedArray())
        if (exitCode != 0)
            throw ImportFileCompileException(files)

        val classLoader = URLClassLoader(arrayOf(outDir.toURI().toURL()))
        return findClassFile(outDir)
                .map { filepathToClassname(outDir, it) }
                .map { classLoader.loadClass(it) }
    }

    private fun compileKotlin(files: List<File>): List<Class<*>> {
        val outDir = File("out/kotlin")
        val arguments = K2JVMCompilerArguments().apply {
            freeArgs = files.map { it.absolutePath }
            classpath = System.getProperty("java.class.path")
            destination = outDir.absolutePath
        }
        val collector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, arguments.verbose)

        val exitCode = K2JVMCompiler().exec(collector, Services.EMPTY, arguments)
        if (exitCode != ExitCode.OK)
            throw ImportFileCompileException(files)

        val classLoader = URLClassLoader(arrayOf(outDir.toURI().toURL()))
        return findClassFile(outDir)
                .map { filepathToClassname(outDir, it) }
                .map { classLoader.loadClass(it) }
    }

    private fun findClassFile(dir: File): Collection<File> {
        return dir.listFiles().fold(mutableListOf()) { fileList, it->
            if(it.isDirectory)
                fileList.addAll(findClassFile(it))
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