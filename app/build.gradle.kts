plugins {
    alias(libs.plugins.android.application)// Плагин Android приложения
    alias(libs.plugins.kotlin.android) // Плагин Kotlin
    id("com.google.gms.google-services") // Плагин Google Services для Firebase
}

android {
    namespace = "com.example.fooddelivery"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fooddelivery"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation("com.google.firebase:firebase-analytics")
    // Add the dependencies for Firebase Authentication and Cloud Firestore
    implementation("com.google.firebase:firebase-auth")   // Аутентификация
    implementation("com.google.firebase:firebase-firestore") // База данных
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.3")
    implementation(libs.androidx.recyclerview)
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.3")
    // UI
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}