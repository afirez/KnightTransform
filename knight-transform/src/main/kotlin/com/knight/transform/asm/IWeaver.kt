package com.knight.transform.asm

import java.io.File

abstract class IWeaver(var classloader: ClassLoader? = null, var isNeedScanRClass: Boolean = false) {
    abstract fun weaveJar(inputJar: File, outputJar: File)
    abstract fun weaveFile(inputFile: File, outputFile: File, inputDir: String)
    open fun isWeaveableClass(filePath: String): Boolean = if (isNeedScanRClass) {
        filePath.endsWith(".class")
    } else {
        filePath.endsWith(".class")
                && !filePath.contains("R$")
                && !filePath.contains("R.class")
                && !filePath.contains("BuildConfig.class")
    }
}