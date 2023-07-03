plugins {
    id("kotlin-platform-jvm")
    application
    id("io.ktor.plugin") version libs.versions.ktor.version
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.appstractive"

dependencies {
    implementation(projects.sdk.backend)
    implementation(projects.server.ktor)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)

    implementation(libs.ktor.serialization)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.contentnegotiation)
    implementation(libs.ktor.server.statuspages)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.slf4j)

    testImplementation(libs.test.kotlin)
}

application {
    mainClass.set("io.supertokens.ApplicationKt")
}