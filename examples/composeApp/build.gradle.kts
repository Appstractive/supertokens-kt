plugins {
    kotlin("multiplatform")
    id("com.android.application")
    kotlin("native.cocoapods")
    alias(libs.plugins.compose)
    kotlin("plugin.parcelize")
}

kotlin {
    cocoapods {
        version = "1.0"
        summary = "Compose app"
        homepage = "not published"
        ios.deploymentTarget = "13.0"
    }

    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "app"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":sdk:frontend"))

            implementation(libs.kotlin.serialization)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.kotlin.coroutines)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)

            implementation(libs.compose.qr)

            implementation(libs.circuit.core)
            implementation(libs.circuit.overlays)
            implementation(libs.circuitx.overlays)
        }

        androidMain.dependencies {
            api(libs.androidx.activity.compose)
            api(libs.androidx.appcompat)
            api(libs.androidx.coreKtx)

            implementation(libs.kotlin.coroutines.android)

            implementation(libs.circuitx.android)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(compose.desktop.common)
        }
    }
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.myapplication"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.myapplication.MyApplication"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
