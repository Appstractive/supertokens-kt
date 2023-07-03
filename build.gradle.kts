allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.androidGradle)

        classpath(libs.kotlinGradle)

        classpath((kotlin("serialization", version = libs.versions.kotlin.serialization.get())))
    }
}
