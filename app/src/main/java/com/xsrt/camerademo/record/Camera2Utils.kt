package com.xsrt.camerademo.record

import android.util.Log
import android.util.Size
import java.util.*

class Camera2Utils {
    companion object {
        fun calculateOptimalSize(sizes: Array<Size>, width: Int, height: Int): Size {
            var backSize: Size? = null
            val bigEnough = ArrayList<Size>()
            var reRatio = height.toFloat() / width
            Log.d("chufei", "the surface ratio = $reRatio , $width  ,$height")
            var perRatio = Int.MAX_VALUE.toFloat()
            for (size in sizes) {
                val ratio = size.width.toFloat() / size.height
                Log.d("chufei", "the camera ratio = $ratio , ${size.width}  ,${size.height}")
                if (ratio == reRatio) {
                    bigEnough.add(size)
                } else {
                    var defRatio = Math.abs(ratio - reRatio)
                    if (defRatio < perRatio) {
                        perRatio = defRatio
                        backSize = size
                    }
                }
            }
            if (bigEnough.size >= 0) {
                var defWidth = 0
                for (size in bigEnough) {
                    if (size.width > defWidth) {
                        backSize = size
                    }
                }

            }
            return backSize!!
        }
    }

}

class CompareSizeByArea : Comparator<Size> {
    override fun compare(lhs: Size, rhs: Size): Int {
        //确保乘法不会溢出范围
        return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
    }
}