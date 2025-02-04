package kr.co.opencv

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.opencvNavGraph() {
    composable("opencv") {
        OpenCvScreen()
    }
}

@Composable
private fun OpenCvScreen() {
}
