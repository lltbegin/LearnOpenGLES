package com.example.learnopengles.help

import android.graphics.Bitmap
import android.graphics.Canvas
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates


/*
    Renderer
 */
class GLImageRenderer : GLSurfaceView.Renderer {
    private lateinit var mFilter: GLBaseFilter
    private var mGLCubeBuffer: FloatBuffer by Delegates.notNull()
    private var mGLTextureBuffer: FloatBuffer by Delegates.notNull()
    private val mRunOnDraw: Queue<Runnable> = LinkedList<Runnable>()
    private var mOutputWidth = 0
    private var mOutputHeight = 0

    //    private val mSurfaceChangedWaiter = java.lang.Object()
    private var gpuBgColors: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)
    private val NO_IMAGE = -1
    private var mGLTextureId: Int = NO_IMAGE

    private val sCube = floatArrayOf(
//        -1.0f, 1.0f,
//        -1.0f, -1.0f,
//        1.0f, 1.0f,
//        1.0f, -1.0f
        -1.0f, -1.0f,
        1.0f, -1.0f,
        -1.0f, 1.0f,
        1.0f, 1.0f
    )

    private val sTextureCoord = floatArrayOf(
//        0.0f, 0.0f,
//        0.0f, 1.0f,
//        1.0f, 0.0f,
//        1.0f, 1.0f
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )

    constructor(filter: GLBaseFilter) {
        mFilter = filter
        mGLCubeBuffer = ByteBuffer.allocateDirect(sCube.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mGLCubeBuffer.put(sCube).position(0)
        mGLTextureBuffer = ByteBuffer
            .allocateDirect(sTextureCoord.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        mGLTextureBuffer.put(sTextureCoord).position(0)


    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.i("渲染流程", "onSurfaceCreated ")
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        mFilter.onInit()
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        Log.i("渲染流程", "onSurfaceChanged width:$width height:$height")
        mOutputWidth = width
        mOutputHeight = height
        GLES20.glViewport(0, 0, width, height)
        GLES20.glUseProgram(mFilter.getProgramId())
        mFilter.onOutputSizeChanged(width, height)
//        synchronized(mSurfaceChangedWaiter) { mSurfaceChangedWaiter.notifyAll() }
    }


    override fun onDrawFrame(gl: GL10?) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        synchronized(mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.poll().run()
            }
        }

        if (mGLTextureId == NO_IMAGE) {
            return
        }
        GLES20.glClearColor(
            gpuBgColors[0], gpuBgColors[1], gpuBgColors[2],
            gpuBgColors[3]
        )
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (mFilter != null) {
            mFilter.draw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer)
        }
    }

    /**
     * @param r red 0-1.0f
     * @param g green 0-1.0f
     * @param b blue 0-1.0f
     * @param a alpha 0-1.0f
     */
    fun setGpuBgColors(
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        this.gpuBgColors = floatArrayOf(r, g, b, a)
    }

    interface RenderPartEvent {
        fun onPart1Generate()
        fun onPart2Generate()
    }

    private lateinit var renderPartEvent: RenderPartEvent

    fun setRenderPartEvent(renderPartEvent: RenderPartEvent) {
        this.renderPartEvent = renderPartEvent
    }

    //该方法只适合filtergroup时调用
