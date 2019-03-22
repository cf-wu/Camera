package com.xsrt.camerademo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import com.xsrt.camerademo.record.Camera2Utils
import com.xsrt.camerademo.record.RecordThread
import kotlinx.android.synthetic.main.activity_camera2.*
import java.util.*
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.util.SparseIntArray


class Camera2Activity : AppCompatActivity(), TextureView.SurfaceTextureListener {

    private val FRONT_FACE = 1
    private val BACK_FACE = 0
    private val REQUDST_CODE: Int = 101
    /**
     * 录像需要的权限
     */
    private val VIDEO_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var mCameraDevice: CameraDevice? = null
    private var mVideoSize: Size? = null
    private var mPreviewSize: Size? = null
    private var mRecordThread: RecordThread? = null
    private var mRecordHandler: Handler? = null
    private var mCameraId: String = ""
    private var isFrontFacing = false
    private var mWidth = 0
    private var mHeight = 0

    //拍照
    private var mImageReader: ImageReader? = null
    private var mCaptureSize: Size? = null
    //录像
    private var mMediaRecorder: MediaRecorder? = null

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        return false
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        this.mWidth = width
        this.mHeight = height
        openCamera(width, height, BACK_FACE)
    }

    private val mStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            //开启预览
            startPerview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            mCameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            mCameraDevice = null
        }

    }

    private lateinit var mPreviewSession: CameraCaptureSession
    private lateinit var mPerviewRequest:CaptureRequest
    private lateinit var mPerviewBuilder:CaptureRequest.Builder
    private fun startPerview() {
        val surfaceTexture = camera_view.surfaceTexture!!
        //设置缓冲区大小
        surfaceTexture.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
        //获取显示数据
        val surface = Surface(surfaceTexture)
        //发起预览请求
        mPerviewBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)!!
        mPerviewBuilder?.addTarget(surface)

        //默认预览不开启闪光灯
        mPerviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)

        mCameraDevice?.createCaptureSession(Arrays.asList(surface,mImageReader!!.surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                //创建捕获请求
                mPreviewSession = session
                mPerviewRequest = mPerviewBuilder.build()
                //设置反复抓取数据
                session.setRepeatingRequest(mPerviewRequest, null, null)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {

            }

        }, null)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera2)
        if (!hasPermissions()) {
            requestPermissions(VIDEO_PERMISSIONS, REQUDST_CODE)
        }
        initView()
        initListener()
    }

    private fun initView() {
        startRecordThread()//开启线程
        camera_view.surfaceTextureListener = this
    }

    private fun initListener() {
        iv_toggle.setOnClickListener {
            //切换摄像头
            switchCamera()
        }
        start_stop.setOnClickListener {
            takePickture()
        }

    }

    private fun switchCamera() {
        if (null != mCameraDevice) {
            mCameraDevice?.close()
            mCameraDevice = null
        }
        if (isFrontFacing) {
            openCamera(mWidth, mHeight, BACK_FACE)
        } else {
            openCamera(mWidth, mHeight, FRONT_FACE)
        }
    }


    private fun openCamera(width: Int, height: Int, type: Int) {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        mCameraId = if (type == FRONT_FACE) {
            isFrontFacing = true
            cameraManager.cameraIdList[1]
        } else {
            isFrontFacing = false
            cameraManager.cameraIdList[0]
        }
        val characteristics = cameraManager.getCameraCharacteristics(mCameraId)
        val facing = characteristics[CameraCharacteristics.LENS_FACING]
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            Log.d("chufei", "the facing is front-->$mCameraId")
        } else {
            Log.d("chufei", "the facing is back-->$mCameraId")
        }
        //管理摄像头支持所有输出的格式和尺寸
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        //获取预览尺寸
        mPreviewSize = Camera2Utils.calculateOptimalSize(map.getOutputSizes(SurfaceTexture::class.java), width, height)
        Log.d("chufei", "the perview size is-->${mPreviewSize?.width} , ${mPreviewSize?.height}")
        //获取拍照最大尺寸
        mCaptureSize = Collections.max(Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG))) { o1, o2 -> java.lang.Long.signum((o1.width * o1.height - o2.width * o2.height).toLong()) }
        Log.d("chufei", "the mCaptureSize size is-->${mCaptureSize?.width} , ${mCaptureSize?.height}")
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        //用于拍照
        setupImageReader()
        //用于录像
        mMediaRecorder = MediaRecorder()
        cameraManager.openCamera(mCameraId, mStateCallback, null)
    }


    private fun setupImageReader() {
        mImageReader = ImageReader.newInstance(mCaptureSize!!.width, mCaptureSize!!.height, ImageFormat.JPEG, 2)
        mImageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader?.acquireNextImage()
            val buffer = image!!.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            image?.close()
        }, mCameraHandler)
    }


    private fun hasPermissions(): Boolean {
        for (permission in VIDEO_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUDST_CODE) {
            for (permission in grantResults) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private lateinit var mCameraThread: RecordThread

    private lateinit var mCameraHandler: Handler

    private fun startRecordThread() {
        mRecordThread = RecordThread("record_thread")
        mRecordThread?.start()
        mRecordHandler = Handler(mRecordThread!!.looper)

        mCameraThread = RecordThread("camera_thread")
        mCameraThread.start()
        mCameraHandler = Handler(mCameraThread.looper)

    }

    private val zoom: Rect?=null

    private fun takePickture() {
        if (null == mCameraDevice || !camera_view.isAvailable || null == mCaptureSize) {
            return
        }
        val captureBuilder = mCameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        val rotation = windowManager.defaultDisplay.rotation
        captureBuilder?.addTarget(mImageReader!!.surface)

        if (isFrontFacing) {
            captureBuilder?.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION[Surface.ROTATION_180])
        } else {
            captureBuilder?.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION[rotation])
        }

        captureBuilder?.set(CaptureRequest.CONTROL_AF_TRIGGER,CameraMetadata.CONTROL_AF_TRIGGER_START)
//        builder?.set(CaptureRequest.SCALER_CROP_REGION,zoom)
        mPreviewSession.stopRepeating()
        //建立会话
        mPreviewSession.capture(captureBuilder!!.build(),object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                // 构建失能AF的请求
                mPerviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
                //闪光灯重置为未开启状态
                mPerviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                //继续开启预览
                mPreviewSession.setRepeatingRequest(mPerviewRequest, null, mCameraHandler)
            }
        },null)
    }

    companion object {
        //拍照方向
        private val ORIENTATION = SparseIntArray()
    }

    init {
        ORIENTATION.append(Surface.ROTATION_0, 90)
        ORIENTATION.append(Surface.ROTATION_90, 0)
        ORIENTATION.append(Surface.ROTATION_180, 270)
        ORIENTATION.append(Surface.ROTATION_270, 180)
    }
}