pluginManagement {
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

rootProject.name = "MarxVersion"
include(":app")
// 🧩 appkit (shared app platform, ../appkit/README.md · D-079): APK self-update module
include(":selfupdate")
project(":selfupdate").projectDir = file("../appkit/core/selfupdate")
