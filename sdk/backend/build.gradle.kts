import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm")
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.supertokens.sdk"

dependencies {

    
    implementation(projects.common)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}