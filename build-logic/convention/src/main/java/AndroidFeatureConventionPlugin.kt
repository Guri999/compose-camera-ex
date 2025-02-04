import kr.co.build.implementations
import kr.co.build.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("compose.camera.ex.library")
                apply("compose.camera.ex.library.compose")
            }

            dependencies {
                implementations(
                    libs.bundles.camera
                )
            }
        }
    }
}