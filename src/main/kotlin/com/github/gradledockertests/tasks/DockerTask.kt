package com.github.gradledockertests.tasks

import com.github.gradledockertests.DockerPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.property

/**
 * The default docker task extended by all other docker tasks.
 */
abstract class DockerTask: DefaultTask() {

    /**
     * Specifies whether or not the task should ignore the docker availability status.
     *
     * Setting this to true will skip the execution of any docker command during a task if no docker service is running
     * which may lead to errors in other tasks.
     */
    @Input
    @Optional
    val ignoreDockerAvailabilityStatus: Property<Boolean> = project.objects.property(Boolean::class)

    /**
     * Checks if the docker service is available.
     * @return true if the service is available, false if not.
     */
    protected fun checkDockerAvailability() : Boolean {
        if (!DockerPlugin.isDockerAvailable && !ignoreDockerAvailabilityStatus.getOrElse(false)) {
            project.logger.error("Docker not available. Task can't be executed.")
            throw IllegalStateException("Docker service not available.")
        } else if (!DockerPlugin.isDockerAvailable) {
            project.logger.warn("Docker not available. Docker execution skipped.")
            return false
        }

        return true
    }
}
