pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Smaran"

include(":app")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:security")
include(":core:ui")
include(":feature:today")
include(":feature:notes")
