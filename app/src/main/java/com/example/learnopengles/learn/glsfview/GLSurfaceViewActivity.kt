package com.example.learnopengles.learn.glsfview

import android.graphics.BitmapFactory
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.example.learnopengles.R
import com.example.learnopengles.util.Util
import kotlinx.android.synthetic.main.activity_simple_glsfview.*


class GLSurfaceViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_glsfview)


        Util.context = applicationContext

        // 设置RGBA颜色缓冲、深度缓冲及stencil缓冲大小
        glsurfaceview.setEGLConfigChooser(8, 8, 8, 8, 0, 0)
        // 设置GL版本，这里设置为2.0
        glsurfaceview.setEGLContextClientVersion(2)
        // 设置对应sample的渲染器
        glsurfaceview.setRenderer(SampleTextureRenderer())
    }
}