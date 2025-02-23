plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.android")
}

android {

  buildFeatures {
    viewBinding = true
  }

  packagingOptions {
    exclude("META-INF/ASL-2.0.txt")
    exclude("META-INF/LGPL-3.0.txt")
  }

  namespace = "com.google.android.fhir.library"
  compileSdk = 33

  defaultConfig {
    minSdk = 24

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = "1.8"
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.10.1")
  implementation("com.google.zxing:core:3.4.1")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.9.0")
  implementation("com.google.android.gms:play-services-vision-common:19.1.3")
  implementation("com.google.android.gms:play-services-vision:20.1.3")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  testImplementation("org.robolectric:robolectric:4.7.3")
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
  implementation("com.jakewharton.timber:timber:5.0.1")
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")
  implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
  implementation("com.squareup.okhttp3:okhttp:4.11.0")

  implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.8")
  implementation("com.nimbusds:nimbus-jose-jwt:9.31")
  // implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-model:1.0.0")

  implementation("com.google.android.fhir:engine:0.1.0-beta03")
}