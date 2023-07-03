plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
	id("com.android.library")
    kotlin("plugin.serialization")
    `maven-publish`
}

group = "com.appstractive"
version = "1.0.0"

kotlin {
    android()
    jvm("jvm") {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
    }

    ios()
    iosSimulatorArm64()

    mingwX64("win")
    linuxX64("linuxX64")
    linuxArm64("linuxArm64")
    js("js")

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../ios/Podfile")
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
        linuxX64("linuxX64").name,
        linuxArm64("linuxArm64").name,
        js("js").name,
        "kotlinMultiplatform",
    )

    publishing {
        publications {
            matching { it.name in publicationsFromMainHost }.all {
                group = "com.appstractive"
                version = "1.0.0"

                val targetPublication = this@all
                tasks.withType<AbstractPublishToMaven>()
                    .matching { it.publication == targetPublication }
            }
        }
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    namespace = "com.supertokens"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}