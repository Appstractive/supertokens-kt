import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  application
  id("io.ktor.plugin") version "2.3.9"
}

version = "1.0.8"

group = "com.appstractive"

dependencies {
  implementation(projects.server.supertokensSdkBackendKtor)

  implementation(libs.kotlin.serialization)
  implementation(libs.kotlin.coroutines)

  implementation(libs.ktor.serialization)
  implementation(libs.ktor.server.cio)
  implementation(libs.ktor.server.logging)

  implementation(libs.slf4j)

  testImplementation(libs.test.kotlin)
}

application { mainClass.set("com.appstractive.ApplicationKt") }

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "17" }

ktor {
  docker {
    localImageName.set("supertokens-ktor-example")
    imageTag.set(version.toString())
    jreVersion.set(JavaVersion.VERSION_17)

    externalRegistry.set(
        object : io.ktor.plugin.features.DockerImageRegistry {
          override val toImage: Provider<String>
            get() = provider { "hub.appstractive.cloud/supertokens-ktor-example" }

          override val username: Provider<String>
            get() = providers.environmentVariable("DOCKER_HUB_USERNAME")

          override val password: Provider<String>
            get() = providers.environmentVariable("DOCKER_HUB_PASSWORD")
        })

    portMappings.set(
        listOf(
            io.ktor.plugin.features.DockerPortMapping(
                8080, 8080, io.ktor.plugin.features.DockerPortMappingProtocol.TCP)))

    jib.from {
      platforms {
        platform {
          architecture = "amd64"
          os = "linux"
        }
        platform {
          architecture = "arm64"
          os = "linux"
        }
      }
    }
  }
}
