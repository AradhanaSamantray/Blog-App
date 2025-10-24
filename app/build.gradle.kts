plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.blogapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.blogapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout.v220)
    implementation(libs.androidx.cardview)

    // Firebase BoM (controls versions)
    implementation(platform(libs.firebase.bom.v3330))

    // Firebase components
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.firebase.analytics.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth.v2231)
    implementation(libs.firebase.database.v2030)
    // Google Sign-In
    implementation(libs.play.services.auth)

    //Cloudinary Storage
    implementation(libs.cloudinary.android.v1300)
    implementation(libs.cloudinary.cloudinary.android)

    // Glide (image loading)
    implementation(libs.github.glide.v4160)
    implementation(libs.okhttp)
    implementation(libs.okio)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core.v351)
}