//    fun onDrawFrameByPart(gl: GL10?) {
//
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
//        synchronized(mRunOnDraw) {
//            while (!mRunOnDraw.isEmpty()) {
//                mRunOnDraw.poll().run()
//            }
//        }
//
//        if (mGLTextureId == NO_IMAGE) {
//            return
//        }
//        if (gpuBgColors == null || gpuBgColors.size < 4) {
//            GLES20.glClearColor(1f, 1f, 1f, 1f)
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        } else {
//            GLES20.glClearColor(
//                gpuBgColors[0], gpuBgColors[1], gpuBgColors[2],
//                gpuBgColors[3]
//            )
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        }
//
//        if (mFilter is GLFilterGroup) {
//            (mFilter as GLFilterGroup).drawByPart(
//                mGLTextureId,
//                mGLCubeBuffer,
//                mGLTextureBuffer,
//                renderPartEvent
//            )
//        }
//    }

    fun setFilter(filter: GLBaseFilter,isRunNow :Boolean = false) {
        if(isRunNow){
            val oldFilter: GLBaseFilter = mFilter
            mFilter = filter
            if (mFilter != null) {
                mFilter.onInit()
                GLES20.glUseProgram(mFilter.getProgramId())
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight)
            }
            if (oldFilter != null) {
                oldFilter.destroy()
            }
        }else{
            runOnDraw(Runnable {
                val oldFilter: GLBaseFilter = mFilter
                mFilter = filter
                if (oldFilter != null) {
                    oldFilter.destroy()
                }
                if (mFilter != null) {
                    mFilter.onInit()
                    GLES20.glUseProgram(mFilter.getProgramId())
                    mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight)
                }
            })
        }
    }

    fun getFilter(): GLBaseFilter? {
        return mFilter
    }

    fun deleteImage() {
        if (mGLTextureId != NO_IMAGE) {
            GLES20.glDeleteTextures(1, intArrayOf(mGLTextureId), 0)
            mGLTextureId =
                NO_IMAGE
        }
    }

    fun setImageBitmap(bitmap: Bitmap) {
        setImageBitmap(bitmap, false)
    }


    fun setImageBitmap(
        bitmap: Bitmap,
        recycle: Boolean
    ) {
        removeLoadTextureTask()
        runOnDraw(
            LoadTextureWorker(
                bitmap,
                recycle
            )
        )
    }

    protected fun runOnDraw(runnable: Runnable?) {
        synchronized(mRunOnDraw) { mRunOnDraw.add(runnable) }
    }

    private fun removeLoadTextureTask() {
        synchronized(mRunOnDraw) {
            for (task in mRunOnDraw) {
                if (task is LoadTextureWorker) {
                    mRunOnDraw.remove(task)
                    return
                }
            }
        }
    }

    //    private class LoadTextureWorker constructor(
//        texture: Bitmap,
//        recycleBmp: Boolean
//    ) :
//        Runnable {
//        private val bitmap: Bitmap?
//        private val recycle: Boolean
//        override fun run() {
//            var resizedBitmap: Bitmap? = null
//            if (bitmap == null || bitmap.isRecycled) {
//                return
//            }
//            if (bitmap.width % 2 == 1) {
//                resizedBitmap = Bitmap.createBitmap(
//                    bitmap.width - 1,
//                    bitmap.height, Bitmap.Config.ARGB_8888
//                )
//                val can = Canvas(resizedBitmap)
//                can.drawARGB(0x00, 0x00, 0x00, 0x00)
//                if (bitmap != null && !bitmap.isRecycled) {
//                    can.drawBitmap(bitmap, 0f, 0f, null)
//                }
//            } else {
//            }
//            if (mGLTextureId != BeautyGPUImageRenderer.NO_IMAGE) {
//                GLES20.glDeleteTextures(1, intArrayOf(mGLTextureId), 0)
//                mGLTextureId = BeautyGPUImageRenderer.NO_IMAGE
//            }
//            mGLTextureId = OpenGlUtils.loadTexture(
//                resizedBitmap ?: bitmap,
//                mGLTextureId, recycle
//            )
//            resizedBitmap?.recycle()
//        }
//
//        init {
//            bitmap = texture
//            recycle = recycleBmp
//        }
//    }
    inner class LoadTextureWorker constructor(
        texture: Bitmap,
        recycleBmp: Boolean
    ) :
        Runnable {
        private val bitmap: Bitmap
        private val recycle: Boolean

        override fun run() {
            var resizedBitmap: Bitmap? = null
            if (bitmap == null || bitmap.isRecycled) {
                return
            }
            if (bitmap.width % 2 == 1) {
                resizedBitmap = Bitmap.createBitmap(
                    bitmap.width - 1,
                    bitmap.height, Bitmap.Config.ARGB_8888
                )
                val can = Canvas(resizedBitmap)
                can.drawARGB(0x00, 0x00, 0x00, 0x00)
                if (bitmap != null && !bitmap.isRecycled) {
                    can.drawBitmap(bitmap, 0f, 0f, null)
                }
            }
            if (mGLTextureId != NO_IMAGE) {
                GLES20.glDeleteTextures(1, intArrayOf(mGLTextureId), 0)
                mGLTextureId =
                    NO_IMAGE
            }
            mGLTextureId = ShaderHelp.loadTexture(
                resizedBitmap ?: bitmap,
                mGLTextureId, recycle
            )

            resizedBitmap?.recycle()

        }

        init {
            bitmap = texture
            recycle = recycleBmp
        }
    }
}