package com.xsrt.camerademo.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.xsrt.camerademo.camera.CameraController
import com.xsrt.camerademo.camera.CameraDrawer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraView : GLSurfaceView, GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {


    private var mContext: Context? = null
    private var mCameraDrawer: CameraDrawer? = null
    private var mCamera: CameraController? = null
    private var preW = 0
    private var preH = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context!!
        setup()
    }

    private fun setup() {
        //初始化信息
        setEGLContextClientVersion(2)
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
        cameraDistance = 100.0f
        mCameraDrawer = CameraDrawer(resources)
        mCamera = CameraController()
    }

    private fun open(cameraId: Int) {
        mCamera?.close()
        mCamera?.open(cameraId)
        mCameraDrawer?.setCameraId(cameraId)
        val point = mCamera?.getPreViewSize()
        preW = point?.x!!
        preH = point.y
        var texture = mCameraDrawer?.getTexture()
        texture?.setOnFrameAvailableListener(this)
        mCamera?.setPreviewTexture(texture!!)
        mCamera?.preView()
    }

    fun destory() {
        mCamera?.close()
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        this.requestRender()
    }

    override fun onDrawFrame(gl: GL10?) {
        mCameraDrawer?.onDrawFrame(gl)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mCameraDrawer?.onSurfaceChanged(gl, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mCameraDrawer?.onSurfaceCreated(gl, config)
        open(Camera.CameraInfo.CAMERA_FACING_BACK)
        mCameraDrawer?.setPreViewSize(preW, preH)
    }


}