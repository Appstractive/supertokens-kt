plugins {
  kotlin("multiplatform")
  kotlin("native.cocoapods")
  id("com.android.library")
  kotlin("plugin.serialization")
  id("org.jetbrains.dokka") version "1.8.20"
  `maven-publish`
  signing
}

group = rootProject.group

version = rootProject.version

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by
    tasks.registering(Jar::class) {
      dependsOn(dokkaHtml)
      archiveClassifier.set("javadoc")
      from(dokkaHtml.outputDirectory)
    }

kotlin {
  androidTarget { publishLibraryVariants("release", "debug") }
  jvm("jvm") { compilations.all { kotlinOptions.jvmTarget = "17" } }

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  cocoapods {
    summary = "SuperTokens frontend SDK"
    homepage = "https://github.com/Appstractive/supertokens-kt"
    version = rootProject.version.toString()
    ios.deploymentTarget = "14.1"
    framework {
      baseName = "frontend_sdk"
      isStatic = true
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(projects.supertokensSdkCommon)

        implementation(libs.kotlin.serialization)
        implementation(libs.kotlin.serialization.json)
        implementation(libs.kotlin.coroutines)

        implementation(libs.ktor.serialization)

        api(libs.ktor.client.core)
        implementation(libs.ktor.client.cio)
        implementation(libs.ktor.client.contentnegotiation)
        implementation(libs.ktor.client.serialization)
        implementation(libs.ktor.client.json)
        implementation(libs.ktor.client.logging)
        api(libs.ktor.client.auth)

        implementation(libs.jwt)
        implementation(libs.settings)
      }
    }
    val commonTest by getting { dependencies { implementation(kotlin("test")) } }
    val androidMain by getting {
      dependencies {
        implementation(libs.android.startup)
        implementation(libs.android.crypto)
      }
    }
    val iosX64Main by getting
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosX64Main.dependsOn(this)
      iosArm64Main.dependsOn(this)
      iosSimulatorArm64Main.dependsOn(this)
    }
    val jvmMain by getting
  }

  val publicationsFromMainHost =
      listOf(
          androidTarget(),
          jvm("jvm").name,
          "kotlinMultiplatform",
      )

  publishing {
    repositories {
      maven {
        name = "oss"
        val releasesRepoUrl =
            uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
        val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

        credentials {
          username = extra["mavenUser"].toString()
          password = extra["mavenPassword"].toString()
        }
      }
    }

    publications {
      matching { it.name in publicationsFromMainHost }
          .all {
            val targetPublication = this@all
            tasks.withType<AbstractPublishToMaven>().matching {
              it.publication == targetPublication
            }
          }

      withType<MavenPublication> {
        artifactId = "supertokens-sdk-frontend"
        version = rootProject.version.toString()
        if (name == "androidRelease") {
          afterEvaluate { artifactId = "supertokens-sdk-frontend-android" }
        } else if (name == "androidDebug") {
          afterEvaluate { artifactId = "supertokens-sdk-frontend-android-debug" }
        } else if (name != "kotlinMultiplatform") {
          artifactId = "$artifactId-$name"
        }

        artifact(javadocJar)
        pom {
          name.set("SuperTokens-SDK-Frontend")
          description.set("SuperTokens frontend SDK")
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

android {
  compileSdk = libs.versions.compileSdk.get().toInt()
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  namespace = "com.supertokens.sdk"

  defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
