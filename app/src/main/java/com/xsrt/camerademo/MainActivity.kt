package com.xsrt.camerademo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initListener()
    }

    private fun initListener() {
        to_camera2.setOnClickListener { startActivity(Intent(this@MainActivity, Camera2Activity::class.java)) }
        to_camera.setOnClickListener { startActivity(Intent(this@MainActivity, CameraActivity::class.java)) }
    }

    external fun stringFromJNI(): String

    companion object {

        init {
            System.loadLibrary("native-lib")
        }
    }
}
