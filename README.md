# gradle-docker-tests

A gradle plugin that allows to provide docker containers and images that can be used in unit tests.

This plugin is in development and features may change or be removed at any time!
To use the plugin, an active docker installation is required, and the user executing gradle needs to be able to execute
docker commands.

# Documentation

## Installation

The recommended way to install this plugin is by using the gradle `plugins` block (build.gradle.kts):

```gradle
plugins {
    id("com.github.gradle-docker.gradle-docker-tests") version "1.0.0"
}
```

## Usage

As of now, the plugin has three major use cases. All examples require the user to import the required tasks using an
import statement:
`import com.github.gradledockertests.tasks.*`
A wildcard import is not necessary but is used for convenience here.

### Build a docker image

By default, the `DockerBuildTask` builds a docker image using a Dockerfile in the current project directory. The name of
the image is the name of the current project, and the current version is added as tag.

```gradle
val buildImage by registering(DockerBuildTask::class)
```

**This task is only executed if invoked directly or specified as dependency or finalizer for another task.**

It is also possible to configure the docker build. The following example defines the repository name and image name for
the created image. The name definition is redundant in this case because the project name is already the default value.

```gradle
val buildImage by registering(DockerBuildTask::class) {
    imageName(project.name)
    repository("hs-aalen")
}
```

By default, the `latest` tag is added to the image. This can be disabled with `tagLatest.set(false)`. The working
directory for the docker build can be specified by using the `directory` property.

### Run a docker container

The most important use case of the plugin is to run one or more docker containers during unit tests. This can be
accomplished by using the `DockerRunTask`.

```gradle
val dockerRun by registering(DockerRunTask::class) {
    environment["MONGO_INITDB_ROOT_USERNAME"] = "root"
    environment["MONGO_INITDB_ROOT_PASSWORD"] = "password"
    environment["MONGO_INITDB_DATABASE"] = "notes"
    addPort(freeHostPort, 27017)
    args("-itd")
    image("mongo")
}
```

This example shows the creation of a MongoDB docker container. Environment variables can be set using the `environment`
map property. Port mappings can be added with the `addPort` method that accepts two integers: The host port the
container port. It is possible to assign a free host port automatically through the use of the `freeHostPort` property.

**If you assign multiple port mappings and need to access them later it is not recommended using this property.**

### Stop a docker container

The containers created by the `DockerRunTask` can be stopped using the `DockerStopTask`. To do this, simply add the task
provider of the run task to the stop task:

```gradle
val stopDocker by registering(DockerStopTask::class) {
    stopContainerFromTask(taskToStop)
}
```

It is also possible to stop containers from multiple run tasks using the `stopContainersFromTasks` method that accepts a
vararg of `DockerRunTask` task providers.
