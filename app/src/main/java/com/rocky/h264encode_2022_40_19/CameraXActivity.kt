package com.rocky.h264encode_2022_40_19

import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.rocky.h264encode_2022_40_19.camera.CameraXHelper
import com.rocky.h264encode_2022_40_19.databinding.ActivityCameraxBinding

/**
 * <pre>
 *     author : rocky
 *     time   : 2022/04/20
 * </pre>
 */
class CameraXActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        check()
        CameraXHelper.init(this, this,binding.viewFinder, Size(720,1080))

        binding.imageCaptureButton.setOnClickListener {

        }
        binding.verticalCenterline.setOnClickListener {

        }

    }

    private fun check() {
        XXPermissions.with(this)
            // 申请单个权限
            .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .permission(Permission.RECORD_AUDIO)
            .permission(Permission.CAMERA)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                    if (all) {
                        //toast("获取录音和日历权限成功")
                    } else {
                        // toast("获取部分权限成功，但部分权限未正常授予")
                    }
                }

                override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                    if (never) {
                        // toast("被永久拒绝授权，请手动授予录音和日历权限")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(this@CameraXActivity, permissions)
                    } else {
                        //toast("获取录音和日历权限失败")
                    }
                }
            })
    }

    override fun onStop() {
        super.onStop()

        CameraXHelper.stop()
    }
}