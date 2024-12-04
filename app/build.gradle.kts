import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    kotlin("kapt")
}

val properties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

android {
    namespace = "com.example.potpytown"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.potpytown"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true

        buildConfigField("String", "URL_WEATHER", "\"${properties["url.weather"]}\"")
        buildConfigField("String", "API_KEY", "\"${properties["api.key"]}\"")
        buildConfigField ("String", "PLACES_API_KEY", "\"${properties["places.api.key"]}\"")
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
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-simplexml:2.11.0")

    // 위치 서비스
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.22"))
    implementation("com.google.android.libraries.places:places:4.1.0")
    implementation ("com.google.android.gms:play-services-maps:19.0.0")

    implementation("com.airbnb.android:lottie:6.6.0")

    // AndroidX 테스트 라이브러리
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")

    implementation ("com.github.bumptech.glide:glide:4.13.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.13.0")
}

configurations.all {
    resolutionStrategy {
        force("androidx.appcompat:appcompat:1.6.1")
        force ("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
        force("com.google.android.material:material:1.9.0")
    }
}