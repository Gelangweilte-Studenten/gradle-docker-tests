plugins {
    kotlin("jvm") version "1.4.32"
    `java-gradle-plugin`
}

group = "com.github"
version = "1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    val gradleDockerPlugin by plugins.creating {
        id = "com.github.gradle-docker-tests"
        implementationClass = "com.github.gradledockertests.DockerPluginKt"
    }
}
