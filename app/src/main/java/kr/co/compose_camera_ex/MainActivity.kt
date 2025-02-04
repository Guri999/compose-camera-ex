package kr.co.compose_camera_ex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.compose_camera_ex.ui.checkCameraPermission
import kr.co.compose_camera_ex.ui.theme.ComposecameraexTheme
import kr.co.opencv.opencvNavGraph
import kr.co.opengl.openglNavGraph

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!checkCameraPermission()) {
            requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGrant: Boolean ->
                if (isGrant) {
                    setContent {
                        ComposecameraexTheme {
                            Main()
                        }
                    }
                } else {
                    finish()
                }
            }
        } else {
            setContent {
                ComposecameraexTheme {
                    Main()
                }
            }
        }
    }
}

@Composable
fun Main() {
    val navController = rememberNavController()
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = "home"
            ) {
                composable("home") {
                    Home(
                        navigateToOpenGl = {
                            navController.navigate("opengl")
                        }
                    )
                }

                opencvNavGraph()
                openglNavGraph()
            }
        }
    }
}

@Composable
fun Home(
    navigateToOpenGl: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = navigateToOpenGl
            ) {
                Text("open GL")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ComposecameraexTheme {
        Home()
    }
}