package com.iteo.openglfootball

import android.graphics.Bitmap
import android.opengl.GLES20.*
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

class GestureAwareRenderer(
    private val rectangle: RectangleWithPoint,
    private val backgroundTexture: Bitmap,
    private val foregroundTexture: Bitmap
): GLSurfaceView.Renderer {
    private val minScale = 0.1f
    private val maxScale = 0.5f

    @Volatile
    private var resolution: SurfaceResolution = SurfaceResolution(width = 0.0f, height = 0.0f)

    @Volatile
    private var point: SurfacePoint = SurfacePoint(x = 0.5f, y = 0.5f) // Center

    @Volatile
    private var scale: Float = (minScale + maxScale) / 2

    val dragListener: (SurfacePoint) -> Unit = { surfacePoint -> point = surfacePoint }

    val scaleListener: (Float) -> Unit = { calculateAndApplyScale(it) }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        rectangle.setBackground(backgroundTexture)
        rectangle.setForeground(foregroundTexture)
    }

    override fun onDrawFrame(gl: GL10?) {
        rectangle.draw(resolution, point, scale)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        resolution = SurfaceResolution(width.toFloat(), height.toFloat())
        glViewport(0, 0, width, height)
    }

    private fun calculateAndApplyScale(scaleFactor: Float) {
        scale = when {
            scaleFactor > 1 -> min(maxScale, scale * scaleFactor)
            else -> max(minScale, scale * scaleFactor)
        }
    }
}