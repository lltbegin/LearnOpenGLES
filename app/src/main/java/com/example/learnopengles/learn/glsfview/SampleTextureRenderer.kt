package com.example.learnopengles.learn.glsfview

import android.opengl.GLSurfaceView
import com.example.learnopengles.learn.lut.LUTFilter
import com.example.learnopengles.util.Util
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
class SampleTextureRenderer : GLSurfaceView.Renderer {

    // GLSurfaceView的宽高
    // The width and height of GLSurfaceView
    private var glSurfaceViewWidth = 0
    private var glSurfaceViewHeight = 0

    lateinit var  simpleFilter:LUTFilter

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        setFilter()
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // 记录GLSurfaceView的宽高
        // Record the width and height of the GLSurfaceView
        glSurfaceViewWidth = width
        glSurfaceViewHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        simpleFilter.onDrawFrame(glSurfaceViewWidth,glSurfaceViewHeight)
    }

    private  fun setFilter(){

        var bitmap =Util.decodeBitmapFromAssets("testpic.jpg")
        var lutBitmap =Util.decodeBitmapFromAssets("lut_test.png")

        simpleFilter = LUTFilter()
        simpleFilter.init()
        simpleFilter.setTexture(bitmap)
        simpleFilter.setTexture2(lutBitmap)
    }
}