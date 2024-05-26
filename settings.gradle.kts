enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "supertokens-kt"

include(":common", ":sdk:backend", ":sdk:frontend", ":server:ktor", ":examples:ktor-server", ":examples:composeApp")

project(":common").name = "supertokens-sdk-common"
