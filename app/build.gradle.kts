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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.play.services.mlkit.text.recognition)
    implementation(libs.identity.doctypes.jvm)
    implementation(libs.firebase.inappmessaging.display)
    testImplementation(libs.junit)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth.v2101)
    implementation(libs.firebase.database.v2030)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.auth)
    androidTestImplementation(libs.ext.junit)
    implementation("com.google.firebase:firebase-auth:23.0.0")

    androidTestImplementation(libs.espresso.core)
}
