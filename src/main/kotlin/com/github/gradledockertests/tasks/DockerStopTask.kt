package com.github.gradledockertests.tasks

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * A task that allows to stop running containers from other tasks.
 */
@Suppress("unused")
abstract class DockerStopTask : DockerTask() {
    /**
     * List of started containers while gradle execution to stop them after tests finished.
     */
    @Internal
    val containers = mutableListOf<DockerRunTask>()

    /**
     * Adds containers from multiple [DockerRunTask] instances to the list of the containers to stop.
     */
    fun stopContainersFromTasks(vararg containers: TaskProvider<DockerRunTask>) {
        this.containers.addAll(containers.map { it.get() })
    }

    /**
     * Adds a container from a single [DockerRunTask] instance to the list of containers to stop.
     */
    fun stopContainerFromTask(container: TaskProvider<DockerRunTask>) {
        containers += container.get()
    }

    /**
     * Called when gradle wants to execute this task.
     */
    @TaskAction
    fun start() {
        if (!checkDockerAvailability()) return

        if (containers.isEmpty()) {
            return
        }
        project.exec {
            // Stop and remove container
            commandLine(listOf("docker", "stop") + containers.joinToString(" ") { it.containerName })
            workingDir(project.projectDir)
            isIgnoreExitValue = true // Ignore when container already stopped
        }
    }

    companion object {
        const val Name = "dockerStop"
    }
}
