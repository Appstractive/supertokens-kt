plugins {
    id("kotlin-platform-jvm")
    kotlin("plugin.serialization")
    `maven-publish`
}

dependencies {
    api(projects.supertokensSdkCommon)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.coroutines)

    implementation(libs.ktor.serialization)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.json)
    implementation(libs.ktor.client.logging)

    implementation(libs.javax.mail)
    api(libs.freemarker)

    implementation(libs.slf4j)
    implementation(libs.jwt)

    testImplementation(libs.test.kotlin)
}

/*tasks.withType<Jar>() {

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(configurations.runtimeClasspath.get()
        .filter {
            it.canonicalPath.contains("common-jvm-.*\\.jar".toRegex())
        }.map {
            zipTree(it)
        })
}*/

publishing {
    publications {
        create<MavenPublication>("backendSDK") {
            groupId = "com.appstractive"
            artifactId = "supertokens-sdk-backend"
            version = "1.0.0"

            setOf("runtimeElements")
                .flatMap { configName -> configurations[configName].hierarchy }
                .forEach { configuration ->
                    configuration.dependencies.removeIf { dependency ->
                        println(dependency.name)
                        dependency.name == "common-jvm"
                    }
                }

            from(components["java"])
        }
    }
}