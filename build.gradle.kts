allprojects {
    ext {
        val keyPropertiesFile = rootDir.resolve("key.properties")
        if (keyPropertiesFile.exists()) {
            val keyProperties = java.util.Properties()
            keyProperties.load(keyPropertiesFile.inputStream())

            set("signingKey", rootDir.resolve(keyProperties["keyRingFile"].toString()).readText())
            set("signingPassword", keyProperties["keyPassword"].toString())
            set("mavenUser", keyProperties["ossrhUsername"].toString())
            set("mavenPassword", keyProperties["ossrhPassword"].toString())
        }
        else {
            set("signingKey", System.getenv("GPG_KEY"))
            set("signingPassword", System.getenv("GPG_KEY_PASSWORD"))
            set("mavenUser", System.getenv("MAVEN_USERNAME"))
            set("mavenPassword", System.getenv("MAVEN_PASSWORD"))
        }
    }
}

plugins {
    alias(libs.plugins.android).apply(false)
    alias(libs.plugins.kotlin).apply(false)
    alias(libs.plugins.serialization).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
}
