package com.xsrt.camerademo.camera

import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import java.util.*
import kotlin.Comparator

class CameraController : ICamera {
    override fun setOnPreViewCallBack(callback: ICamera.PreViewFrameCallBack) {
        if (mCamera != null) {
            mCamera!!.setPreviewCallback { data, _ ->
                callback.onPreViewFrame(data!!, mPreSize!!.x, mPreSize!!.y)
            }
        }
    }

    private var mConfig: ICamera.Config? = null
    private var mCamera: Camera? = null
    private var preSize: Camera.Size? = null
    private var picSize: Camera.Size? = null
    private var mPreSize: Point? = null
    private var mPicSize: Point? = null

    constructor() {
        mConfig = ICamera.Config()
        mConfig!!.minPictureWidth = 720
        mConfig!!.minPreViewWidth = 720
    }

    override fun open(cameraId: Int) {
        mCamera = Camera.open(cameraId)
        if (mCamera != null) {

            /** 初始化尺寸*/
            val parameters = mCamera!!.parameters
            preSize = getProPreviewSize(parameters.supportedPreviewSizes, mConfig!!.rate, mConfig!!.minPreViewWidth!!)
            picSize = getProPicSize(parameters.supportedPictureSizes, mConfig!!.rate, mConfig!!.minPictureWidth!!)
            parameters.setPictureSize(picSize!!.width, picSize!!.height)
            parameters.setPreviewSize(preSize!!.width, preSize!!.height)
            mCamera!!.parameters = parameters
            val previewSize = parameters.previewSize
            val pictureSize = parameters.pictureSize
            mPicSize = Point(pictureSize.height, pictureSize.width)
            mPreSize = Point(previewSize.height, previewSize.height)
        }

    }

    private var sizeComparator: Comparator<Camera.Size> = Comparator { v1, v2 ->
        when {
            v1.height == v2.height -> return@Comparator 0
            v1.height > v2.height -> return@Comparator 1
            else -> return@Comparator -1
        }

    }

    private fun getProPicSize(list: List<Camera.Size>, rate: Float, minWidth: Int): Camera.Size? {
        Collections.sort(list, sizeComparator)
        var i = 0
        for (s in list) {
            if ((s.height >= minWidth) && equalRate(s, rate)) {
                break
            }
            i++
        }
        if (i == list.size) {
            i = 0
        }
        return list[i]

    }

    private fun equalRate(s: Camera.Size, rate: Float): Boolean {
        val r = s.width.toFloat() / s.height.toFloat()
        return Math.abs(r - rate) <= 0.03
    }

    private fun getProPreviewSize(list: List<Camera.Size>, rate: Float, minWidth: Int): Camera.Size? {
        Collections.sort(list, sizeComparator)
        var i = 0
        for (s in list) {
            if ((s.height >= minWidth) && equalRate(s, rate)) {
                break
            }
            i++
        }
        if (i == list.size) {
            i = 0
        }
        return list[i]
    }

    override fun setPreviewTexture(texture: SurfaceTexture) {
        if (mCamera != null) {
            try {
                Log.e("tag", "----setPreviewTexture")
                mCamera!!.setPreviewTexture(texture)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun setConfig(config: ICamera.Config) {
        this.mConfig = config
    }

    override fun preView() {
        if (mCamera != null) {
            mCamera!!.startPreview()
        }
    }

    override fun getPreViewSize(): Point {
        return mPreSize!!
    }

    override fun close(): Boolean {
        if (mCamera != null) {
            mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
        }
        return false
    }

}