plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)

}

android {
    namespace = "com.example.scannerallerpro"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.scannerallerpro"
        minSdk = 27
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/versions/9/OSGI-INF/MANIFEST.MF") // Exclude the conflicting file
    }
}
dependencies {
    // Android libraries
    implementation(libs.appcompat)  // AndroidX AppCompat
    implementation(libs.material)   // Material Design Components
    implementation(libs.activity)   // AndroidX Activity
    implementation(libs.constraintlayout)  // ConstraintLayout

    // Firebase BOM (manages all Firebase dependencies version)
    implementation(platform(libs.firebase.bom))  // Firebase BOM

    // Firebase dependencies
    implementation(libs.com.google.firebase.firebase.auth)  // Firebase Auth
    implementation(libs.google.firebase.database)           // Firebase Realtime Database
    implementation(libs.google.firebase.functions)          // Firebase Cloud Functions
    implementation(libs.google.firebase.analytics)          // Firebase Analytics
    implementation ("org.apache.commons:commons-text:1.9")


    // ML Kit and Play Services
    implementation(libs.play.services.mlkit.text.recognition)  // ML Kit Text Recognition
    implementation(libs.identity.doctypes.jvm)                 // Identity Document Types
    implementation(libs.play.services.auth)                    // Play Services Auth

    // Testing libraries
    testImplementation(libs.junit)  // JUnit for unit testing
    androidTestImplementation(libs.ext.junit)  // AndroidX JUnit for instrumentation tests
    androidTestImplementation(libs.espresso.core)  // Espresso for UI tests
}
