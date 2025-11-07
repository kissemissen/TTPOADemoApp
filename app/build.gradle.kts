import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.havrebollsolutions.ttpoademoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.havrebollsolutions.ttpoademoapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val secretsFile = rootProject.file("secrets.properties")
        if (secretsFile.exists()) {
            val secrets = Properties()
            secretsFile.inputStream().use { secrets.load(it) }
            buildConfigField("String", "TERMINAL_SERVICE_API_KEY", secrets.getProperty("TERMINAL_SERVICE_API_KEY"))
            buildConfigField("String", "TERMINAL_SERVICE_CONFIG_ID", secrets.getProperty("TERMINAL_SERVICE_CONFIG_ID"))
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "11"
//    }
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // Refer to the dependencies using libs.
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.androidx.navigation.compose)
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.hilt.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Datastore
    implementation(libs.androidx.datastore.preferences)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Activity
    implementation(libs.androidx.activity.compose)

    // Startup
    implementation(libs.androidx.startup)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Networking
    implementation(libs.retrofit2)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.converter)
    implementation(libs.kotlinx.serialization.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.compose.network)
    implementation(libs.coil.compose.gif)



    // Gson
    implementation(libs.gson)

    // Adyen
    debugImplementation(libs.adyen.ipp)
    debugImplementation(libs.adyen.ipp.taptopay)
    debugImplementation(libs.adyen.ipp.cardreader)

    // QR code reader
    implementation(libs.quickie.bundled)


    // Unit Tests
    testImplementation(libs.junit)

    // Android Instrumentation Tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom)) // BOM must be a platform dependency
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug-only dependencies
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}