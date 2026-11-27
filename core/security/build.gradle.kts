plugins {
    alias(libs.plugins.smaran.android.library)
    alias(libs.plugins.smaran.android.hilt)
}

android {
    namespace = "com.smaran.core.security"
}

dependencies {
    implementation(libs.androidx.core.ktx)
}
