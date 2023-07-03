enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "supertokens_kotlin"

include(":common", ":sdk:backend", ":sdk:frontend", ":server:ktor", ":examples:ktor-server")

project(":common").name = "supertokens-sdk-common"
