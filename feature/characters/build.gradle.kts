plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("dagger.hilt.android.plugin")

}

android {
    namespace = "com.example.characters"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(libs.androidx.core.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.3")

    implementation(libs.hilt.android)
    implementation(libs.androidx.paging.runtime.ktx)
    kapt(libs.hilt.compiler)

    // SwipeRefreshLayout
    implementation(libs.androidx.swiperefreshlayout)


    // Material
    implementation("com.google.android.material:material:1.9.0")

    implementation(libs.glide)

    implementation("androidx.hilt:hilt-navigation-fragment:1.1.0")

}