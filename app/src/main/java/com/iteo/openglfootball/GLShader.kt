package com.iteo.openglfootball

import java.nio.FloatBuffer

interface GLShader {
    val vertex: String
    val fragment: String

    // Returns reference to the vertex's position
    fun applyCoordinates(
        program: Int,
        coordinatesPerVertex: Int,
        verticesBuffer: FloatBuffer
    ): Int

    // Returns reference to the texture's position
    fun applyBackTexture(
        program: Int,
        texture: Int?,
        coordinatesPerVertex: Int,
        textureBuffer: FloatBuffer
    ): Int? = null

    fun applyFrontTexture(
        program: Int,
        texture: Int?,
        coordinatesPerVertex: Int,
        textureBuffer: FloatBuffer
    ): Int? = null

    fun applyUniforms(program: Int): Unit = Unit
}