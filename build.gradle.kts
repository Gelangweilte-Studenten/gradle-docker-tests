plugins {
    kotlin("jvm") version "1.4.31"
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.14.0"
    id("org.jetbrains.dokka") version "1.4.32"
}

group = "com.github.gradle-docker"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation(kotlin("test-junit5"))
}

val javaCompatibilityVersion = JavaVersion.VERSION_1_8
val pluginName = "gradleDockerTests"

java {
    sourceCompatibility = javaCompatibilityVersion
    targetCompatibility = javaCompatibilityVersion
}

gradlePlugin {
    plugins.create(pluginName) {
        id = "$group.gradle-docker-tests"
        implementationClass = "com.github.gradledockertests.DockerPlugin"
    }
}

pluginBundle {
    website = "https://github.com/Gelangweilte-Studenten/gradle-docker-tests"
    vcsUrl = "https://github.com/Gelangweilte-Studenten/gradle-docker-tests"
    description = "A plugin to easily create docker containers for local testing."

    (plugins) {
        pluginName {
            id = project.group.toString()
            displayName = "Gradle Docker test plugin"
            description = "Gradle plugin for building and running docker images and containers that can be used during unit tests."
            tags = listOf("java", "kotlin", "gradle", "docker", "test")
        }
    }

    mavenCoordinates {
        groupId = project.group.toString()
        artifactId = "gradle-docker-tests"
    }
}

publishing {
    repositories {
        mavenLocal()
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
}
