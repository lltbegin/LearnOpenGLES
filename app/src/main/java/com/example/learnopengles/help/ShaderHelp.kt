package com.example.learnopengles.help

import android.graphics.Bitmap
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLES20.*
import android.opengl.GLUtils
import android.util.Log
import java.nio.IntBuffer

val LOG_SWITCH = true

class ShaderHelp {

    companion object {
        val NO_TEXTURE = -1

        fun compileVertexShader(shaderCode: String): Int {
            return compileShader(GL_VERTEX_SHADER, shaderCode)
        }

        fun compileFragmentShader(shaderCode: String): Int {
            return compileShader(GL_FRAGMENT_SHADER, shaderCode)
        }

        fun compileShader(type: Int, shaderCode: String): Int {
            var shaderObjectId = glCreateShader(type)
            if (shaderObjectId == 0) {
                if (LOG_SWITCH) {
                    Log.i("shader", "创建着色器对象失败");
                }
                return 0;
            }
            glShaderSource(shaderObjectId, shaderCode)

            glCompileShader(shaderObjectId)

            var compileStatus = IntArray(1)

            glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0)

            if (LOG_SWITCH) {
//                Log.i(
//                    "shader",
//                    "编译着色器代码结果:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId)
//                );
            }

            if (compileStatus[0] == 0) {
                glDeleteShader(shaderObjectId)
                if (LOG_SWITCH) {
                    Log.i("shader", "编译着色器代码失败");
                }
            }
            return shaderObjectId;
        }

        fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
            var programObjectId = glCreateProgram()
            if (programObjectId == 0) {
                if (LOG_SWITCH) {
                    Log.i("shader", "创建着色器程序对象失败");
                }
                return 0
            }
            glAttachShader(programObjectId, vertexShaderId)
            glAttachShader(programObjectId, fragmentShaderId)
            glLinkProgram(programObjectId)

            var linkStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0)

            if (LOG_SWITCH) {
                Log.i("shader", "链接着色器程序结果：" + glGetProgramInfoLog(programObjectId));
            }

            if (linkStatus[0] == 0) {
                glDeleteProgram(programObjectId)
                if (LOG_SWITCH) {
                    Log.i("shader", "链接着色器代码失败");
                }
                return 0;
            }
            return programObjectId;
        }

        fun validateProgram(programObjectId: Int): Boolean {
            glValidateProgram(programObjectId)
            var validateStatus = IntArray(1)
            glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0)
            if (LOG_SWITCH) {
                Log.i(
                    "shader",
                    "验证程序状态:" + validateStatus[0] + " Log:" + glGetProgramInfoLog(programObjectId)
                );
            }
            return validateStatus[0] != 0;
        }

        /**
         * Helper function that compiles the shaders, links and validates the
         * program, returning the program ID.
         */
        fun buildProgram(
            vertexShaderSource: String,
            fragmentShaderSource: String
        ): Int {
            val program: Int
            // Compile the shaders.
            val vertexShader: Int =
                compileVertexShader(vertexShaderSource)
            val fragmentShader: Int =
                compileFragmentShader(fragmentShaderSource)
            // Link them into a shader program.
            program =
                linkProgram(vertexShader, fragmentShader)
            if (LOG_SWITCH) {
                validateProgram(program)
            }
            return program
        }

        fun checkError(tag: String = "") {
            var error = GLES20.GL_NO_ERROR
            if (GLES20.glGetError().also {
                    error = it
                } != GLES20.GL_NO_ERROR) {
                if (LOG_SWITCH) {
                    Log.e("shader", "checkError: 0x" + Integer.toHexString(error) + tag);
                }
            }
        }

        fun loadTexture(img: Bitmap, usedTexId: Int): Int {
            return loadTexture(img, usedTexId, true)
        }

        fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean): Int {
            var textureId = -1
            try {
                val textures = IntArray(1)
                if (img == null || img.isRecycled) return 0
                if (usedTexId == ShaderHelp.NO_TEXTURE) {
                    glGenTextures(1, textures, 0)
                    glBindTexture(GL_TEXTURE_2D, textures[0])

                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_LINEAR
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MAG_FILTER,
                        GL_LINEAR
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_WRAP_S,
                        GL_CLAMP_TO_EDGE
                    )
                    glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_WRAP_T,
                        GL_CLAMP_TO_EDGE
                    )

                    if (img == null || img.isRecycled) return 0
                    GLUtils.texImage2D(GL_TEXTURE_2D, 0, img, 0)
                } else {
                    if (img == null || img.isRecycled) return 0
                    glBindTexture(GL_TEXTURE_2D, usedTexId)
                    GLUtils.texSubImage2D(GL_TEXTURE_2D, 0, 0, 0, img)
                    textures[0] = usedTexId
                }
                textureId = textures[0]
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (recycle) {
                img?.recycle()
            }
            return textureId
        }

        fun loadTexture(
            data: IntBuffer,
            size: Camera.Size,
            usedTexId: Int
        ): Int {
            val textures = IntArray(1)
            if (usedTexId == ShaderHelp.NO_TEXTURE) {
                glGenTextures(1, textures, 0)
                glBindTexture(GL_TEXTURE_2D, textures[0])
                // Set filtering: a default must be set, or the texture will be
// black.
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR_MIPMAP_LINEAR
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_MAG_FILTER,
                    GL_LINEAR
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_S,
                    GL_CLAMP_TO_EDGE
                )
                glTexParameteri(
                    GL_TEXTURE_2D,
                    GL_TEXTURE_WRAP_T,
                    GL_CLAMP_TO_EDGE
                )
                glTexImage2D(
                    GL_TEXTURE_2D, 0, GL_RGBA, size.width, size.height,
                    0, GL_RGBA, GL_UNSIGNED_BYTE, data
                )
            } else {
                glBindTexture(GL_TEXTURE_2D, usedTexId)
                glTexSubImage2D(
                    GL_TEXTURE_2D, 0, 0, 0, size.width,
                    size.height, GL_RGBA, GL_UNSIGNED_BYTE, data
                )
                textures[0] = usedTexId
            }
            return textures[0]
        }

        fun loadTextureAsBitmap(
            data: IntBuffer,
            size: Camera.Size,
            usedTexId: Int
        ): Int {
            val bitmap = Bitmap
                .createBitmap(data.array(), size.width, size.height, Bitmap.Config.ARGB_8888)
            return loadTexture(bitmap, usedTexId)
        }

    }
}