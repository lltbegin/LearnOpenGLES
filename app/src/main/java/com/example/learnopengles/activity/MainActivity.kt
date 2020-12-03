package com.example.learnopengles.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.learnopengles.R
import com.example.learnopengles.learn.glsfview.GLSurfaceViewActivity
import com.example.learnopengles.learn.lut.LUTActivity
import com.example.learnopengles.learn.simple.SimpleActivity
import com.example.learnopengles.learn.texture.TextureActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_simple.setOnClickListener{
            var intent = Intent(this@MainActivity, SimpleActivity::class.java)
            startActivity(intent)
        }

        btn_texture.setOnClickListener{
            var intent = Intent(this@MainActivity, TextureActivity::class.java)
            startActivity(intent)
        }

        btn_lut.setOnClickListener{
            var intent = Intent(this@MainActivity, LUTActivity::class.java)
            startActivity(intent)
        }

        btn_glsf.setOnClickListener{
            var intent = Intent(this@MainActivity, GLSurfaceViewActivity::class.java)
            startActivity(intent)
        }
    }
}