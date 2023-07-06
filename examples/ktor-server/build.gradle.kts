import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22")

        classpath((kotlin("serialization", version = "1.5.1")))
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22"
    application
    id("io.ktor.plugin") version "2.3.1"
}

version = "1.0.3"
group = "com.appstractive"

dependencies {
    implementation("com.appstractive:supertokens-sdk-backend-ktor:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.1")
    implementation("io.ktor:ktor-server-cio-jvm:2.3.1")

    implementation("org.slf4j:slf4j-simple:1.6.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.22")
}

application {
    mainClass.set("io.supertokens.ApplicationKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

ktor {
    docker {
        localImageName.set("supertokens-ktor-example")
        imageTag.set(version.toString())
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)

        externalRegistry.set(object : io.ktor.plugin.features.DockerImageRegistry {
            override val toImage: Provider<String>
                get() = provider { "hub.appstractive.com/supertokens-ktor-example" }
            override val username: Provider<String>
                get() = providers.environmentVariable("DOCKER_HUB_USERNAME")
            override val password: Provider<String>
                get() = providers.environmentVariable("DOCKER_HUB_PASSWORD")
        })

        portMappings.set(
            listOf(
                io.ktor.plugin.features.DockerPortMapping(
                    8080,
                    8080,
                    io.ktor.plugin.features.DockerPortMappingProtocol.TCP
                )
            )
        )
    }
}