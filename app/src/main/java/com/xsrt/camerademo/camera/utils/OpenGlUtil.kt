package com.xsrt.camerademo.camera.utils

import android.util.Log
import com.xsrt.camerademo.App

class OpenGlUtil {
    companion object {
        fun uRes(path: String): String {
            val res = App.getContext().resources
            val result = StringBuilder()
            try {
                val stream = res!!.assets.open(path)
                val bytes = ByteArray(1024)
                var ch = 0
                do {
                    result.append(String(bytes, 0, ch))
                    ch = stream.read(bytes)
                } while (-1 != ch)
            } catch (e: Exception) {
                Log.e("tag", "read file error ${e.message}")
                e.printStackTrace()
                return ""
            }
            return result.toString().replace("\\r\\n", "\n")
        }

    }
}