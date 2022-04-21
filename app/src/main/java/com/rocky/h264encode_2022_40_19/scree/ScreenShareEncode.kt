package com.rocky.h264encode_2022_40_19.scree

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import com.rocky.h264encode_2022_40_19.encode.H264EncodeThread
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

/**
 * <pre>
 *     author : rocky
 *     time   : 2022/04/19
 * </pre>
 */
object ScreenShareEncode {

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var contextRef: WeakReference<Context>? = null
    const val REQUEST_CODE = 0X1
    private var mediaProjection: MediaProjection? = null
    private var h264EncodeThread: H264EncodeThread? = null

    fun init(context: Context): ScreenShareEncode {
        mediaProjectionManager =
            context.getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        contextRef = WeakReference<Context>(context)
        //初始化之后 设置 请求回调 当回调被执行后 可进一步操作
        onActivityResult = { requestCode, resultCode, data ->
            if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
                //此时初始化projection
                mediaProjectionManager?.apply {
                    data ?: return@apply
                    mediaProjection = getMediaProjection(resultCode, data)
                    //初始化编码器  开始编码
                    //在这个里有个兼容的坑  宽高设置不能超过手机支持的最大分辨率 否则 mediaCodec配置会直接报错
                    val dm = context.resources.displayMetrics

                    val width = dm.widthPixels * 2 / 3
                    val height = dm.heightPixels * 2 / 3
                    h264EncodeThread = H264EncodeThread(
                        width, height,
                        bindSurface = { surface ->
                            //创建数据接收地 类似于一个容器
                            mediaProjection?.createVirtualDisplay(
                                "screen-h264", //容器的名字
                                width, height,
                                dm.densityDpi,//像素密度  数组越大 越清晰
                                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,//设置共享的
                                surface,
                                null,//displayCallback,
                                null//这里可以设置callback的消息处理线程，为null则在主线程
                            )
                        }
                    )

                    h264EncodeThread?.startEncode()
                    h264EncodeThread?.onException = {
                        //异常回调
                        stopShare()
                    }
                }
            }
        }

        return this
    }

    fun startScreenShare(): ScreenShareEncode {
        if (null == contextRef) {
            throw IllegalStateException("请先调用init方法以便完成初始化")
        }
        mediaProjectionManager?.apply {
            //创建一个intent 开启录屏服务
            createScreenCaptureIntent()
                .also {
                    contextRef?.get()?.apply {
                        try {
                            val activity = this as Activity
                            startActivityForResult(it, REQUEST_CODE)
                        } catch (e: Exception) {
                            throw IllegalStateException("context 必须为activity")
                        }

                    }

                }


        }
        return this
    }

    var onActivityResult: ((Int, Int, Intent?) -> Unit)? = null

    fun stopShare() {
        mediaProjection?.stop()
        h264EncodeThread?.stopEncode()

    }

    //在这里可以处理VirtualDisplay 状态的改变
    private val displayCallback = object : VirtualDisplay.Callback() {
        override fun onPaused() {
            super.onPaused()
        }

        override fun onResumed() {
            super.onResumed()
        }

        override fun onStopped() {
            super.onStopped()
        }
    }
}