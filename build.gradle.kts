// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}

buildscript {
    repositories {
        // Check that you have the following line (if not, add it):

        google()  // Google's Maven repository
    }
    dependencies {


        classpath("com.google.gms:google-services:4.4.1")

    }
}