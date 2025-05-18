plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false // Add Compose plugin
}

allprojects {
    repositories {
        google()
        mavenCentral()
        // Add Compose-specific repository for latest artifacts
        maven {
            url = uri("https://androidx.dev/storage/compose-compiler/repository")
        }
    }
}
