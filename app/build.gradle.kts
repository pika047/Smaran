plugins {
    alias(libs.plugins.smaran.android.application)
    alias(libs.plugins.smaran.android.compose)
    alias(libs.plugins.smaran.android.hilt)
}

android {
    namespace = "com.smaran.app"

    defaultConfig {
        applicationId = "com.smaran.app"
        versionCode = 1
        versionName = "0.1.0"
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
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:security"))
    implementation(project(":core:reminders"))
    implementation(project(":core:speech"))
    implementation(project(":feature:today"))
    implementation(project(":feature:notes"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
}
