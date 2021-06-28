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
    val tagLatest: Property<Boolean> = project.objects.property(Boolean::class).value(true)

    /**
     * The working directory for the docker build process.
     */
    @Internal
    var directory: File = project.projectDir

    /**
     * The target directory where build images can be saved. Set to null to disable saving build image.
     */
    @Internal
    var targetImageFolder: File? = File(project.buildDir, "container-images")

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
     * Specify the path where the new image should be saved.
     */
    fun saveImageTo(directory: File) {
        this.targetImageFolder = directory
    }

    /**
     * Specify the path where the new image should be saved.
     */
    fun saveImageTo(directoryPath: String) {
        targetImageFolder = File(directoryPath)
    }

    /**
     * Set the tagLatest property to the specified value.
     */
    fun tagLatest(isTagLatest: Boolean) {
        tagLatest.set(isTagLatest)
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

        // Execute image build command
        project.exec {
            workingDir(directory)
            commandLine(argumentList)
        }

        // Save the new image
        val targetFolder = targetImageFolder
        if (targetFolder != null) {
            // Ensure target folder exists
            targetFolder.mkdirs()
            val targetImageLocation = File(targetFolder.path, project.name + ".tar")

            // docker-archive doesn't support modifying existing images
            if (targetImageLocation.exists()) {
                targetImageLocation.delete()
            }

            project.exec {
                workingDir(directory)

                commandLine("docker", "save", "-o", targetImageLocation.path, imageBaseName + "latest")
            }
        }
    }

    companion object {
        const val Name = "dockerBuild"
    }
}
