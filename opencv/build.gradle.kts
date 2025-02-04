import kr.co.build.setNamespace

plugins {
    alias(libs.plugins.compose.camera.ex.feature)
}

setNamespace("open_cv")

dependencies {
    implementation(libs.opencv)
}