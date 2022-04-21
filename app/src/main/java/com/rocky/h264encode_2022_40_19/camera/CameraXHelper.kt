package com.rocky.h264encode_2022_40_19.camera

import android.content.Context
import android.media.MediaCodecInfo
import android.util.Log
import android.util.Size

import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.rocky.h264encode_2022_40_19.encode.H264EncodeThread
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import com.rocky.h264encode_2022_40_19.utils.ImageUtil


/**
 * <pre>
 *     author : rocky
 *     time   : 2022/04/20
 * </pre>
 */
object CameraXHelper {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var mPreviewSize: Size

    /**
     * @param lifecycleOwner 需要绑定生命周期
     * @param context 上下文
     * @param preview PreviewView 布局中PreviewView  和Preview绑定
     */
    fun init(
        lifecycleOwner: LifecycleOwner,
        context: Context,
        preview: PreviewView,
        mPreviewSize: Size,
    ): CameraXHelper {
        cameraExecutor = Executors.newSingleThreadExecutor()
        this.mPreviewSize = mPreviewSize
        //可以绑定lifecycle
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener(
            {
                //这里是runnable
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                //绑定生命周期 在绑定之前 解绑其他的
                cameraProvider.unbindAll()
                try {
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,//lifecycleOwner
                        cameraSelector,//前后摄像头
                        getPreview(preview),
                        getAnalysis()//useCase 这里有四种实现类 Preview ImageAnalysis VideoCapture  VideoCaptureLegacy
                    )
                } catch (e: Exception) {
                    Log.e("CameraXHelper", "Use case binding failed", e)
                }

            },
            ContextCompat.getMainExecutor(context))
        return this
    }

    //实现预览
    private fun getPreview(mPreview: PreviewView): Preview {
        // 分辨率并不是最终的分辨率，CameraX会自动根据设备的支持情况，结合你的参数，设置一个最为接近的分辨率
        return Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(mPreview.surfaceProvider)
            }

    }

    //拿到数据 进行分析 和处理

    private var nv21: ByteArray? = null
    private var nv21_rotated: ByteArray? = null
    private var nv12: ByteArray? = null
    private var h264EncodeThread: H264EncodeThread? = null
    private fun getAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, CameraXAnalyzer { y, u, v, rowStride ->
                    if (null == nv21) {
                        nv21 = ByteArray(rowStride * mPreviewSize.height * 3 / 2)
                        nv21_rotated = ByteArray(rowStride * mPreviewSize.height * 3 / 2)
                    }



                    ImageUtil.yuvToNv21(y, u, v, nv21, rowStride, mPreviewSize.height)
//                    //对数据进行旋转   90度
                    ImageUtil.nv21_rotate_to_90(nv21, nv21_rotated, rowStride, mPreviewSize.height)
                    //Nv12     yuv420
                    val temp: ByteArray = ImageUtil.nv21toNV12(nv21_rotated, nv12)
                    //进行编码
                    if (null == h264EncodeThread) {
                        h264EncodeThread = H264EncodeThread(WIDTH = mPreviewSize.width,
                            HEIGHT = mPreviewSize.height,
                            encode = { temp },
                            colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
                        )
                        h264EncodeThread?.startEncode()
                    }
                    h264EncodeThread?.encode = { temp }

                })
            }

    }


    fun stop() {
        cameraExecutor.shutdown()
        h264EncodeThread?.stopEncode()
    }

}

