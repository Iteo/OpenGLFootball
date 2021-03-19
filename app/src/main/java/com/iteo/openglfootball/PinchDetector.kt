package com.iteo.openglfootball

import android.view.ScaleGestureDetector

class PinchDetector(
    private val scaleListener: (Float) -> Unit
): ScaleGestureDetector.SimpleOnScaleGestureListener() {
    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector?.let { scaleListener(it.scaleFactor) }
        return true
    }
}