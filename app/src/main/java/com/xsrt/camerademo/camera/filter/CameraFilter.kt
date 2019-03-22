package com.xsrt.camerademo.camera.filter

import android.content.res.Resources

class CameraFilter(resources: Resources?) : AFliter() {
    override fun setFlag(cameraId: Int) {
    }

    override fun onCreate() {
        createProgramByAssetsFile("shader/oes_base_vertex.sh", "shader/oes_base_fragment.sh")
    }

}