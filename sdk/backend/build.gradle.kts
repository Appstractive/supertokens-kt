plugins {
    id("kotlin-platform-jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    signing
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
        create<MavenPublication>("SupertokensSdkBackend") {
            groupId = "com.appstractive"
            artifactId = "supertokens-sdk-backend"
            version = "1.0.0"

            from(components["java"])

            pom {
                name.set("SuperTokens-SDK-Backend")
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