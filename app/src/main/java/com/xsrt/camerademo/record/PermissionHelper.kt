package com.xsrt.camerademo.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import com.tbruyelle.rxpermissions2.RxPermissions

class PermissionHelper {
    private var mRxPermissions: RxPermissions

    constructor(activity: Activity) {
        mRxPermissions = RxPermissions(activity)
    }

    /**
     * 拍照需要的权限
     */
    val CAMERA_REQUEST = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @SuppressLint("CheckResult")
    fun requestRcordPermissions(permissions: Array<String>) {
        for (permission in permissions) {
            mRxPermissions.request(permission)
        }
    }

    fun checkPermission(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            val granted = mRxPermissions.isGranted(permission)
            if (!granted) return false
        }
        return true

    }
}