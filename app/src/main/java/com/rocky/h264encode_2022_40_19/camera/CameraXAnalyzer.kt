package com.rocky.h264encode_2022_40_19.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class CameraXAnalyzer(
    private val
    callback: (
        y: ByteArray,
        u: ByteArray,
        v: ByteArray, rowStride: Int,
    ) -> Unit,
) : ImageAnalysis.Analyzer {
    private var y: ByteArray? = null
    private var u: ByteArray? = null
    private var v: ByteArray? = null
    override fun analyze(image: ImageProxy) {
        val planes = image.planes
        //防止多次初始化数组
        if (null == y) {
            //初始化
            y = ByteArray(planes[0].buffer.limit() - planes[0].buffer.position())
            u = ByteArray(planes[1].buffer.limit() - planes[1].buffer.position())
            v = ByteArray(planes[2].buffer.limit() - planes[2].buffer.position())
        }
        if (planes[0].buffer.remaining() == y?.size) {
            planes[0].buffer.get(y!!)
            planes[1].buffer.get(u!!)
            planes[2].buffer.get(v!!)
        }

        callback.invoke(y!!, u!!, v!!, planes[0].rowStride)

        image.close()
    }

}