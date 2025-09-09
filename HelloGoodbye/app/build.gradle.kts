plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.loulblemo.hellogoodbye"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.loulblemo.hellogoodbye"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../../Key.jks")
            storePassword = "Ultreia1988!"
            keyAlias = "Key0"
            keyPassword = "Ultreia1988!"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
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
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/**/baseline.prof"
            excludes += "/**/baseline.profm"
            excludes += "/**/flag-icons/**"
        }
    }
    
    // Compose Compiler plugin handles compiler version in Kotlin 2.0+
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    
    // Compose dependencies
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-geometry")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material:material-ripple")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    // Google Fonts for Compose
    implementation("androidx.compose.ui:ui-text-google-fonts")
    // Google Play services (provides built-in fonts certs resource)
    implementation("com.google.android.gms:play-services-basement:18.3.0")
    
    // Lottie animation
    implementation("com.airbnb.android:lottie-compose:6.1.0")
    
    // Image loading (SVG badges)
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("io.coil-kt:coil-svg:2.6.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}