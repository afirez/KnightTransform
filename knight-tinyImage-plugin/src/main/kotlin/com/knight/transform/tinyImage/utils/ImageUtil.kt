package com.knight.transform.tinyImage.utils

import com.android.build.gradle.BaseExtension
import com.knight.transform.Utils.Log
import com.knight.transform.tinyImage.Context
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import javax.imageio.ImageIO

object ImageUtil {
    private const val TAG = "ImageUtil"
    private const val VERSION_SUPPORT_WEBP = 14
    private const val VERSION_SUPPORT_ALPHA_WEBP = 18

    fun isImage(file: File): Boolean {
        return (file.name.endsWith(Context.JPG) ||
                file.name.endsWith(Context.PNG) ||
                file.name.endsWith(Context.JPEG)
                ) && !file.name.endsWith(Context.DOT_9PNG)
    }

    fun isPicture(file: File): Boolean {
        return (file.name.endsWith(Context.WEBP) ||
                file.name.endsWith(Context.JPG) ||
                file.name.endsWith(Context.PNG) ||
                file.name.endsWith(Context.JPEG)
                ) && !file.name.endsWith(Context.DOT_9PNG)
    }


    fun isJPG(file: File): Boolean {
        return file.name.endsWith(Context.JPG) || file.name.endsWith(Context.JPEG)
    }

    fun isSupportConvertToWebp(project: Project): Boolean {
        return getMinSdkVersion(project) >= VERSION_SUPPORT_WEBP
    }

    fun isSupportConvertToWebpWithAlpha(project: Project): Boolean {
        return getMinSdkVersion(project) >= VERSION_SUPPORT_ALPHA_WEBP
    }

    fun isAlphaPNG(imageFile: File): Boolean {
        return if (imageFile.exists()) {
            try {
                val img = ImageIO.read(imageFile)
                img.colorModel.hasAlpha()
            } catch (e: Exception) {
                Log.i(e.message ?: "")
                false
            }
        } else {
            false
        }
    }

    private fun getMinSdkVersion(project: Project): Int {
        return (project.property("android") as BaseExtension).defaultConfig.minSdkVersion.apiLevel
    }


    fun command(context: Context, cmd: String) {
        val rootStr = if (context.extension.rootPath.isNotEmpty()) context.extension.rootPath else context.project.rootDir.path
        var url = ""
        val cdStr = run {
            val system = System.getProperty("os.name")
            when (system) {
                "Mac OS X" -> {
                    url = "https://github.com/296777513/KnightTransform/releases/download/1.0.2/tinytools_mac.zip"
                    "${rootStr}/tinytools/mac/$cmd"

                }
                "Linux" -> {
                    url = "https://github.com/296777513/KnightTransform/releases/download/1.0.2/tinytools_linux.zip"
                    "${rootStr}/tinytools/linux/$cmd"
                }
                "Windows" -> {
                    url = "https://github.com/296777513/KnightTransform/releases/download/1.0.2/tinytools_windows.zip"
                    "${rootStr}/tinytools/windows/$cmd"
                }
                else -> ""
            }
        }
        try {
            val process = Runtime.getRuntime().exec(cdStr)
            process.waitFor()
        } catch (e: Exception) {
            Log.i("exception is  : ${e}")
            throw  GradleException("$e \n you should download resource at \n $url \n, and put the unzip tinytools in the root of project's directory")
        }
    }
}