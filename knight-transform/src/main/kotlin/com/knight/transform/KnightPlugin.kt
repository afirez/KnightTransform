package com.knight.transform

import com.android.build.api.transform.Transform
import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.ApplicationVariantImpl
import com.android.builder.model.AndroidProject
import com.knight.transform.Utils.Timer
import org.gradle.api.*
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import transform.task.OutPutMappingTask
import transform.task.WeavedClass
import java.util.concurrent.TimeUnit

abstract class KnightPlugin<E : BaseExtension, C : BaseContext> : Plugin<Project>, IPlugin {

    protected lateinit var context: C
    protected lateinit var extension: E

    protected lateinit var project: Project
    protected lateinit var android: TestedExtension
    protected abstract val isNeedPrintMapAndTaskCostTime: Boolean
    private lateinit var transform: Transform


    abstract fun getContext(project: Project, extension: E, android: TestedExtension): C
    abstract fun createExtensions(): E


    override fun apply(project: Project) {
        if (!project.plugins.hasPlugin(AppPlugin::class.java) && !project.plugins.hasPlugin(LibraryPlugin::class.java)) {
            throw  GradleException("'com.android.application' or 'com.android.library' plugin required!")
        }

        val aClass = if (project.plugins.hasPlugin(AppPlugin::class.java))
            AppExtension::class.java
        else LibraryExtension::class.java
        this.project = project
        android = project.extensions.getByType(aClass)
        extension = createExtensions()
        context = getContext(project, extension, android)
        if (isNeedPrintMapAndTaskCostTime) {
            if (android is AppExtension) {
                (android as AppExtension).applicationVariants.all {
                    createWriteMappingTask(it, context)
                }
            } else if (android is LibraryExtension) {
                (android as LibraryExtension).libraryVariants.all {
                    createWriteMappingTask(it, context)
                }
            }
        }
        transform = createTransform()
        android.registerTransform(transform)
    }


    abstract fun createTransform(): Transform


    private fun createWriteMappingTask(variant: BaseVariant, context: BaseContext) {
        val mappingTaskName = "${transform.name}outputMappingFor${variant.name.capitalize()}"
        val myTask = project.tasks.getByName("transformClassesWith${transform.name}For${variant.name.capitalize()}")
        myTask.apply {
            doFirst {
                Timer.start(name)
            }

            doLast {
                Timer.stop(name)
            }
        }
        val outputMappingTask =
//                try {
//            project.tasks.getByName(mappingTaskName)
//        } catch (e: UnknownTaskException) {
                project.tasks.create(mappingTaskName, OutPutMappingTask::class.java)
//        }

        outputMappingTask.apply {
            this as OutPutMappingTask
            classes.set(context.weavedClassMap)
            variantName.set(transform.name)
            outputMappingFile.set(com.android.utils.FileUtils.join(project.buildDir, AndroidProject.FD_OUTPUTS, "mapping",
                    transform.name, variant.name, "${transform.name}Mapping.txt"))

            doFirst {
                Timer.start(name)
            }

            doLast {
                Timer.stop(name)
            }
        }

        myTask.finalizedBy(outputMappingTask)
        outputMappingTask.onlyIf { myTask.didWork }
        outputMappingTask.dependsOn(myTask)
    }
}