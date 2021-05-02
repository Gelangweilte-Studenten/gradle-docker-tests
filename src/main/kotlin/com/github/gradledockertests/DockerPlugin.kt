package com.github.gradledockertests

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.ByteArrayOutputStream

/**
 * Gradle docker plugin main class.
 */
@Suppress("unused")
class DockerPlugin : Plugin<Project> {
    private lateinit var project: Project

    override fun apply(project: Project) {
        this.project = project
        isDockerAvailable = checkDockerStatus()

        if (!isDockerAvailable) {
            project.logger.warn("Access to docker not possible. Disabling docker functionality.")
        }
    }

    /**
     * Checks if docker is currently available.
     */
    private fun checkDockerStatus(): Boolean {
        return ByteArrayOutputStream().use { outputStream ->
            try {
                project.exec {
                    commandLine("docker", "version")
                    standardOutput = outputStream
                }.exitValue == 0
            } catch (e: Exception) {
                false
            }
        }
    }

    companion object {
        var isDockerAvailable: Boolean = false
            private set
    }
}
