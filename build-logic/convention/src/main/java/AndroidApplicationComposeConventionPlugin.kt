import kr.co.build.configureAndroidCompose
import kr.co.build.applicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidApplicationComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            configureAndroidCompose(applicationExtension)
        }
    }
}