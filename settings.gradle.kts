enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "supertokens_kotlin"

include(":common", ":sdk:backend", ":sdk:frontend", ":server:ktor")
includeBuild("./examples/ktor-server")
includeBuild("./examples/app")

project(":common").name = "supertokens-sdk-common"
