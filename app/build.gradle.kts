// build.gradle.kts (Module: app)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
//
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Core ExoPlayer library
    implementation("com.google.android.exoplayer:exoplayer:2.18.0")

    // ExoPlayer DASH support
    implementation("com.google.android.exoplayer:exoplayer-dash:2.18.0")

    // ExoPlayer HLS support (si también necesitas HLS)
    implementation("com.google.android.exoplayer:exoplayer-hls:2.18.0")

    // ExoPlayer SmoothStreaming support (si necesitas SmoothStreaming)
    implementation("com.google.android.exoplayer:exoplayer-smoothstreaming:2.18.0")

    // ExoPlayer UI module (para PlayerView y controles de UI)
    implementation("com.google.android.exoplayer:exoplayer-ui:2.18.0")

    implementation("androidx.appcompat:appcompat:1.6.1") // Asegúrate de que esté presente
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    // Add ConstraintLayout dependency
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Add CardView dependency
    implementation("androidx.cardview:cardview:1.0.0")

    // Add RecyclerView dependency
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Add Glide dependency
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    
    // Add SlidingPaneLayout dependency
    implementation("androidx.slidingpanelayout:slidingpanelayout:1.2.0")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // OkHttp logging interceptor for debugging
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.robolectric:robolectric:4.10.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    //
    implementation("org.videolan.android:libvlc-all:3.5.1")
     implementation("com.android.volley:volley:1.2.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
