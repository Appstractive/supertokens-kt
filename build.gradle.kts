allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
    }
    dependencies {
        classpath(libs.androidGradle)

        classpath(libs.kotlinGradle)

        classpath((kotlin("serialization", version = libs.versions.kotlin.serialization.get())))
    }
}
