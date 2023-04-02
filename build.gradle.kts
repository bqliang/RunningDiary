// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val androidGradlePluginVersion = "7.4.2"
    val kotlinVersion = "1.8.10"
    id("com.android.application") version androidGradlePluginVersion apply false
    id("com.android.library") version androidGradlePluginVersion apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("com.google.dagger.hilt.android") version "2.45" apply false
    id("androidx.navigation.safeargs") version "2.5.3" apply false
}


//fun ProcessBuilder.execute() = this.start().inputStream.bufferedReader().readText().trim()
//
//extra.apply {
//    val gitCommitId = ProcessBuilder("git", "rev-parse", "--short", "HEAD").execute()
//    val gitCommitCount = ProcessBuilder("git", "rev-list", "--count", "HEAD").execute().toInt()
//    val gitTag = ProcessBuilder("git", "describe", "--tags", "--abbrev=0").execute().let { tag ->
//        if (tag.isEmpty()) "" else "$tag."
//    }
//    set("appVersionCode", gitCommitCount)
//    set("appVersionName", "${gitTag}r${gitCommitCount}.${gitCommitId}")
//}