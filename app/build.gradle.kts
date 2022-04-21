plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
}
android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.rocky.h264encode_2022_40_19"
        minSdk = 24
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    implementation("androidx.camera:camera-lifecycle:1.1.0-beta01")
    implementation("androidx.camera:camera-view:1.1.0-beta01")
    val kotlin_version = "1.6.20"
    implementation("androidx.appcompat:appcompat:1.3.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlin_version")
    implementation("com.github.getActivity:XXPermissions:13.5")


    implementation("androidx.camera:camera-lifecycle:1.1.0-beta01")
    implementation("androidx.camera:camera-view:1.1.0-beta01")
    implementation("androidx.camera:camera-camera2:1.1.0-beta01")

//    val camerax_version = "1.1.0-beta01"
//    implementation("androidx.camera:camera-core:${camerax_version}")
//    implementation("androidx.camera:camera-camera2:${camerax_version}")
//    implementation("androidx.camera:camera-lifecycle:${camerax_version}")
//    implementation("androidx.camera:camera-video:${camerax_version}")
//    implementation("androidx.camera:camera-view:${camerax_version}")
//    implementation("androidx.camera:camera-extensions:${camerax_version}")
}
