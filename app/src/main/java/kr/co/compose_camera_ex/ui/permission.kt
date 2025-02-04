package kr.co.compose_camera_ex.ui

import android.Manifest
import android.app.AppOpsManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat

internal fun ComponentActivity.checkCameraPermission(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        checkCameraPermission30()
    } else {
        checkCameraPermission19()
    }

@RequiresApi(30)
private fun ComponentActivity.checkCameraPermission30(): Boolean {
    val appOps = getSystemService(AppOpsManager::class.java)
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_CAMERA,
        android.os.Process.myUid(),
        packageName
    )

    return mode == AppOpsManager.MODE_ALLOWED
}

private fun ComponentActivity.checkCameraPermission19(): Boolean {
    val status =
        checkSelfPermission(Manifest.permission.CAMERA)

    return status == PackageManager.PERMISSION_GRANTED
}

internal fun ComponentActivity.requestCameraPermission() {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.CAMERA),
        CAMERA_PERMISSION_REQUEST_CODE
    )
}

private const val CAMERA_PERMISSION_REQUEST_CODE = 100