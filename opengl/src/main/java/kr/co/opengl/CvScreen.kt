package kr.co.opengl

import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

fun NavGraphBuilder.openglNavGraph() {
    composable("opengl") {
        OpenGlScreen()
    }
}

@Composable
private fun OpenGlScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProvider: ProcessCameraProvider = ProcessCameraProvider.getInstance(context).get()

    var filterTextureView by remember { mutableStateOf<FilterTextureView?>(null) }

    LaunchedEffect(Unit) {
        filterTextureView?.let { view ->
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(view::setCameraSurfaceRequest)

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
        }
    }
    CameraScreen { view ->
        filterTextureView = view
    }
}

@Composable
private fun CameraScreen(
    onFilterTextureViewAvailable: (FilterTextureView) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Spacer(Modifier.height(32.dp))

        AndroidView(
            modifier = Modifier.weight(1f),
            factory = { context ->
                FilterTextureView(context).also {
                    onFilterTextureViewAvailable(it)
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(80.dp)
            ) {
                drawCircle(
                    color = Color.White,
                    radius = size.minDimension / 2
                )
            }
        }
    }
}