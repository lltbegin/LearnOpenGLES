package com.example.learnopengles.help

import android.graphics.PointF
import android.opengl.GLES20
import java.nio.FloatBuffer
import java.util.*

val DEFAULT_VERTEX_SHADER = "" +
        "attribute vec4 position;\n" +
        "attribute vec4 inputTextureCoordinate;\n" +
        "varying vec2 textureCoordinate;\n" +
        "void main()\n" +
        "{\n" +
        "    gl_Position = vec4(position.xyz, 1.0);\n" +
        "    textureCoordinate = inputTextureCoordinate.xy;\n" +  //            "    textureCoordinate = vec2((1.0+position.x)/2.0, (1.0-position.y)/2.0);" +
        "}"
val DEFAULT_FRAGMENT_SHADER = "" +
        "varying highp vec2 textureCoordinate;\n" +
        "uniform sampler2D inputImageTexture;\n" +
        "void main()\n" +
        "{\n" +
//        "      if(textureCoordinate.x > 0.5){" +
//        "        gl_FragColor = texture2D(inputImageTexture, textureCoordinate)*0.5;   " +
//        "   }else{" +
        "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
//        "   }" +
//        "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
        "}"

open class GLBaseFilter(vertexShader: String = DEFAULT_VERTEX_SHADER, fragmentShader: String = DEFAULT_FRAGMENT_SHADER) {


    private var mVertexShader: String = vertexShader
    private var mFragmentShader: String = fragmentShader
    private var mRunOnDraw: LinkedList<Runnable> = LinkedList()
    private var mGLProgId = -1
    private var mGLAttribPosition = 0
    private var filterTextureLocation = 0
    private var filterTextureCoordinateLocation = 0
    protected var mIsInitialized = false
    protected var mOutputWidth = 0
    protected var mOutputHeight = 0
    private var isDividePartLine = false

    open fun onInit() {
        mGLProgId = ShaderHelp.buildProgram(mVertexShader, mFragmentShader)
        mGLAttribPosition = GLES20.glGetAttribLocation(mGLProgId, "position")
        filterTextureLocation = GLES20.glGetUniformLocation(mGLProgId, "inputImageTexture")
        filterTextureCoordinateLocation = GLES20.glGetAttribLocation(
            mGLProgId,
            "inputTextureCoordinate"
        )
        mIsInitialized = true
        ShaderHelp.checkError("BaseFilter_onInit")

    }

    /*
        textureId : 纹理id
        cubeBuffer: 顶点坐标数据
        texturePosBuffer：纹理坐标数据
        该方法适合的是一个输入源的情况
     */
    open fun draw(
        textureId: Int, vertexPosBuffer: FloatBuffer,
        texturePosBuffer: FloatBuffer
    ) {

        if (!mIsInitialized) {
            return
        }

        GLES20.glUseProgram(mGLProgId)
        //调用例如赋值之类的延迟操作
        runPendingOnDrawTasks()

        if (textureId != ShaderHelp.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(filterTextureLocation, 0)
        }

        onDrawArraysPre(vertexPosBuffer, texturePosBuffer)

        vertexPosBuffer.position(0)
        GLES20.glVertexAttribPointer(
            mGLAttribPosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexPosBuffer
        )
        GLES20.glEnableVertexAttribArray(mGLAttribPosition)
        texturePosBuffer.position(0)
        GLES20.glVertexAttribPointer(
            filterTextureCoordinateLocation, 2, GLES20.GL_FLOAT, false, 0,
            texturePosBuffer
        )
        GLES20.glEnableVertexAttribArray(filterTextureCoordinateLocation)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        onDrawArraysAfter()

        GLES20.glDisableVertexAttribArray(mGLAttribPosition)
        GLES20.glDisableVertexAttribArray(filterTextureCoordinateLocation)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        ShaderHelp.checkError("BaseFilter_draw")
    }

    protected open fun onDrawArraysPre(vertexPosBuffer: FloatBuffer,
                                       texturePosBuffer: FloatBuffer) {}

    protected open fun onDrawArraysAfter() {}

    protected fun runPendingOnDrawTasks() {
        synchronized(mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.removeFirst().run()
            }
        }
    }

    fun getProgramId():Int{
        return mGLProgId;
    }

    open fun isInitialized(): Boolean {
        return mIsInitialized
    }

    open fun onOutputSizeChanged(width: Int, height: Int) {
        mOutputWidth = width
        mOutputHeight = height
    }

    fun destroy() {
        mIsInitialized = false
        if (mGLProgId != -1) {
            GLES20.glDeleteProgram(mGLProgId)
            mGLProgId = -1
        }
        onDestroy()
    }

    open fun onDestroy() {}

    fun runOnDraw(runnable: Runnable?) {
        synchronized(mRunOnDraw) { mRunOnDraw.addLast(runnable) }
    }

    fun setInteger(location: Int, intValue: Int) {
        runOnDraw(Runnable { GLES20.glUniform1i(location, intValue) })
    }

    fun setFloat(location: Int, floatValue: Float) {
        runOnDraw(Runnable { GLES20.glUniform1f(location, floatValue) })
    }

    fun setFloatVec2(location: Int, arrayValue: FloatArray?) {
        runOnDraw(Runnable { GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue)) })
    }

    fun setFloatVec3(location: Int, arrayValue: FloatArray?) {
        runOnDraw(Runnable { GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue)) })
    }

    fun setFloatVec4(location: Int, arrayValue: FloatArray?) {
        runOnDraw(Runnable { GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue)) })
    }

    fun setFloat(msg: String, location: Int, floatValue: Float) {
        runOnDraw(Runnable {
            GLES20.glUniform1f(location, floatValue)
        })
    }

    fun setFloatVec2(
        msg: String,
        location: Int,
        arrayValue: FloatArray?
    ) {
        runOnDraw(Runnable {
            GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue))
        })
    }

    fun setFloatVec3(
        msg: String,
        location: Int,
        arrayValue: FloatArray?
    ) {
        runOnDraw(Runnable {
            GLES20.glUniform3fv(location, 1, FloatBuffer.wrap(arrayValue))
        })
    }

    fun setFloatVec4(
        msg: String,
        location: Int,
        arrayValue: FloatArray?
    ) {
        runOnDraw(Runnable {
            GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue))
        })
    }

    fun setFloatArray(location: Int, arrayValue: FloatArray) {
        runOnDraw(Runnable {
            GLES20.glUniform1fv(
                location,
                arrayValue.size,
                FloatBuffer.wrap(arrayValue)
            )
        })
    }

    fun setPoint(location: Int, point: PointF) {
        runOnDraw(Runnable {
            val vec2 = FloatArray(2)
            vec2[0] = point.x
            vec2[1] = point.y
            GLES20.glUniform2fv(location, 1, vec2, 0)
        })
    }

    fun setUniformMatrix3f(location: Int, matrix: FloatArray?) {
        runOnDraw(Runnable { GLES20.glUniformMatrix3fv(location, 1, false, matrix, 0) })
    }

    fun setUniformMatrix4f(location: Int, matrix: FloatArray?) {
        runOnDraw(Runnable { GLES20.glUniformMatrix4fv(location, 1, false, matrix, 0) })
    }

    open fun isDividePartLine(): Boolean {
        return isDividePartLine
    }

    open fun setDividePartLine(dividePartLine: Boolean) {
        isDividePartLine = dividePartLine
    }

    open fun cloneSelf(width: Int,height: Int):GLBaseFilter{
        return GLBaseFilter()
    }
}