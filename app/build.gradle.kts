plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)

    // Aici doar activăm plugin-ul (versiunea e luată din fișierul de mai sus)
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.sharoma_finder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sharoma_finder"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // --- AICI SUNT MODIFICĂRILE PENTRU APP SIZE ---
            // Activează R8 pentru a micșora și obfusca codul
            isMinifyEnabled = true
            // Activează ștergerea resurselor (imagini, layout-uri) nefolosite
            isShrinkResources = true

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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // --- FIREBASE SETUP ---
    implementation(libs.firebase.database)

    // Firebase BoM (gestionează versiunile librăriilor de mai jos)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Librăriile Crashlytics și Analytics (fără versiune, o iau din BoM)
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics")
    // ---------------------

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Alte librării
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.36.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("com.google.maps.android:maps-compose:6.12.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("com.google.android.gms:play-services-location:21.0.1")
}