package com.iteo.openglfootball

enum class ShaderType { VERTEX, FRAGMENT }

enum class TextureConfig {
    FULLSCREEN, ORIGINAL
}

data class SurfacePoint(val x: Float, val y: Float)

data class SurfaceResolution(val width: Float, val height: Float)