plugins {
  kotlin("multiplatform")
  id("com.android.application")
  kotlin("native.cocoapods")
  alias(libs.plugins.compose)
  alias(libs.plugins.compose.compiler)
  kotlin("plugin.parcelize")
}

kotlin {
  applyDefaultHierarchyTemplate()

  cocoapods {
    version = "1.0"
    summary = "app"
    homepage = "not published"
    ios.deploymentTarget = "13.0"
  }

  androidTarget()

  listOf(
          iosX64(),
          iosArm64(),
          iosSimulatorArm64(),
      )
      .forEach { iosTarget ->
        iosTarget.binaries.framework {
          baseName = "app"
          isStatic = true
          export(projects.sdk.supertokensSdkFrontend)
        }
      }

  jvm()

  compilerOptions {
    freeCompilerArgs.addAll(
        "-P",
        "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.appstractive.util.CommonParcelize",
    )
  }

  sourceSets {
    commonMain.dependencies {
      api(projects.sdk.supertokensSdkFrontend)

      implementation(libs.kotlin.serialization)
      implementation(libs.kotlin.serialization.json)
      implementation(libs.kotlin.coroutines)

      implementation(compose.runtime)
      implementation(compose.foundation)

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
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
  kotlin { jvmToolchain(21) }
}
