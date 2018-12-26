package com.knight.transform.asm

import com.knight.transform.weave.ExtendClassWriter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


class ScanWeaver(private val wrapClassWriter: (ClassWriter) -> ClassVisitor?) : IWeaver() {

    override fun weaveJar(inputJar: File, outputJar: File) {
        val inputZip = ZipFile(inputJar)
        inputZip.entries().toList().forEach { entry ->
            val outEntry = ZipEntry(entry.name)
            if (isWeaveableClass(outEntry.name.replace("/", "."))) {
                scanWithByte(BufferedInputStream(inputZip.getInputStream(entry)))
            }
        }
    }

    override fun weaveFile(inputFile: File, outputFile: File, inputDir: String) {
        var inputBaseDir = inputDir
        if (!inputBaseDir.endsWith("/")) inputBaseDir += "/"
        if (inputFile.isFile && isWeaveableClass(inputFile.absolutePath.replace(inputBaseDir, "").replace("/", "."))) {
            val inputStream = FileInputStream(inputFile)
            scanWithByte(inputStream)
        }
    }

    fun scanWithByte(intputStream: InputStream) {
        val classReader = ClassReader(intputStream)
        val classWriter = ExtendClassWriter(classloader, classReader,
                ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val classVisitor = wrapClassWriter(classWriter)
        try {
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        } catch (e: Exception) {
            println("Exception occurred when visit code \n " + e.printStackTrace())

        }
    }
}