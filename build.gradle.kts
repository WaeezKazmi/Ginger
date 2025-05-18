buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
    }
}


plugins {
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("org.jetbrains.kotlin.jvm") version "1.9.23" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
}
