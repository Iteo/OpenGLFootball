package com.iteo.openglfootball

import android.graphics.Bitmap
import android.opengl.GLES20.*
import android.opengl.GLUtils
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

private const val FLOAT_SIZE = 4
private const val MIPMAPPING_LEVEL = 0 // For default there is no mipmapping strategy
private const val TEXTURE_COUNT = 2 // Extend it to place more than 2 textures
const val BACKGROUND_TEXTURE_POSITION = 0
const val FOREGROUND_TEXTURE_POSITION = 1

object GLUtil {
    private val textureHandler = IntArray(TEXTURE_COUNT)

    fun getBuffer(vertices: FloatArray): FloatBuffer =
        ByteBuffer.allocateDirect(vertices.size * FLOAT_SIZE).run {
            // use the device hardware's native byte order
            order(ByteOrder.nativeOrder())
            // create a floating point buffer from the ByteBuffer
            asFloatBuffer().apply {
                // add the coordinates to the FloatBuffer
                put(vertices)
                // set the buffer to read the first coordinate
                position(0)
            }
        }

    fun loadShader(type: ShaderType, shaderCode: String): Int {
        val shaderType = when (type) {
            ShaderType.VERTEX -> GL_VERTEX_SHADER
            ShaderType.FRAGMENT -> GL_FRAGMENT_SHADER
        }

        // Create shader instance and return it's id
        return glCreateShader(shaderType).also { shader ->
            // Select shader code
            glShaderSource(shader, shaderCode)
            // Compile shader
            glCompileShader(shader)
        }
    }

    fun initProgram(vertexShader: Int, fragmentShader: Int) =
        // Create program and return it's id
        glCreateProgram().also { program ->
            // add the vertex shader to program
            glAttachShader(program, vertexShader)
            // add the fragment shader to program
            glAttachShader(program, fragmentShader)
            // Finally tell OpenGL which program to run
            glLinkProgram(program)
            // Clean up
            glDeleteShader(vertexShader)
            glDeleteShader(fragmentShader)
        }

    fun applyValues(program: Int, variableName: String, values: FloatArray) {
        glGetUniformLocation(program, variableName)
            .also { variablePointer ->
                when (values.size) {
                    1 -> glUniform1f(variablePointer, values.first())
                    2 -> glUniform2fv(variablePointer, 1, values, 0) // (x, y)
                    3 -> glUniform3fv(variablePointer, 1, values, 0) // (x, y, z)
                    4 -> glUniform4fv(variablePointer, 1, values, 0) // (r, g, b, a)
                    else -> throw IllegalArgumentException("Unknown method for values number = ${values.size}")
                }
            }
    }

    fun applyInt(program: Int, variableName: String, value: Int) {
        glGetUniformLocation(program, variableName)
            .also { variablePointer -> glUniform1i(variablePointer, value) }
    }

    fun applyCoordinates(
        program: Int,
        variableName: String,
        coordinatesPerVertex: Int,
        vertexBuffer: FloatBuffer
    ): Int {
        val vertexStride: Int = coordinatesPerVertex * FLOAT_SIZE // 4 bytes per vertex
        // get handle to vertex shader's vPosition member
        glGetAttribLocation(program, variableName).also { positionHandle ->
            // Enable a handle to the triangle vertices
            glEnableVertexAttribArray(positionHandle)
            // Prepare the triangle coordinate data
            glVertexAttribPointer(
                positionHandle, // Vertex attribute to modify ^'vPosition'
                coordinatesPerVertex, // Number of components in this triangle (x, y, z)
                GL_FLOAT, // Data type of the coordinates
                false, // Float is already normalized
                vertexStride, // Amount of bytes between each vertex. Size of vertex
                vertexBuffer // Pointer to the data that we want to access. Here pointer to vertices
            )

            return positionHandle
        }
    }



    fun generateTexture(bitmap: Bitmap, position: Int, config: TextureConfig): Int {
        glGenTextures(TEXTURE_COUNT, textureHandler, 0)
        bindTexture(textureHandler[position], position)
        setTextureParameters(config)
        GLUtils.texImage2D(GL_TEXTURE_2D, MIPMAPPING_LEVEL, bitmap, 0)

        if (textureHandler[position] == 0)
            throw RuntimeException(
                "Invalid texture generated from a Bitmap for configuration ( " +
                        "position = $position, texture config = $config )"
            )

        return textureHandler[position]
    }

    fun bindTexture(texture: Int, position: Int = 0) {
        glActiveTexture(GL_TEXTURE0 + position)
        glBindTexture(GL_TEXTURE_2D, texture)
    }

    private fun setTextureParameters(config: TextureConfig) {
        when (config) {
            TextureConfig.FULLSCREEN -> selectFullScreenParams()
            TextureConfig.ORIGINAL -> selectOriginalParams()
        }
    }

    private fun selectFullScreenParams() {
        with(GL_TEXTURE_2D) {
            glTexParameteri(this, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(this, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            glTexParameteri(this, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(this, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        }
    }

    private fun selectOriginalParams() {
        with(GL_TEXTURE_2D) {
            glTexParameteri(this, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
            glTexParameteri(this, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        }
    }
}