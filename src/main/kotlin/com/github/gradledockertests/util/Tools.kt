package com.github.gradledockertests.util

import com.github.gradledockertests.tasks.DockerRunTask
import org.gradle.api.tasks.TaskProvider
import java.net.ServerSocket

/**
 * Retrieves a free port on the current host system.
 */
val freeHostSystemPort: Int
    get() = ServerSocket(0).use { it.localPort }

/**
 * Extension property to easily retrieve the first port published to the host by the container.
 * Results may be inconsistent if multiple ports are mapped for one container.
 *
 * @see publishedPorts
 */
val TaskProvider<DockerRunTask>.firstPublishedPort: Int
    get() = get().portMapping.get().entries.first().key

/**
 * Extension property to retrieve all published ports of a container.
 * Ports may not be ordered.
 */
val TaskProvider<DockerRunTask>.publishedPorts: Set<Int>
    get() = get().portMapping.get().keys.toSet()
