package kr.co.opengl

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.TextureView
import androidx.camera.core.SurfaceRequest

internal class FilterTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextureView(context, attrs), TextureView.SurfaceTextureListener {

    private lateinit var renderThread: GLRenderThread
    private var cameraSurfaceRequest: SurfaceRequest? = null

    init {
        surfaceTextureListener = this
    }

    fun setCameraSurfaceRequest(request: SurfaceRequest) {
        cameraSurfaceRequest = request
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        cameraSurfaceRequest?.let { request ->
            val cameraSurface = android.view.Surface(surface)
            request.provideSurface(
                cameraSurface,
                androidx.core.content.ContextCompat.getMainExecutor(context),
                { result ->
                    if (result.resultCode != SurfaceRequest.Result.RESULT_SURFACE_USED_SUCCESSFULLY) {

                    }
                }
            )
        }
        renderThread = GLRenderThread(surface, width, height, context)
        renderThread.start()

    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        renderThread.onSurfaceChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        renderThread.requestExitAndWait()
        return true
    }


    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) { }
}