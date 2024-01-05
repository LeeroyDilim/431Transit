import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.a431transit"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.a431transit"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())

        buildConfigField("String", "TRANSIT_API_KEY", "\"${properties.getProperty("TRANSIT_API_KEY")}\"")
        buildConfigField("String", "GOOGLE_API_KEY", "\"${properties.getProperty("GOOGLE_API_KEY")}\"")
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

secrets {
    // Optionally specify a different file name containing your secrets.
    // The plugin defaults to "local.properties"
    propertiesFileName = "secrets.properties"

    // A properties file containing default secret values. This file can be
    // checked in version control.
    defaultPropertiesFileName = "local.properties"
}


dependencies {
    implementation ("com.google.android.gms:play-services-location:21.0.1");
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}