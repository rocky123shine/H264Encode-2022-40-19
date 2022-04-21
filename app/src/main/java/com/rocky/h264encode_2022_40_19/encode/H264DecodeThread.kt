package com.rocky.h264encode_2022_40_19.encode

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Environment
import android.view.Surface
import com.rocky.h264encode_2022_40_19.utils.FileUtils
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.ByteBuffer

/**
 * <pre>
 *     author : rocky
 *     time   : 2022/04/19
 *     des    : Mediacodec 和mediaProjection 天生就匹配
 *              内部 通过Mediacodec提供的surface 绑定数据通道
 *              mediaProjection 录屏拿到数据之后就会自动传给Mediacodec进行编码
 *              所以 可以直接拿到编码好的数据
 *              因此 要根据数据源的不同 做出相应的兼容
 *              如果是摄像头数据传输进来 那就需要自己手动编码
 * </pre>
 */
class H264EncodeThread(
    private val WIDTH: Int = 720,
    private val HEIGHT: Int = 1080,
    var encode: (() -> ByteArray)? = null,
    private val bindSurface: ((Surface) -> Unit)? = null,
    private val colorFormat: Int = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface,
) : Thread(("encode-h264")) {
    private val mediaCodec: MediaCodec =
        MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)

    init {
        initMediaCodec()
    }

    //初始化编码器
    private fun initMediaCodec() {
        //此时是用户的编码
        val mediaFormat =
            MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,//和编码type一致
                WIDTH,
                HEIGHT)
        //设置 关键帧  码率等 帧率
        //帧率 每秒20帧
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 20)

        //i帧之间间隔的
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 30)
        //码率  describing the average bitrate in bits/sec. The associated value is an integer
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, WIDTH * HEIGHT)
        //重要
        mediaFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            colorFormat
        )

        //配置编码器
        mediaCodec.configure(
            mediaFormat,//配置参数
            null,//编码 不是输出 不要要surface
            null,//这里可处理加密
            MediaCodec.CONFIGURE_FLAG_ENCODE //这里是编码
        )

        bindSurface?.let {
            //录屏和编码关联
            val surface = mediaCodec.createInputSurface()
            it(surface)
        }
    }

    @Volatile
    private var isStop = true

    override fun run() {
        super.run()
        mediaCodec.start()
        //这里多线程 使用外部变量终端线程 可以多做考虑  这里就简单处理了
        val info: MediaCodec.BufferInfo = MediaCodec.BufferInfo()
        try {
            while (!isStop) {

                encode?.apply {
                    val temp = invoke()
                    val inIndex = mediaCodec.dequeueInputBuffer(100_000)
                    if (inIndex >= 0) {
                        val byteBuffer: ByteBuffer? = mediaCodec.getInputBuffer(inIndex)
                        byteBuffer?.clear()
                        byteBuffer?.put(temp)
                        mediaCodec.queueInputBuffer(
                            inIndex,
                            0,
                            temp.size,
                            System.nanoTime() / 1000,
                            0)
                    }
                }


                var outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
                while (outIndex >= 0) {
                    //拿到可用的buffer
                    val outtBuffer = mediaCodec.getOutputBuffer(outIndex)
                    //读取编码好的数据
                    val data = ByteArray(info.size)
                    outtBuffer?.get(data)
//                     to file
                    FileUtils.writeBytes(data)
                    FileUtils.writeContent(data)
                    //使用之后释放
                    mediaCodec.releaseOutputBuffer(
                        outIndex,
                        false//如果有渲染显示目标这需要传true
                    )
                    outIndex = mediaCodec.dequeueOutputBuffer(info, 100_000)
                }

            }
        } catch (e: Exception) {
            //中断异常 也在这里处理
            e.printStackTrace()
            onException?.apply {
                invoke(e.message ?: "")
            }
        } finally {
            isStop = true
            mediaCodec.stop()
            mediaCodec.release()

        }
    }

    fun startEncode() {
        isStop = false
        start()//启动线程
    }

    fun stopEncode() {
        isStop = true
    }

    var onException: ((String) -> Unit)? = null

}