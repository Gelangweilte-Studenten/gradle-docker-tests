package com.github.gradledockertests.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

@Suppress("unused")
class DockerBuildTask : DefaultTask() {

    /**
     * The tag to use for the built image.
     */
    @Input
    @Optional
    val tag: Property<String> = project.objects.property(String::class.java)

    /**
     * The name of the image to build.
     */
    @Input
    val imageName: Property<String> = project.objects.property(String::class.java)

    /**
     * The repository of the image.
     * Specify only the name without a trailing /.
     */
    @Input
    @Optional
    val repository: Property<String> = project.objects.property(String::class.java)

    /**
     * Indicates whether or not the built image should be tagged as latest build.
     */
    @Input
    @Optional
    val tagLatest: Property<Boolean> = project.objects.property(Boolean::class.java)

    /**
     * The working directory for the docker build process.
     */
    @Internal
    var directory: File = project.projectDir

    /**
     * Utility method to specify the tag directly.
     */
    fun tag(tag: String) {
        this.tag.set(tag)
    }

    /**
     * Utility method to specify the image name directly.
     */
    fun imageName(imageName: String) {
        this.imageName.set(imageName)
    }

    /**
     * Utility method to specify the repository directly.
     */
    fun repository(repository: String) {
        this.repository.set(repository.replace("/", ""))
    }

    /**
     * Utility method to specify the working directory directly.
     */
    fun directory(directory: File) {
        this.directory = directory
    }

    /**
     * Executes the docker build process using the provided arguments.
     */
    @TaskAction
    private fun start() {
        val actualTag = if (tag.isPresent) tag.get() else project.version.toString()
        val actualRepository = if (repository.isPresent) repository.get() + "/" else ""
        val imageBaseName = actualRepository + imageName.get() + ":"
        val argumentList = mutableListOf("docker", "build", "-t", imageBaseName + actualTag)
        if (tagLatest.isPresent && tagLatest.get()) {
            argumentList.add("-t")
            argumentList.add(imageBaseName + "latest")
        }
        argumentList.add(".")

        project.exec {
            it.workingDir(directory)
            it.commandLine(argumentList)
        }
    }
}
