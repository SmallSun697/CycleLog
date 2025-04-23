plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)

  id("com.google.devtools.ksp")
}

android {
  namespace = "com.example.cyclelog"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.example.cyclelog"
    minSdk = 33
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
      pickFirsts.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
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
  implementation(libs.androidx.room.common)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.material3.adaptive.navigation.suite)
  implementation(libs.androidx.datastore.core.android)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.livedata.ktx)
  implementation(libs.play.services.location)
  implementation(libs.android)
  implementation(libs.maps.compose)
  implementation(libs.mapbox.sdk.turf)
  implementation(libs.vico.compose)
  implementation(libs.vico.compose.m3)
  implementation(libs.identity.jvm)
  testImplementation(libs.junit.junit)
  ksp(libs.androidx.room.compiler)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}