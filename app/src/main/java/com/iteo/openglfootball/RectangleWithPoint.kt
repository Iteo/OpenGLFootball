package com.iteo.openglfootball

import android.graphics.Bitmap
import android.opengl.GLES20.*
import java.nio.FloatBuffer
import kotlin.math.abs

private const val NO_OFFSET = 0

class RectangleWithPoint(private val shader: GLShader) {

    private val coordinatesPerVertex = 2

    private val vertices = floatArrayOf(
        -1f, -1f, // Lower left
        1f, -1f, // Lower right
        -1f, 1f, // Upper left
        1f, 1f // Upper right
    )

    private val backTextureVertices = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    // Take only coordinate from vertex. Sometimes vertex may have other values.
    private val vertexCount: Int = vertices.size / coordinatesPerVertex

    private val verticesBuffer: FloatBuffer by lazy { GLUtil.getBuffer(vertices) }

    private val backTextureBuffer: FloatBuffer by lazy { GLUtil.getBuffer(backTextureVertices) }

    private val vertexShader by lazy {
        GLUtil.loadShader(ShaderType.VERTEX, shader.vertex)
    }

    private val fragmentShader by lazy {
        GLUtil.loadShader(ShaderType.FRAGMENT, shader.fragment)
    }

    private val program: Int by lazy {
        GLUtil.initProgram(vertexShader, fragmentShader)
    }

    private var backTexture: Int? = null
    private var frontTexture: Int? = null

    fun setBackground(texture: Bitmap) {
        backTexture = GLUtil.generateTexture(texture, BACKGROUND_TEXTURE_POSITION, TextureConfig.FULLSCREEN)
    }

    fun setForeground(texture: Bitmap) {
        frontTexture = GLUtil.generateTexture(texture, FOREGROUND_TEXTURE_POSITION, TextureConfig.ORIGINAL)
    }

    fun draw(resolution: SurfaceResolution, point: SurfacePoint, scale: Float) {
        var backTextureHandle: Int? = null
        var frontTextureHandle: Int? = null
        // Add program to OpenGL ES environment
        glUseProgram(program)
        // Get pointer to the vertices
        val positionHandle = shader.applyCoordinates(program, coordinatesPerVertex, verticesBuffer)

        backTexture?.let {
            backTextureHandle =
                shader.applyBackTexture(program, it, coordinatesPerVertex, backTextureBuffer)
        }

        val frontVerticesBuffer = getCalculatedVertices(point, scale)
        frontTexture?.let {
            frontTextureHandle =
                shader.applyFrontTexture(program, it, coordinatesPerVertex, frontVerticesBuffer)
        }

        // Pass a value to the shader
        GLUtil.applyValues(program, "resolution", floatArrayOf(resolution.width, resolution.height))
        GLUtil.applyValues(program, "touchPosition", floatArrayOf(point.x, point.y))
        GLUtil.applyValues(program, "radius", floatArrayOf(scale))

        shader.applyUniforms(program)

        // Draw the triangles -> glDrawArrays(MODE. OFFSET, COUNT)
        glDrawArrays(GL_TRIANGLE_STRIP, NO_OFFSET, vertexCount)
        // cleanup
        glDisableVertexAttribArray(positionHandle)
        backTextureHandle?.let { glDisableVertexAttribArray(it) }
        frontTextureHandle?.let { glDisableVertexAttribArray(it) }
    }

    private fun getCalculatedVertices(point: SurfacePoint, scale: Float): FloatBuffer {
        val x = maxOf(0f, point.x)
        val y = maxOf(0f, point.y)

        // 0.6 = minScale + maxScale from Renderer
        val diff = 0.6f - scale

        return GLUtil.getBuffer(
            floatArrayOf(
                x - diff, y + diff,
                x + diff, y + diff,
                x - diff, y - diff,
                x + diff, y - diff
            )
        )
    }
}