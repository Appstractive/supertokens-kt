allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        mavenLocal()
    }

    ext {
        val keyPropertiesFile = rootDir.resolve("key.properties")
        if (keyPropertiesFile.exists()) {
            val keyProperties = java.util.Properties()
            keyProperties.load(keyPropertiesFile.inputStream())

            set("signingKey", rootDir.resolve(keyProperties["keyRingFile"].toString()).readText())
            set("signingPassword", keyProperties["keyPassword"].toString())
            set("mavenUser", keyProperties["ossrhUsername"].toString())
            set("mavenPassword", keyProperties["ossrhPassword"].toString())
        }
        else {
            set("signingKey", rootDir.resolve(System.getenv("GPG_KEY_FILE")).readText())
            set("signingPassword", System.getenv("GPG_KEY_PASSWORD"))
            set("mavenUser", System.getenv("MAVEN_USERNAME"))
            set("mavenPassword", System.getenv("MAVEN_PASSWORD"))
        }
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
