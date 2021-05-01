package com.github.gradledockertests.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

@Suppress("unused")
open class DockerStopTask : DefaultTask() {
    /**
     * List of started containers while gradle execution to stop them after tests finished.
     */
    @Internal
    val containers = mutableListOf<DockerRunTask>()

    /**
     * Adds a container from a [DockerRunTask] to the list of the containers to stop.
     */
    fun stopContainerFromTask(container: TaskProvider<DockerRunTask>) {
        containers += container.get()
    }

    /**
     * Called when gradle wants to execute this task.
     */
    @TaskAction
    fun start() {
        if (containers.isEmpty()) {
            return
        }
        project.exec { exec ->
            // Stop and remove container
            exec.commandLine(listOf("docker", "stop") + containers.joinToString(" ") { it.containerName } )
            exec.workingDir(project.projectDir)
            exec.isIgnoreExitValue = true // Ignore when container already stopped
        }
    }
}
