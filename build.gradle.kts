buildscript {
    dependencies {
        classpath(libs.google.services)
//        classpath("com.google.dagger:hilt-android-gradle-plugin:2.33-beta")

    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.hiltAndroid) apply false
}