// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        jcenter()
        google()
        maven (url = "https://maven.fabric.io/public")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.3.2")
        classpath("com.google.gms:google-services:4.2.0")
        classpath(kotlin("gradle-plugin", version = "1.3.30"))
        classpath("io.fabric.tools:gradle:1.28.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        jcenter()
        maven(url = "https://jitpack.io")
        google()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
