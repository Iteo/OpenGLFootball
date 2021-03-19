package com.iteo.openglfootball

import java.nio.FloatBuffer

class CircleShader : GLShader {

    override val vertex: String =
        """
            attribute vec4 vertexPosition;
            attribute vec2 backTexPosition;
            attribute vec2 frontTexPosition;
            
            varying vec2 vBackTexPosition;
            varying vec2 vFrontTexPosition;
            
            void main() {
                gl_Position = vertexPosition;
                vBackTexPosition = backTexPosition; 
                vFrontTexPosition = frontTexPosition;
            }
        """.trimIndent()

    override val fragment: String =
        """
            precision mediump float;
            
            uniform vec2 resolution;
            uniform vec2 touchPosition;
            uniform float radius;
            
            varying vec2 vBackTexPosition;
            varying vec2 vFrontTexPosition;
            
            uniform sampler2D uBackTexture;
            uniform sampler2D uFrontTexture;
            
            float drawCircle(vec2 coord, float radius) {
                return step(length(coord), radius);
            }
            
            void main() {
                // Get current pixel coordinates
                vec2 coord = gl_FragCoord.xy / resolution;
                // Get pixel's color
                float circle = drawCircle(coord - touchPosition, radius);
                
                vec4 backTexture = texture2D(uBackTexture, vBackTexPosition);
                vec4 frontTexture = texture2D(uFrontTexture, vFrontTexPosition);

                if (circle > 0.0) {
                    gl_FragColor = frontTexture;
                } else {
                    gl_FragColor = backTexture;
                }
            }
        """.trimIndent()

    override fun applyCoordinates(
        program: Int,
        coordinatesPerVertex: Int,
        verticesBuffer: FloatBuffer
    ): Int = GLUtil.applyCoordinates(
        program,
        "vertexPosition",
        coordinatesPerVertex,
        verticesBuffer
    )

    override fun applyUniforms(program: Int) {
        GLUtil.applyInt(program, "uBackTexture", BACKGROUND_TEXTURE_POSITION)
        GLUtil.applyInt(program, "uFrontTexture", FOREGROUND_TEXTURE_POSITION)
    }

    override fun applyBackTexture(
        program: Int,
        texture: Int?,
        coordinatesPerVertex: Int,
        textureBuffer: FloatBuffer
    ): Int? = GLUtil.applyCoordinates(
        program,
        "backTexPosition",
        coordinatesPerVertex,
        textureBuffer
    )

    override fun applyFrontTexture(
        program: Int,
        texture: Int?,
        coordinatesPerVertex: Int,
        textureBuffer: FloatBuffer
    ): Int? = GLUtil.applyCoordinates(
        program,
        "frontTexPosition",
        coordinatesPerVertex,
        textureBuffer
    )
}