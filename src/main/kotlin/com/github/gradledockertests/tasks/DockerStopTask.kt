package com.github.gradledockertests.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

@Suppress("unused")
class DockerStopTask : DefaultTask() {
    /**
     * List of started containers while gradle execution to stop them after tests finished.
     */
    @Internal
    val containers = mutableListOf<String>()

    /**
     * Adds a container from a [DockerRunTask] to the list of the containers to stop.
     */
    fun stopContainerFromTask(container: TaskProvider<DockerRunTask>) {
        containers += container.get().containerName.get()
    }

    /**
     * Adds a container to the list of the containers to stop by using its name.
     */
    fun stopContainerByName(containerName: String) {
        containers += containerName
    }

    /**
     * Called when gradle wants to execute this task.
     */
    @TaskAction
    fun start() {
        if (containers.isEmpty()) {
            return
        }
        project.exec {
            // Stop and remove container
            it.commandLine(listOf("docker", "stop") + containers.joinToString(" "))
            it.workingDir(project.projectDir)
            it.isIgnoreExitValue = true // Ignore when container already stopped
        }
    }
}
