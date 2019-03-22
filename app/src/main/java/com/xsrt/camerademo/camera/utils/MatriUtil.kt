package com.xsrt.camerademo.camera.utils

class MatriUtil {
    companion object {
        fun getOriginalMatrix(): FloatArray {
            return floatArrayOf(
                    1.0f, 0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f, 0.0f,
                    0.0f, 0.0f, 1.0f, 0.0f,
                    0.0f, 0.0f, 0.0f, 1.0f
            )
        }
    }
}