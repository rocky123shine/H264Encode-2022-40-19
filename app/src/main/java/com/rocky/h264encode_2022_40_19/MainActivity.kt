package com.rocky.h264encode_2022_40_19

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.rocky.h264encode_2022_40_19.databinding.ActivityMainBinding
import com.rocky.h264encode_2022_40_19.scree.ScreenShareEncode
import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat

import androidx.core.app.ActivityCompat.startActivityForResult
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions


/**
 * <pre>
 *     author : rocky
 *     time   : 2022/04/19
 * </pre>
 */
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        checkPermission()
        binding?.btnStart?.setOnClickListener {
            ScreenShareEncode.init(this).startScreenShare()
        }
        binding?.btnStop?.setOnClickListener {
            ScreenShareEncode.stopShare()
        }
        binding?.cameraX?.setOnClickListener {
            startActivity(Intent(this, CameraXActivity::class.java))
        }
    }

    private fun checkPermission() {
        XXPermissions.with(this)
            // 申请单个权限
            .permission(Permission.READ_EXTERNAL_STORAGE)
            // 申请多个权限
            .permission(Permission.WRITE_EXTERNAL_STORAGE)
            .permission(Permission.CAMERA)
            // 设置权限请求拦截器（局部设置）
            //.interceptor(new PermissionInterceptor())
            // 设置不触发错误检测机制（局部设置）
            //.unchecked()
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
                        XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                    } else {
                        //toast("获取录音和日历权限失败")
                    }
                }
            })

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ScreenShareEncode.onActivityResult?.invoke(requestCode, resultCode, data)
    }
}