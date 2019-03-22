package com.xsrt.camerademo.camera.utils

import android.opengl.GLES20

class EasyGlUtil {
    companion object {
        fun bindFrameTexture(frameBufferId:Int,textureId:Int){
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferId)
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, textureId, 0)
        }

        fun unBindFrameBuffer() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }
    }
}