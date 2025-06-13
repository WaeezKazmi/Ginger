plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") // Add Compose plugin
    id  ("kotlin-kapt")
    id("com.google.gms.google-services")
}
kapt {
    correctErrorTypes = true
}

android {
    namespace = "com.example.collegealert"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.collegealert"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0" // Match Kotlin version
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.ui:ui-graphics:1.7.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
    implementation("androidx.compose.material3:material3:1.3.1") // Use valid version
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation(libs.firebase.database)

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.4")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.4")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation ("androidx.compose.material3:material3-window-size-class:1.2.0")
    implementation ("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-database-ktx")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // For image loading
    implementation("io.coil-kt:coil-compose:2.4.0")

// For Firebase Storage (for image uploads)
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation ("androidx.core:core:1.12.0") // For NotificationCompat


// For image picker
    implementation("androidx.activity:activity-compose:1.8.0")
}