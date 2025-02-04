import kr.co.build.App

plugins {
    alias(libs.plugins.compose.camera.ex.application)
    alias(libs.plugins.compose.camera.ex.application.compose)
}

android {
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(projects.opencv)
    implementation(projects.opengl)

    App()
}