import com.android.build.api.dsl.ApplicationExtension
import kr.co.build.configureKotlinAndroid
import kr.co.build.groupId
import kr.co.build.targetSdk
import kr.co.build.versionCode
import kr.co.build.versionName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = targetSdk

                namespace = project.groupId

                defaultConfig {
                    applicationId = project.groupId
                    versionCode = project.versionCode
                    versionName = project.versionName

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

                    vectorDrawables {
                        useSupportLibrary = true
                    }
                }
            }
        }
    }
}