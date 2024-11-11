import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-android")
    //id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") // 추가된 플러그인
    kotlin("kapt") // Kapt 플러그인 추가
}

// local.properties 사용을 위함
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

        // url 변수 지정
        buildConfigField("String", "URL_WEATHER", "\"${properties["url.weather"]}\"")

        // api key 변수 지정
        buildConfigField("String", "API_KEY", "\"${properties["api.key"]}\"")

        // kakao map rest api key 변수 지정
        //buildConfigField("String", "KAKAO_MAP_REST_API_KEY", properties["kakao.map.rest.api.key"] as String)
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
        freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    // retrofit2
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-simplexml:2.11.0") // 추가된 XML 파싱

    // 위치
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.code.gson:gson:2.11.0")

    //애니메이션 아이콘
    implementation ("com.airbnb.android:lottie:6.6.0")
}
