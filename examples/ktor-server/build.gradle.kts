plugins {
    id("kotlin-platform-jvm")
    application
    id("io.ktor.plugin") version libs.versions.ktor.version
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.appstractive"

dependencies {
    implementation(projects.server.ktor)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)

    implementation(libs.ktor.serialization)

    implementation(libs.ktor.server.cio)

    implementation(libs.slf4j)

    testImplementation(libs.test.kotlin)
}

application {
    mainClass.set("io.supertokens.ApplicationKt")
}