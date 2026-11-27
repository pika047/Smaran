import com.android.build.gradle.LibraryExtension
import com.smaran.buildlogic.configureKotlinAndroid
import com.smaran.buildlogic.lib
import com.smaran.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
        }

        dependencies {
            add("testImplementation", libs.lib("junit"))
        }
    }
}
