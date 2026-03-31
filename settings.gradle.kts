pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolution {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "VoxAble"

include(":app")

// Core modules
include(":core")
include(":core-ui")
include(":core-accessibility")
include(":core-network")
include(":core-database")

// Feature modules
include(":feature-auth")
include(":feature-home")
include(":feature-reader")
include(":feature-media")
include(":feature-ocr")
include(":feature-currency")
include(":feature-converter")
include(":feature-downloader")
include(":feature-settings")
