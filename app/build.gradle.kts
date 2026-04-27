plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.firebase.perfomance)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
    alias(libs.plugins.secrets)
}

android {
    namespace = "com.loki.deni"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.loki.deni"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["ADMOB_APP_ID"] =
            (project.findProperty("ADMOB_APP_ID") as String?)
                ?: "ca-app-pub-3940256099942544~3347511713"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.bundles.compose.debug)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.bundles.test.common)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.splash.screen)
    implementation(libs.timber)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.coroutines)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("net.zetetic:sqlcipher-android:4.10.0")
    implementation("androidx.sqlite:sqlite-ktx:2.5.1")
    implementation("androidx.work:work-runtime-ktx:2.10.1")
    implementation("androidx.compose.material:material")
    implementation(libs.pager)
    implementation(libs.palette)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.bundles.retrofit)
    implementation(libs.form.builder)
    implementation(libs.lottie)
    implementation(libs.lottie.compose)

    implementation(libs.bundles.hilt)
    ksp(libs.bundles.hilt.ksp)

    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")
    implementation("com.google.zxing:core:3.5.3")
}
