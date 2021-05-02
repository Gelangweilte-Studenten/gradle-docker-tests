package com.github.gradledockertests.tasks

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import java.io.ByteArrayOutputStream

@Suppress("unused")
abstract class DockerRunTask : DockerTask() {

    /**
     * Specifies the name of the image to run.
     */
    @Input
    val image: Property<String> = project.objects.property(String::class)

    /**
     * The environment variables to be configured in docker container.
     */
    @Input
    val environment: MapProperty<String, String> = project.objects.mapProperty(String::class, String::class)

    /**
     * The port mapping used by the created container.
     * Mapping is specified as (host port, container port) according to docker syntax.
     */
    @Input
    val portMapping: MapProperty<Int, Int> = project.objects.mapProperty(Int::class, Int::class)

    /**
     * The container name will automatically be set after this task has been executed by analyzing the output stream.
     * Note: This is an internal property that should not be visible from outside to prevent hardcoded container names.
     */
    @Internal
    var containerName: String = ""
        private set

    /**
     * Additional docker arguments that are not already covered by the other properties.
     * @see args
     */
    @Input
    var args: ListProperty<String> = project.objects.listProperty(String::class)

    /**
     * Utility function to specify the image name.
     */
    fun image(imageName: String) {
        this.image.set(imageName)
    }

    /**
     * Additional docker arguments that are not already covered by the other properties.
     */
    fun args(vararg args: String) {
        this.args.set(args.toList())
    }

    /**
     * Adds or updates a port mapping for this run task.
     */
    fun addPort(host: Int, container: Int) {
        this.portMapping[host] = container
    }

    /**
     * Removes a port mapping for this run task.
     */
    fun removePort(host: Int) {
        this.portMapping.get().remove(host)
    }

    /**
     * Manually set the container name for the started container.
     */
    fun containerName(containerName: String) {
        this.containerName = containerName
    }

    /**
     * Easier kotlin specific way to simplify map property assignments.
     */
    operator fun <K : Any, V : Any> MapProperty<K, V>.set(key: K, value: V) {
        this.put(key, value)
    }

    /**
     * Called when gradle wants to execute this task.
     */
    @TaskAction
    fun start() {
        if (!checkDockerAvailability()) return

        ByteArrayOutputStream().use { output ->
            project.exec {
                workingDir(project.projectDir)
                commandLine(toCommandLine())
                println("Execute command: " + commandLine.joinToString(" "))

                // Used to store the output
                standardOutput = output
            }
            if (containerName.isBlank()) {
                containerName = output.toString().lineSequence().first()
            }
            println("Started container $containerName")
        }
    }

    /**
     * Build to command line from this docker configuration task.
     */
    private fun toCommandLine(): List<String> {
        val cmd = mutableListOf("docker", "run", "--rm") // --rm ensures that the container gets removed after stop
        for ((key, value) in environment.get()) {
            cmd += "-e"
            cmd += "$key=$value"
        }
        for ((hostPort, containerPort) in portMapping.get()) {
            cmd += "-p"
            cmd += "$hostPort:$containerPort"
        }

        if (containerName.isNotBlank()) {
            cmd += "--name"
            cmd += containerName
        }
        cmd += args.get()
        cmd += image.get()
        return cmd
    }

    companion object {
        const val Name = "dockerRun"
    }
}
