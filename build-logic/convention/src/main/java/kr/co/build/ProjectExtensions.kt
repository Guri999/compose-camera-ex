package kr.co.build

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the


internal val Project.libs get() = the<LibrariesForLibs>()

fun Project.App() {
    project.dependencies {
        implementations(
            libs.androidx.activity.compose,
            libs.androidx.lifecycle.runtime.ktx,
            libs.androidx.core.ktx
        )

        testImplementations(
            libs.junit
        )

        androidTestImplementations(
            libs.androidx.junit,
            libs.androidx.espresso.core
        )
    }
}
