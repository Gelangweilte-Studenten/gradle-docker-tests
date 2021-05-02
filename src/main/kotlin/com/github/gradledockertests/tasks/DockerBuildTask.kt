package com.github.gradledockertests.tasks

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File

/**
 * A gradle task that builds a docker image using a Dockerfile.
 */
@Suppress("unused")
abstract class DockerBuildTask : DockerTask() {

    /**
     * The name of the image to build.
     */
    @Input
    @Optional
    val imageName: Property<String> = project.objects.property(String::class)

    /**
     * The tag to use for the built image.
     */
    @Input
    @Optional
    val tag: Property<String> = project.objects.property(String::class)

    /**
     * The repository of the image.
     * Specify only the name without a trailing /.
     */
    @Input
    @Optional
    val repository: Property<String> = project.objects.property(String::class)

    /**
     * Indicates whether or not the built image should be tagged as latest build.
     */
    @Input
    @Optional
    val tagLatest: Property<Boolean> = project.objects.property(Boolean::class)

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
        if (!checkDockerAvailability()) return

        val actualTag = if (tag.isPresent) tag.get() else project.version.toString()
        val actualRepository = if (repository.isPresent) repository.get() + "/" else ""
        val actualImageName = if (imageName.isPresent) imageName.get() else project.name
        val imageBaseName = "$actualRepository$actualImageName:"
        val argumentList = mutableListOf("docker", "build", "-t", imageBaseName + actualTag)
        if (tagLatest.isPresent && tagLatest.get()) {
            argumentList.add("-t")
            argumentList.add(imageBaseName + "latest")
        }
        argumentList.add(".")

        project.exec {
            workingDir(directory)
            commandLine(argumentList)
        }
    }

    companion object {
        const val Name = "dockerBuild"
    }
}
