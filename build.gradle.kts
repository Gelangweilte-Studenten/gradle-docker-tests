plugins {
    kotlin("jvm") version "1.4.32"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "com.github"
version = "1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins.create("gradleDockerTests") {
        id = "com.github.gradle-docker-tests"
        implementationClass = "com.github.gradledockertests.DockerPlugin"
    }
}

pluginBundle {
    website = "https://github.com/Gelangweilte-Studenten/gradle-docker-tests"
    vcsUrl = "https://github.com/Gelangweilte-Studenten/gradle-docker-tests"
    description = "A plugin to easily create docker containers for local testing."
}

publishing {
    repositories {
        mavenLocal()
    }
}
