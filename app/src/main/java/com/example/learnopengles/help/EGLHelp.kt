package com.example.learnopengles.help

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLExt
import android.opengl.GLES20
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGL10

object EGLHelp {
    private var eglDisplay = EGL14.EGL_NO_DISPLAY
    private var eglSurface = EGL14.EGL_NO_SURFACE
    private var eglContext = EGL14.EGL_NO_CONTEXT
    private lateinit var eglConfig: Array<EGLConfig?>

    fun init() {

        // 获取显示设备
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)

        //用于存放支持的EGL的最大和最小版本号
        val version = IntArray(2)
        // 初始化显示设备
        EGL14.eglInitialize(eglDisplay, version, 0, version, 1)

        //期望配置： 将RGBA颜色深度设置为8位，并将OpenGL ES版本设置为2和3，表示同时支持OpenGL 2和OpenGL 3
        val attribList = intArrayOf(
            EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT or EGLExt.EGL_OPENGL_ES3_BIT_KHR, EGL14.EGL_NONE
        )
        // 系统会按照我们的期望配置返回一个按照匹配程度排序的列表
        eglConfig = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        EGL14.eglChooseConfig(
            eglDisplay, attribList, 0, eglConfig, 0, eglConfig.size,
            numConfigs, 0
        )

        // 创建EGL Context
        eglContext = EGL14.eglCreateContext(
            eglDisplay, eglConfig[0], EGL14.EGL_NO_CONTEXT,
            intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE), 0
        )

    }

    /**
     * 创建Surface
     */
    fun initSurface(width:Int,height:Int){
        val surfaceAttribs = intArrayOf(
            EGL10.EGL_WIDTH, width,
            EGL10.EGL_HEIGHT, height,
            EGL10.EGL_NONE
        )

        //如果我们不需要渲染出来看，那么就可以创建一个pbuffer surface，它不和surface绑定，不需要传surface给它，这也称为离屏渲染，
        // 本文中将创建pbuffer surface
        eglSurface = EGL14.eglCreatePbufferSurface(eglDisplay, eglConfig[0], surfaceAttribs, 0)

        // 创建windows surface
        //如果我们创建这个EGL环境是为了跟一块Surface绑定，例如希望渲染到SurfaceView上，那么就要选择windows surface，
        //第三个参数surface就是surfaceview对应的surface
//        eglSurface = EGL14.eglCreateWindowSurface(
//            eglDisplay,
//            eglConfig[0],
//            surface,
//            surfaceAttribs,
//            0
//        )
    }

    //将EGL绑定到线程上让它具体有EGL环境
    fun bind() {
        EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)
    }

    fun release() {
        if (eglDisplay !== EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(eglDisplay, eglSurface)
            EGL14.eglDestroyContext(eglDisplay, eglContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(eglDisplay)
        }
        eglDisplay = EGL14.EGL_NO_DISPLAY
        eglContext = EGL14.EGL_NO_CONTEXT
        eglSurface = EGL14.EGL_NO_SURFACE

    }

    private fun checkEglError(msg: String) {
        val error= EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            throw RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error))
        }
    }

    fun getBitmap(width: Int,height: Int): Bitmap? {
        var intBuffer = IntBuffer.allocate(width * height)
        var intArrayTarget = IntArray(width * height)
        intBuffer.position(0)
        GLES20.glReadPixels(
            0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
            intBuffer
        )
        val ia = intBuffer?.array()
        // Convert upside down mirror-reversed image to right-side up normal
        for (i in 0 until height) {
            System.arraycopy(ia, i * width, intArrayTarget, (height - i - 1) * width, width)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(intArrayTarget))

        return bitmap
    }
}