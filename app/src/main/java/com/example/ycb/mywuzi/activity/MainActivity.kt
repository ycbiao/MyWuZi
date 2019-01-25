package com.example.ycb.mywuzi.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import cdc.sed.yff.os.PointsManager
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.base.BaseActivity
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_RENJI
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_RENREN
import com.example.ycb.mywuzi.util.UpdateHelper
import kotlinx.android.synthetic.main.checker_acticity.*
import kotlinx.android.synthetic.main.main_activity.*


class MainActivity : BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置沉浸式标题栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            )
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
        }
        init()
    }

    private fun init() {
        setContentView(R.layout.main_activity)
        btn_pp.setOnClickListener {  //人人
            startActivity(Intent(this@MainActivity,CheckerActivity::class.java).putExtra(MODEL_TYPE,MODEL_TYPE_RENREN))
             }
        btn_pm.setOnClickListener {//人机
            startActivity(Intent(this@MainActivity,CheckerActivity::class.java).putExtra(MODEL_TYPE,MODEL_TYPE_RENJI))
        }
        btn_blue.setOnClickListener { //蓝牙
             }
        btn_rank.setOnClickListener {
            //排行版
        }
//        var pointsBalance = PointsManager.getInstance(this).queryPoints();
//        tv_integral.text = "已有积分${pointsBalance.toInt()}(积分可用于悔棋)"

        //新版本更新
        var updateHelper =  UpdateHelper(this)
        updateHelper.execute()
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }
}
