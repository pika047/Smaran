plugins {
    alias(libs.plugins.smaran.android.library)
    alias(libs.plugins.smaran.android.hilt)
    alias(libs.plugins.smaran.android.room)
}

android {
    namespace = "com.smaran.core.data"
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:security"))
    implementation(libs.sqlcipher.android)
    implementation(libs.androidx.sqlite.ktx)
}
