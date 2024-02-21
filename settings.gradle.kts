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
        //maven { url = uri("https://jitpack.io") } possible alteration
        maven ("https://jitpack.io")
    }
}

rootProject.name = "SwimTrackML"
include(":app")
include(":wear")
