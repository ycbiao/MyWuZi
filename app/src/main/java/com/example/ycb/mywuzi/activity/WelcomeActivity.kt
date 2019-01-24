package com.example.ycb.mywuzi.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import cdc.sed.yff.AdManager
import cdc.sed.yff.nm.cm.ErrorCode
import cdc.sed.yff.nm.sp.SpotManager
import cdc.sed.yff.nm.sp.SpotRequestListener
import cdc.sed.yff.nm.sp.SplashViewSettings
import cdc.sed.yff.nm.sp.SpotListener
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.util.PermissionHelper
import com.example.ycb.mywuzi.base.BaseActivity
import com.example.ycb.mywuzi.widget.LogUtil
import kotlinx.android.synthetic.main.welcome_acticity.*


/**
 * Created by biao on 2019/1/22.
 */
class WelcomeActivity: BaseActivity(){
    private lateinit var mPermissionHelper: PermissionHelper

    override fun onDestroy() {
        super.onDestroy()
        // 开屏展示界面的 onDestroy() 回调方法中调用
//        SpotManager.getInstance(this).onDestroy();
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.setIsLog(true)
        // 设置全屏
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        // 移除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.welcome_acticity)
        // 当系统为6.0以上时，需要申请权限
        mPermissionHelper = PermissionHelper(this)
        mPermissionHelper.setOnApplyPermissionListener(object : PermissionHelper.OnApplyPermissionListener {
            override fun onAfterApplyAllPermission() {
                LogUtil.LogMsg(this@WelcomeActivity.javaClass, "All of requested permissions has been granted, so run app logic.")
                initAD()
            }
        })
        if (Build.VERSION.SDK_INT < 23) {
            // 如果系统版本低于23，直接跑应用的逻辑
            LogUtil.LogMsg(this@WelcomeActivity.javaClass, "The api level of system is lower than 23, so run app logic directly.")
            initAD()
        } else {
            // 如果权限全部申请了，那就直接跑应用逻辑
            if (mPermissionHelper.isAllRequestedPermissionGranted()) {
                LogUtil.LogMsg(this@WelcomeActivity.javaClass, "All of requested permissions has been granted, so run app logic directly.")
                initAD()
            } else {
                // 如果还有权限为申请，而且系统版本大于23，执行申请权限逻辑
                LogUtil.LogMsg(this@WelcomeActivity.javaClass, "Some of requested permissions hasn't been granted, so apply permissions first.")
                mPermissionHelper.applyPermissions()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPermissionHelper.onActivityResult(requestCode, resultCode, data)
    }


    private fun initAD() {
        AdManager.getInstance(this).init("d3e43e6a7d8f9f7c","8a63fbd2bf248526", false);
        SpotManager.getInstance(this).requestSpot(object : SpotRequestListener {
            override fun onRequestFailed(p0: Int) {
                LogUtil.LogMsg(this.javaClass,"请求插屏广告失败")
                when (p0) {
                    ErrorCode.NON_NETWORK ->  LogUtil.LogMsg(this.javaClass,"网络异常")
                    ErrorCode.NON_AD ->  LogUtil.LogMsg(this.javaClass,"暂无插屏广告")
                    else ->  LogUtil.LogMsg(this.javaClass,"请稍后再试")
                }
                startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                finish()
            }

            override fun onRequestSuccess() {
                LogUtil.LogMsg(this@WelcomeActivity.javaClass,"请求插屏广告成功")
                val splashViewSettings = SplashViewSettings()
                splashViewSettings.setAutoJumpToTargetWhenShowFailed(true);
                splashViewSettings.setTargetClass(MainActivity::class.java);
                splashViewSettings.setSplashViewContainer(welcome_ctl);
                SpotManager.getInstance(this@WelcomeActivity).showSplash(this@WelcomeActivity,
                    splashViewSettings, object: SpotListener {
                        override fun onSpotClicked(p0: Boolean) {

                        }

                        override fun onShowSuccess() {
                            LogUtil.LogMsg(this@WelcomeActivity.javaClass,"onShowSuccess")
                        }

                        override fun onShowFailed(p0: Int) {
                            LogUtil.LogMsg(this@WelcomeActivity.javaClass,"开屏展示失败")
                            when (p0) {
                                ErrorCode.NON_NETWORK -> LogUtil.LogMsg(this@WelcomeActivity.javaClass,"网络异常")
                                ErrorCode.NON_AD -> LogUtil.LogMsg(this@WelcomeActivity.javaClass,"暂无开屏广告")
                                ErrorCode.RESOURCE_NOT_READY -> LogUtil.LogMsg(this@WelcomeActivity.javaClass,"开屏资源还没准备好")
                                ErrorCode.SHOW_INTERVAL_LIMITED -> LogUtil.LogMsg(this@WelcomeActivity.javaClass,"开屏展示间隔限制")
                                ErrorCode.WIDGET_NOT_IN_VISIBILITY_STATE -> LogUtil.LogMsg(this@WelcomeActivity.javaClass,"开屏控件处在不可见状态")
                                else -> LogUtil.LogMsg(this@WelcomeActivity.javaClass,"errorCode: %d $p0")
                            }
                        }

                        override fun onSpotClosed() {
//                    startActivity(Intent(this@WelcomeActivity, MainActivity::class.java))
                        }
                    });
//
            }
        });
    }
}