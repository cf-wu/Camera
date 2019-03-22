package com.xsrt.camerademo.camera.filter

import android.content.res.Resources
import android.opengl.GLES20
import android.util.Log
import com.xsrt.camerademo.camera.utils.MatriUtil
import com.xsrt.camerademo.camera.utils.OpenGlUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

abstract class AFliter {
    private var mVerBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null
    private var mTextureId: Int = 0
    /**
     * 默认纹理贴图句柄
     */
    private var mHTexture: Int = 0
    private var textureType = 0      //默认使用Texture2D0
    /**
     * 程序句柄
     */
    private var mProgram: Int = 0
    /**
     * 顶点坐标句柄
     */
    private var mHPosition: Int = 0
    /**
     * 纹理坐标句柄
     */
    private var mHCoord: Int = 0
    /**
     * 总变换矩阵句柄
     */
    private var mHMatrix: Int = 0
    //顶点坐标
    private var pos = floatArrayOf(
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    )
    //文理坐标
    private var coord = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    )
    /**
     * 单位矩阵
     */
    val OM = MatriUtil.getOriginalMatrix()
    private var matrix = Arrays.copyOf(OM, 16)

    constructor() {
        initBuffer()
    }

    private fun initBuffer() {
        val a = ByteBuffer.allocateDirect(32)
        a.order(ByteOrder.nativeOrder())
        mVerBuffer = a.asFloatBuffer()
        mVerBuffer!!.put(pos)
        mVerBuffer!!.position(0)
        val b = ByteBuffer.allocateDirect(32)
        b.order(ByteOrder.nativeOrder())
        mTexBuffer = b.asFloatBuffer()
        mTexBuffer!!.put(coord)
        mTexBuffer!!.position(0)
    }

    abstract fun setFlag(cameraId: Int)
    abstract fun onCreate()

    fun create() {
        onCreate()
    }

    /**
     * 绑定默认纹理
     */
    private fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0.plus(textureType))
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId())
        GLES20.glUniform1i(mHTexture, textureType)
    }

    private fun getTextureId(): Int {
        return mTextureId
    }

    fun setTextureId(id: Int) {
        this.mTextureId = id
    }

    /**
     * 启动顶点坐标和纹理坐标进行绘制
     */
    fun onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer)
        GLES20.glEnableVertexAttribArray(mHCoord)
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mHCoord)
        GLES20.glDisableVertexAttribArray(mHPosition)
    }

    fun draw() {
        onClear()
        onUseProgram()
        onSetExpandData()
        onBindTexture()
        onDraw()
    }

    private fun onSetExpandData() {
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, matrix, 0)
    }

    private fun onUseProgram() {
        GLES20.glUseProgram(mProgram)
    }

    /**
     * 清除画布
     */
    private fun onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
    }

    /**
     * 直接通过句柄创建
     */
    fun createProgram(vertxt: String, fragment: String) {
        Log.e("tag", "onSurfaceCreate-->createProgram, $vertxt:$fragment ")
        mProgram = uCreateGlProgram(vertxt, fragment)
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoodrd")
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
    }

    /**
     * 通过资源文件创建share
     */
    fun createProgramByAssetsFile(vertex: String, fragment: String) {
        createProgram(OpenGlUtil.uRes(vertex), OpenGlUtil.uRes(fragment))
    }

    private fun uCreateGlProgram(vertxtS: String, fragmentS: String): Int {
        var vertex = uLoadShader(GLES20.GL_VERTEX_SHADER, vertxtS)
        if (vertex == 0) return 0
        val fragment = uLoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentS)
        if (fragment == 0) return 0
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertex)
            GLES20.glAttachShader(program, fragment)
            GLES20.glLinkProgram(program)
            val array = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, array, 0)
            if (array[0] != GLES20.GL_TRUE) {
                glError(1, "Coult not link program ${GLES20.glGetProgramInfoLog(program)}")
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    private fun glError(i: Int, s: String) {
        Log.e("tag", "gleError :code is  $i ----> $s")
    }

    /**
     * 加载shader
     */
    private fun uLoadShader(type: Int, source: String): Int {
        var shader = GLES20.glCreateShader(type)
        if (0 != shader) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val array = IntArray(1)
            GLES20.glGetProgramiv(shader, GLES20.GL_COMPILE_STATUS, array, 0)
            if (array[0] != GLES20.GL_TRUE) {
                glError(1, "Coult not link compile shader: $type}")
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }
}