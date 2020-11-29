package com.example.learnopengles.activity

import android.R.attr.bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.learnopengles.R
import com.example.learnopengles.help.EGLHelp
import kotlinx.android.synthetic.main.activity_simple_egl.*


class SimpleEGLActivity : AppCompatActivity() {

    lateinit var rendererHandler:Handler
    var MSG_INIT_EGL = 1
    var MSG_RELEASE_EGL = 2
    var MSG_SET_FILTER = 3
    var MSG_INIT_TEST = 4
    val TAG = "chihong"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_egl)


        val picInputStream = assets.open("testpic.jpg")
        var bitmap = BitmapFactory.decodeStream(picInputStream)

        val handlerThread = HandlerThread("renderThread")
        handlerThread.start()


        rendererHandler = object : Handler(handlerThread.looper){
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_INIT_EGL -> {
                        Log.i(TAG,"EGLHelp.init");
                        EGLHelp.init()
                        EGLHelp.initSurface(bitmap.width,bitmap.height)
                        EGLHelp.bind()
                    }
                    MSG_RELEASE_EGL -> {
                        Log.i(TAG,"EGLHelp.release");
                        EGLHelp.release()
                    }
                    MSG_SET_FILTER -> {

                    }
                    else -> {
                        Log.i(TAG,"EGLHelp test");
                        val textures = IntArray(1)
                        GLES30.glGenTextures(textures.size, textures, 0)
                        val imageTexture = textures[0]
                    }
                }
                super.handleMessage(msg)
            }
        }

        btn_ori.setOnClickListener {
            rendererHandler.sendEmptyMessage(MSG_INIT_EGL)
        }

        btn_gray.setOnClickListener {
            rendererHandler.sendEmptyMessage(MSG_RELEASE_EGL)
        }

        btn_lut.setOnClickListener {
            rendererHandler.sendEmptyMessage(MSG_INIT_TEST)
        }


    }
}