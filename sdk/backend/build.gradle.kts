import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm")
    id("io.ktor.plugin") version libs.versions.ktor.version
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.supertokens.sdk"

dependencies {
    implementation(projects.common)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)

    implementation(libs.ktor.serialization)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)

    implementation(libs.slf4j)
    implementation(libs.jwt)

    testImplementation(libs.test.kotlin)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}