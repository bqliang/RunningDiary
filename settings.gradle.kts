pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        //增加 jitPack Maven 仓库
        maven(url = "https://jitpack.io")
    }
}
rootProject.name = "Running Diary"
include(":app")
