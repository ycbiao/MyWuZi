package com.example.ycb.mywuzi.activity

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import cdc.sed.yff.listener.Interface_ActivityListener
import cdc.sed.yff.os.*
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.base.BaseActivity
import com.example.ycb.mywuzi.imp.OnGameStatusChangeListener
import com.example.ycb.mywuzi.util.UpdateHelper
import com.example.ycb.mywuzi.widget.WZQPanel
import kotlinx.android.synthetic.main.checker_acticity.*
import kotlinx.android.synthetic.main.main_activity.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

/**
 * Created by biao on 2019/1/24.
 * 棋盘
 */
class CheckerActivity : BaseActivity() , PointsChangeNotify, PointsEarnNotify {
    private lateinit var alertBuilder: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog
    private var whiteWin = 0
    private var blackWin = 0
    private var draw = 0 //平局
    private var jishitime = intArrayOf(0, 0, 0, 0)//秒，分，时，总
    private var timer : Timer? = null
    private var integralConsume = 1f // 一次悔棋消费积分

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //设置沉浸式标题栏
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            window.addFlags(
//                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//            )
//            window.addFlags(
//                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
//            )
//        }

        setContentView(R.layout.checker_acticity)
        initAd()
        initTime()
        initDialog()
        initListener()
    }

    private fun initListener() {
        btn_retract_white.run {
            setOnClickListener {
                if (id_wuziqi.retractWhite()) {
                    var isSuccess = PointsManager.getInstance(this@CheckerActivity).spendPoints(integralConsume);
                }
            }
            text = "悔白棋(${integralConsume.toInt()}积分)"
        }
        btn_retract_black.run {
            setOnClickListener {
                if(id_wuziqi.retractBlack()){
                    var isSuccess = PointsManager.getInstance(this@CheckerActivity).spendPoints(integralConsume);
                }
            }
            text = "悔黑棋(${integralConsume.toInt()}积分)"
        }
    }

    private fun initDialog() {
        //游戏结束时弹出对话框
        alertBuilder = AlertDialog.Builder(this@CheckerActivity)
        id_wuziqi.setOnGameStatusChangeListener(object : OnGameStatusChangeListener() {
            override fun onGameOver(gameWinResult: Int) {
                when (gameWinResult) {
                    WZQPanel.WHITE_WIN ->{
                        ++whiteWin
                        tv_white.text = "白子胜:$whiteWin"
                        alertBuilder?.setMessage("白棋胜利!")
                    }
                    WZQPanel.BLACK_WIN -> {
                        ++blackWin
                        tv_black.text = "黑子胜:$blackWin"
                        alertBuilder?.setMessage("黑棋胜利!")
                    }
                    WZQPanel.NO_WIN ->{
                        ++draw
                        tv_checker_draw.text = "和棋：$draw"
                        alertBuilder?.setMessage("和棋!")
                    }
                }
                alertBuilder?.setPositiveButton("再来一局",
                    DialogInterface.OnClickListener { dialog, which -> id_wuziqi?.restartGame()
                        initTime()})
                alertBuilder?.setNegativeButton("退出游戏",
                    DialogInterface.OnClickListener { dialog, which -> this@CheckerActivity.finish() })
                alertBuilder?.setCancelable(false)
                alertBuilder?.setTitle("此局结束")
                alertDialog = alertBuilder?.create()
                alertDialog?.show()
                timer?.cancel()
                timer = null
                jishitime = intArrayOf(0, 0, 0, 0)
            }
        })

    }

    private fun initTime() {
        timer = Timer()
        timer?.schedule(timerTask {
            jishitime[0]++
            jishitime[3]++
            if (jishitime[0] == 60) {
                jishitime[1]++
                jishitime[0] = 0
            }
            if (jishitime[1] == 60) {
                jishitime[2]++
                jishitime[1] = 0
            }
            if (jishitime[2] == 24) {
                jishitime[2] = 0
            }
            tv_run_time.post {
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").let { tv_current_time.text = "当前时间：${it.format(Date())}" }
                tv_run_time.text = "比赛用时：" + (String.format("%02d:%02d:%02d", jishitime[2], jishitime[1], jishitime[0]))
            }
        }, 1000, 1000)
    }


    private fun initAd() {
        tv_checker_task.setOnClickListener {
            initOffersWall()
            var isSuccess = PointsManager.getInstance(this).awardPoints(1000f);
        }
        var pointsBalance = PointsManager.getInstance(this).queryPoints();
        tv_checker_task.text = "我的积分${pointsBalance.toInt()} -->点击这里做任务赚积分"
        PointsManager.getInstance(this).registerNotify(this);
    }


    private fun initOffersWall(){
        OffersManager.getInstance(this@CheckerActivity).onAppLaunch();
        OffersManager.getInstance(this@CheckerActivity).showOffersWall(object : Interface_ActivityListener {
            override fun onActivityDestroy(p0: Context?) {

            }
        });
        // 设置积分墙列表页标题文字
        OffersBrowserConfig.getInstance(this).setBrowserTitleText("做任务赚积分换零钱");
        // 设置积分墙标题背景颜色
        OffersBrowserConfig.getInstance(this).setBrowserTitleBackgroundColor(resources.getColor(R.color.colorPrimary));
        PointsManager.getInstance(this).registerNotify(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        PointsManager.getInstance(this).unRegisterNotify(this)
    }

    override fun onBackPressed() {
        if(timer != null){
            alertBuilder?.setMessage("")
            alertBuilder?.setPositiveButton("确认",
                DialogInterface.OnClickListener { dialog, which -> super.onBackPressed()
                   })
            alertBuilder?.setNegativeButton("取消",
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
            alertBuilder?.setCancelable(false)
            alertBuilder?.setTitle("正在进行比赛，确认退出？")
            alertBuilder.show()
        }
    }

    override fun onPointBalanceChange(p0: Float) {
        tv_checker_task.text = "我的积分${p0.toInt()} -->点击这里做任务赚积分"
    }

    override fun onPointEarn(p0: Context?, p1: EarnPointsOrderList?) {

    }
}