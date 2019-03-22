package com.xsrt.camerademo.camera

import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.service.autofill.Validators.or
import com.xsrt.camerademo.camera.filter.AFliter
import com.xsrt.camerademo.camera.filter.CameraFilter
import com.xsrt.camerademo.camera.filter.NoFliter
import com.xsrt.camerademo.camera.utils.EasyGlUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraDrawer {
    private var mTextureId = 0
    private var mPreViewWidth: Int = 0
    private var mPreViewHeight: Int = 0
    private var showFilter: AFliter? = null
    private var drawFilter: AFliter? = null
    private var mSurfaceTexture: SurfaceTexture? = null

    private val fFrame: IntArray? = IntArray(1)
    private val fTexture: IntArray? = IntArray(1)

    constructor(resources: Resources) {
        showFilter = NoFliter(resources)
        drawFilter = CameraFilter(resources)
    }

    fun setCameraId(cameraId: Int) {
        drawFilter?.setFlag(cameraId)
    }

    fun getTexture(): SurfaceTexture? {
        return mSurfaceTexture
    }

    fun setPreViewSize(width: Int, height: Int) {
        if ((mPreViewHeight != width) or (mPreViewHeight != height)) {
            mPreViewWidth = width
            mPreViewHeight = height
        }
    }

    fun onDrawFrame(gl: GL10?) {
        mSurfaceTexture?.updateTexImage()
        /**绘制显示的filter*/
        EasyGlUtil.bindFrameTexture(fFrame!![0], fTexture!![0])
        GLES20.glViewport(0, 0, mPreViewWidth, mPreViewHeight)
        drawFilter?.draw()
        EasyGlUtil.unBindFrameBuffer()

    }


    fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glDeleteFramebuffers(1, fFrame, 0)
        GLES20.glDeleteTextures(1, fTexture, 0)
        GLES20.glGenFramebuffers(1, fFrame, 0)
        GLES20.glGenTextures(1, fTexture, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture!![0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                mPreViewWidth,
                mPreViewHeight,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null)
        useTexParameter()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mTextureId = creaTextureId()
        mSurfaceTexture = SurfaceTexture(mTextureId)

        drawFilter?.create()
        drawFilter?.setTextureId(mTextureId)
    }


    private fun useTexParameter() {
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
    }

    private fun creaTextureId(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        return texture[0]
    }

}