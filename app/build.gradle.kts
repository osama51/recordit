plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("kotlin-kapt")
//    id("dagger.hilt.android.plugin")

    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.toddler.recordit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.toddler.recordit"
        minSdk = 23
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {


//    implementation(libs.glide)
//    implementation(libs.glide.compose)
//    annotationProcessor(libs.glide.compiler)
//    implementation(libs.landscapist.glide)
    implementation(libs.landscapist.coil)
    implementation(libs.landscapist.placeholder)
    implementation(libs.landscapist.animation)
    implementation(libs.landscapist.transformation)
    implementation(libs.androidx.hilt)
//    kapt("androidx.hilt:hilt-compiler:1.0.0-alpha01")
//    implementation(libs.dagger.hilt.android)
//    implementation(libs.hilt.compiler)


    implementation("com.google.dagger:hilt-android:2.48")

//    kapt("com.google.dagger:hilt-android-compiler:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Add the dependencies for Google Play services' authentication libraries
    implementation(libs.play.services.auth)
    implementation("androidx.compose.ui:ui-text-google-fonts:1.5.4")

    implementation("com.google.code.gson:gson:2.8.8")

    implementation ("com.google.accompanist:accompanist-permissions:0.33.2-alpha")


//    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.0")
//    implementation("androidx.credentials:credentials:1.3.0-alpha01")
//    // optional - needed for credentials support from play services, for devices running
//    // Android 13 and below.
//    implementation("androidx.credentials:credentials-play-services-auth:1.3.0-alpha01")

    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Allow references to generated code
kapt {
//    correctErrorTypes = true
}