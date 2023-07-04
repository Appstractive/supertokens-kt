import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm")
    id("io.ktor.plugin") version libs.versions.ktor.version
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.supertokens.backend"

dependencies {
    api(projects.sdk.backend)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)

    implementation(libs.ktor.serialization)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)

    api(libs.ktor.server.core)
    api(libs.ktor.server.contentnegotiation)
    api(libs.ktor.server.statuspages)
    api(libs.ktor.server.auth)
    api(libs.ktor.server.auth.jwt)

    implementation(libs.slf4j)
    implementation(libs.jwt)

    testImplementation(libs.test.kotlin)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}