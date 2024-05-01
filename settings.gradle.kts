enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "supertokens-kt"

include(":common", ":sdk:backend", ":sdk:frontend", ":server:ktor", ":examples:ktor-server")
//includeBuild("./examples/app")

project(":common").name = "supertokens-sdk-common"
