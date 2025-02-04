import kr.co.build.configureAndroidCompose
import kr.co.build.libraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            configureAndroidCompose(libraryExtension)
        }
    }
}