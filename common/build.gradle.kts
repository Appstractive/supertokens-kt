plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
	id("com.android.library")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka") version "1.8.20"
    `maven-publish`
    signing
}

group = "com.appstractive"
version = properties["version"].toString()

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

kotlin {
    androidTarget()
    jvm("jvm") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    ios()
    iosSimulatorArm64()

    mingwX64("win")
    linuxX64("linux64")
    linuxArm64("linuxArm64")

    cocoapods {
        summary = "SuperTokens common classes for frontend and backend SDKs"
        homepage = "Link to the Shared Module homepage"
        version = properties["version"].toString()
        ios.deploymentTarget = "14.1"
        framework {
            baseName = "common"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.serialization)
                implementation(libs.kotlin.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.test.kotlin)
            }
        }
    }

    val publicationsFromMainHost = listOf(
        jvm("jvm").name,
        mingwX64("win").name,
        linuxX64("linux64").name,
        linuxArm64("linuxArm64").name,
        "kotlinMultiplatform",
    )

    publishing {

        repositories {
            maven {
                name="oss"
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

                credentials {
                    username = extra["mavenUser"].toString()
                    password = extra["mavenPassword"].toString()
                }
            }
        }

        publications {
            matching { it.name in publicationsFromMainHost }.all {
                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>()
                    .matching { it.publication == targetPublication }
            }

            withType<MavenPublication> {
                artifact(javadocJar)
                pom {
                    name.set("SuperTokens-SDK-Common")
                    description.set("SuperTokens common classes for frontend and backend SDKs")
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
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(signingTasks)
}

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
    namespace = "com.supertokens"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}