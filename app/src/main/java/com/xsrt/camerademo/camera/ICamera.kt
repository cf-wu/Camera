package com.xsrt.camerademo.camera

import android.graphics.Point
import android.graphics.SurfaceTexture

interface ICamera {
    fun open(cameraId: Int)
    fun setPreviewTexture(texture: SurfaceTexture)
    fun setConfig(config: Config)
    fun preView()
    fun getPreViewSize(): Point
    fun close(): Boolean
    fun setOnPreViewCallBack(callback: PreViewFrameCallBack)
    class Config {
        val rate = 1.778f//宽高比
        var minPreViewWidth: Int? = null
        var minPictureWidth: Int? = null
    }

    interface PreViewFrameCallBack {
        fun onPreViewFrame(byteArray: ByteArray, width: Int, heigth: Int)
    }
}