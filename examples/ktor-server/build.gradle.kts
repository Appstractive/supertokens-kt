plugins {
    id("kotlin-platform-jvm")
    application
    id("io.ktor.plugin") version libs.versions.ktor.version
    kotlin("plugin.serialization")
}

version = "1.0.0"
group = "com.appstractive"

dependencies {
    implementation("com.appstractive:supertokens-sdk-backend-ktor:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.1")
    implementation("io.ktor:ktor-server-cio-jvm:2.3.1")

    implementation("org.slf4j:slf4j-simple:1.6.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.22")
}

application {
    mainClass.set("io.supertokens.ApplicationKt")
}