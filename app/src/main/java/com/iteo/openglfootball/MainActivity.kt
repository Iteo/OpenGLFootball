package com.iteo.openglfootball

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import kotlinx.android.synthetic.main.activity_main.*

private const val GL_ES2 = 2
private const val CONSUMED_EVENT = true

class MainActivity : AppCompatActivity() {

    private val fieldBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.field)
    }

    private val ballBitmap by lazy {
        BitmapFactory.decodeResource(resources, R.drawable.ball)
    }

    private val renderer by lazy {
        GestureAwareRenderer(
            rectangle = RectangleWithPoint(CircleShader()),
            backgroundTexture = fieldBitmap,
            foregroundTexture = ballBitmap
        )
    }

    private val pinchDetector = PinchDetector { scale -> renderer.scaleListener(scale) }
    private lateinit var scaleDetector: ScaleGestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scaleDetector = ScaleGestureDetector(this, pinchDetector)

        with(surface) {
            setEGLContextClientVersion(GL_ES2)
            setRenderer(renderer)
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN)
                    performClick()

                renderer.dragListener(getPoint(event))
                scaleDetector.onTouchEvent(event)

                return@setOnTouchListener CONSUMED_EVENT
            }
        }
    }

    private fun getPoint(event: MotionEvent) = SurfacePoint(
        x = event.x / surface.width,
        y = 1 - (event.y / surface.height) // Android y is opposite of OpenGL y
    )
}