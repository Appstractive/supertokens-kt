import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-platform-jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    signing
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

publishing {
    repositories {
        maven {
            name="oss"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = extra["mavenUser"].toString()
                password = extra["mavenPassword"].toString()
            }
        }
    }

    publications {
        create<MavenPublication>("SupertokensSdkBackendKtor") {
            groupId = "com.appstractive"
            artifactId = "supertokens-sdk-backend-ktor"
            version = "1.0.0"

            from(components["java"])

            pom {
                name.set("SuperTokens-SDK-Backend-Ktor")
                description.set("SuperTokens backend SDK")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/Appstractive/supertokens-kotlin/issues")
                }
                scm {
                    connection.set("https://github.com/Appstractive/supertokens-kotlin.git")
                    url.set("https://github.com/Appstractive/supertokens-kotlin")
                }
                developers {
                    developer {
                        name.set("Andreas Schulz")
                        email.set("dev@appstractive.com")
                    }
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        extra["signingKey"].toString(),
        extra["signingPassword"].toString(),
    )
    sign(publishing.publications)
}