import com.smaran.buildlogic.lib
import com.smaran.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Everything a feature (screen group) module needs. A new feature is one module
 * with `plugins { alias(libs.plugins.smaran.android.feature) }` and a namespace.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("smaran.android.library")
        pluginManager.apply("smaran.android.compose")
        pluginManager.apply("smaran.android.hilt")

        dependencies {
            add("implementation", project(":core:model"))
            add("implementation", project(":core:domain"))
            add("implementation", project(":core:ui"))
            add("implementation", libs.lib("hilt-navigation-compose"))
            add("implementation", libs.lib("androidx-navigation-compose"))
            add("implementation", libs.lib("androidx-lifecycle-runtime-compose"))
            add("implementation", libs.lib("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.lib("androidx-activity-compose"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
