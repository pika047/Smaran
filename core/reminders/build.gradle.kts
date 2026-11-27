plugins {
    alias(libs.plugins.smaran.android.library)
    alias(libs.plugins.smaran.android.hilt)
}

android {
    namespace = "com.smaran.core.reminders"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(libs.androidx.core.ktx)
}
