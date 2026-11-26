plugins {
    alias(libs.plugins.smaran.android.library)
    alias(libs.plugins.smaran.android.compose)
}

android {
    namespace = "com.smaran.core.ui"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.core)
}
