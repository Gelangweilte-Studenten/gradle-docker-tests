package com.github.gradledockertests.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream

@Suppress("unused")
class DockerRunTask: DefaultTask() {

    /**
     * Specifies the name of the image to run.
     */
    @Input
    val image: Property<String> = project.objects.property(String::class.java)

    /**
     * The environment variables to be configured in docker container.
     */
    @Input
    val environment: MapProperty<String, String> = project.objects.mapProperty(String::class.java, String::class.java)

    /**
     * Used to configure the port for the host of a container.
     */
    @Optional
    @Input
    val hostPort: Property<Int> = project.objects.property(Int::class.java)

    /**
     * Used to configure the local port of a container.
     */
    @Optional
    @Input
    val containerPort: Property<Int> = project.objects.property(Int::class.java)

    /**
     * The container name will automatically be set after this task has been executed by analyzing the output stream.
     * Note: This is an internal property that should not be visible from outside to prevent hardcoded container names.
     */
    @Optional
    @Input
    val containerName: Property<String> = project.objects.property(String::class.java)

    /**
     * Additional docker arguments that are not already covered by the other properties.
     */
    @Input
    var args: ListProperty<String> = project.objects.listProperty(String::class.java)

    /**
     * Additional docker arguments that are not already covered by the other properties.
     */
    fun args(vararg args: String) {
        this.args.set(args.toList())
    }

    /**
     * Used to configure the local port of a container.
     */
    fun containerPort(port: Int) {
        this.containerPort.set(port)
    }

    /**
     * Used to configure the port for the host of a container.
     */
    fun hostPort(port: Int) {
        this.hostPort.set(port)
    }

    /**
     * Easier kotlin specific way to simplify environment map.
     */
    operator fun <K : Any, V : Any> MapProperty<K, V>.set(key: K, value: V) {
        this.put(key, value)
    }

    /**
     * Called when gradle wants to execute this task.
     */
    @TaskAction
    fun start() {
        ByteArrayOutputStream().use { output ->
            project.exec {
                it.workingDir(project.projectDir)
                it.commandLine(toCommandLine())
                println("Execute command: " + it.commandLine.joinToString(" "))

                // Used to store the output
                it.standardOutput = output
            }
            if (!containerName.isPresent) {
                containerName.set(output.toString().lineSequence().first())
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
        if (hostPort.isPresent && containerPort.isPresent) {
            cmd += "-p"
            cmd += hostPort.get().toString() + ":" + containerPort.get()
        }
        if (containerName.isPresent) {
            cmd += "--name"
            cmd += containerName.get()
        }
        cmd += args.get()
        return cmd
    }
}
