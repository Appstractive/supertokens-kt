import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm")
  kotlin("plugin.serialization")
  id("org.jetbrains.dokka") version "1.8.20"
  `maven-publish`
  signing
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by
    tasks.registering(Jar::class) {
      dependsOn(dokkaHtml)
      archiveClassifier.set("javadoc")
      from(dokkaHtml.outputDirectory)
    }

java {
  targetCompatibility = JavaVersion.VERSION_21
  withSourcesJar()
}

dependencies {
  api(projects.sdk.supertokensSdkBackend)

  implementation(libs.kotlin.serialization)
  implementation(libs.kotlin.coroutines)

  implementation(libs.ktor.serialization)

  implementation(libs.ktor.client.core)
  implementation(libs.ktor.client.cio)
  implementation(libs.ktor.client.contentnegotiation)
  implementation(libs.ktor.client.serialization)
  implementation(libs.ktor.client.json)
  implementation(libs.ktor.client.logging)

  api(libs.ktor.server.core)
  api(libs.ktor.server.contentnegotiation)
  api(libs.ktor.server.statuspages)
  api(libs.ktor.server.auth)
  api(libs.ktor.server.auth.jwt)

  implementation(libs.slf4j)
  implementation(libs.jwt)

  testImplementation(libs.test.kotlin)
}

tasks.withType<KotlinCompile> { kotlinOptions.jvmTarget = "21" }

publishing {
  repositories {
    if(extra.has("mavenUser")) {
      maven {
        name = "oss"
        val releasesRepoUrl =
            uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

        credentials {
          username = extra.get("mavenUser")?.toString()
          password = extra.get("mavenPassword")?.toString()
        }
      }
    }
  }

  publications {
    create<MavenPublication>("SupertokensSdkBackendKtor") {
      artifact(javadocJar)

      groupId = "com.appstractive"
      artifactId = "supertokens-sdk-backend-ktor"
      version = rootProject.version.toString()

      from(components["java"])

      pom {
        name.set("SuperTokens-SDK-Backend-Ktor")
        description.set("SuperTokens backend SDK")
        url.set("https://github.com/Appstractive/supertokens-kt")
        licenses {
          license {
            name.set("Apache License 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0")
          }
        }
        issueManagement {
          system.set("Github")
          url.set("https://github.com/Appstractive/supertokens-kt/issues")
        }
        scm {
          connection.set("https://github.com/Appstractive/supertokens-kt.git")
          url.set("https://github.com/Appstractive/supertokens-kt")
        }
        developers {
          developer {
            name.set("Andreas Schulz")
            email.set("dev@appstractive.com")
          }
        }
      }
    }
  }
}

val signingTasks = tasks.withType<Sign>()

tasks.withType<AbstractPublishToMaven>().configureEach { dependsOn(signingTasks) }

signing {
  useInMemoryPgpKeys(
      extra["signingKey"].toString(),
      extra["signingPassword"].toString(),
  )
  sign(publishing.publications)
}
