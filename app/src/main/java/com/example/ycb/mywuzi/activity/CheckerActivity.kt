package com.example.ycb.mywuzi.activity

import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.View
import android.widget.SimpleAdapter
import cdc.sed.yff.listener.Interface_ActivityListener
import cdc.sed.yff.os.*
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.ai.AI
import com.example.ycb.mywuzi.ai.AICallBack
import com.example.ycb.mywuzi.util.Const.Companion.BLACK_WIN
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_RENJI
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_RENREN
import com.example.ycb.mywuzi.util.Const.Companion.NO_WIN
import com.example.ycb.mywuzi.util.Const.Companion.WHITE_WIN
import com.example.ycb.mywuzi.base.BaseActivity
import com.example.ycb.mywuzi.imp.OnGameStatusChangeListener
import com.example.ycb.mywuzi.util.Const
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_BLUE
import com.example.ycb.mywuzi.widget.LogUtil
import kotlinx.android.synthetic.main.checker_acticity.*
import java.util.*
import kotlin.concurrent.timerTask
import x.y.h.i
import android.system.Os.socket
import com.example.ycb.mywuzi.widget.WZQPanel
import java.lang.Exception
import kotlin.concurrent.thread


/**
 * Created by biao on 2019/1/24.
 * 棋盘
 */
class CheckerActivity : BaseActivity() , PointsChangeNotify, PointsEarnNotify {

    val INSTANCES = this

    private var model_type = 0;
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
//        initTime()
        initDialog()
        initGetParentIntent()
    }

    private fun initGetParentIntent() {
        model_type = intent.getIntExtra(MODEL_TYPE,0);
        id_wuziqi.setModel(model_type)
        when(model_type){
            MODEL_TYPE_RENREN ->{
                alertBuilder?.setPositiveButton("确认",
                    DialogInterface.OnClickListener { dialog, which ->
                        initTime()})
                alertBuilder?.setNegativeButton("取消",
                    DialogInterface.OnClickListener { dialog, which -> this@CheckerActivity.finish() })
                alertBuilder?.setCancelable(false)
                alertBuilder?.setTitle("准备完毕")
                alertDialog = alertBuilder?.create()
                alertDialog?.show()

                btn_retract_white.run {
                    setOnClickListener {
                        if (id_wuziqi.retractWhiteRenRen()) {
                            var isSuccess = PointsManager.getInstance(this@CheckerActivity).spendPoints(integralConsume);
                        }
                    }
                    text = "悔白棋(${integralConsume.toInt()}积分)"
                }
                btn_retract_black.run {
                    setOnClickListener {
                        if(id_wuziqi.retractBlackRenRen()){
                            var isSuccess = PointsManager.getInstance(this@CheckerActivity).spendPoints(integralConsume);
                        }
                    }
                    text = "悔黑棋(${integralConsume.toInt()}积分)"
                }
            }
            MODEL_TYPE_RENJI->{
                btn_retract_black.visibility = View.GONE
                alertBuilder?.setCancelable(false)
                alertBuilder?.setTitle("请选择等级")
                var keys = mutableListOf<MutableMap<String,String>>()
                val key1 = mutableMapOf<String,String>()
                key1["Name"] = "简单"
                keys.add(key1)
                val key2 = mutableMapOf<String,String>()
                key2["Name"] = "一般"
                keys.add(key2)
                val key3 = mutableMapOf<String,String>()
                key3["Name"] = "困难"
                keys.add(key3)

                var simpleAdapter = SimpleAdapter(this@CheckerActivity,keys,R.layout.degree_adapter, arrayOf("Name"),
                    intArrayOf(R.id.tv_degree_name)
                )
                alertBuilder.setAdapter(simpleAdapter,object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        tv_checker_degree.text = "比赛等级:${keys[which].get("Name").toString()}"
                        when(which){
                            0 ->{
                               id_wuziqi.getAi()?.setLevel(Const.LEVEL_LOW)
                            }
                            1 ->{
                                id_wuziqi.getAi()?.setLevel(Const.LEVEL_MIDIUM)
                            }
                            2 ->{
                                id_wuziqi.getAi()?.setLevel(Const.LEVEL_HIGH)
                            }
                        }
                        initTime()
                    }
                })
                alertDialog = alertBuilder?.create()
                alertDialog?.show()

                btn_retract_white.run {
                    setOnClickListener {
                        if (id_wuziqi.retractWhiteRenji()) {
                            var isSuccess = PointsManager.getInstance(this@CheckerActivity).spendPoints(integralConsume);
                        }
                    }
                    text = "悔白棋(${integralConsume.toInt()}积分)"
                }
            }
            MODEL_TYPE_BLUE->{
                Thread(object : Runnable{
                    override fun run() {
                        inPut()
                    }
                }).start()
                id_wuziqi.setOnBlueChessListener(object: WZQPanel.OnBlueChessListener() {
                    override fun onChess(s: String) {
                        outPut(s)
                    }
                })
                btn_retract_black.visibility = View.GONE
                id_wuziqi.setIsClient(bIsClient)
                btn_retract_white.run {
                    setOnClickListener {
                        alertBuilder?.setPositiveButton("确认",
                            DialogInterface.OnClickListener { dialog, which ->
                                var isSuccess = PointsManager.getInstance(this@CheckerActivity).spendPoints(integralConsume);
                                if(isSuccess){
                                    id_wuziqi.retractWhiteBlue()
                                }
                                else{
                                    showToast("对不起,您的积分不足,请先获取积分")
                                }
                            })
                        alertBuilder?.setNegativeButton("取消",
                            DialogInterface.OnClickListener { dialog, which -> this@CheckerActivity.finish() })
                        alertBuilder?.setCancelable(false)
                        alertBuilder?.setTitle("确定花费${integralConsume.toInt()}积分悔棋吗?")
                        alertDialog = alertBuilder?.create()
                        alertDialog?.show()
                    }
                    text = "悔白棋(${integralConsume.toInt()}积分)"

                }

                initTime()
            }
        }
    }

    private fun initDialog() {
        //游戏结束时弹出对话框
        alertBuilder = AlertDialog.Builder(this@CheckerActivity)
        id_wuziqi.setOnGameStatusChangeListener(object : OnGameStatusChangeListener() {
            override fun onGameOver(gameWinResult: Int) {
                when (gameWinResult) {
                    WHITE_WIN ->{
                        ++whiteWin
                        tv_white.text = "白子胜:$whiteWin"
                        alertBuilder?.setMessage("白棋胜利!")
                    }
                    BLACK_WIN -> {
                        ++blackWin
                        tv_black.text = "黑子胜:$blackWin"
                        alertBuilder?.setMessage("黑棋胜利!")
                    }
                    NO_WIN ->{
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
//                SimpleDateFormat("yyyy-MM-dd HH:mm:ss").let { tv_current_time.text = "当前时间：${it.format(Date())}" }
                tv_run_time.text = "用时：" + (String.format("%02d:%02d:%02d", jishitime[2], jishitime[1], jishitime[0]))
                if(id_wuziqi.getIsWhite()){
                    tv_checker_turning.text = Html.fromHtml("<font color = \"#ffffff\">" + "白方落子</font>")
                }else{
                    tv_checker_turning.text = Html.fromHtml("<font color = \"#333333\">" + "黑方落子</font>")
                }
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
        if(mSocket.isConnected){
            mSocket.close()
        }
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
        }else{
            super.onBackPressed()
        }
    }

    override fun onPointBalanceChange(p0: Float) {
        tv_checker_task.text = "我的积分${p0.toInt()} -->点击这里做任务赚积分"
    }

    override fun onPointEarn(p0: Context?, p1: EarnPointsOrderList?) {

    }

    companion object {
        lateinit var mSocket: BluetoothSocket
        var bIsClient: Boolean = true
        fun manageConnectedSocket(socket: BluetoothSocket,isClient: Boolean){
            mSocket = socket
            bIsClient = isClient
        }
    }

    fun inPut(){
        try {
            while (true){
                if(mSocket.isConnected){
                    val bytes = ByteArray(1024)
                    var n: Int
                    n = mSocket.inputStream.read(bytes)
                    if( n != -1) {
                        val b = String(bytes, 0, n, Charsets.UTF_8)
                        LogUtil.LogMsg(CheckerActivity.javaClass, "蓝牙服务器接收到数据$b")
                        id_wuziqi.post {
                            id_wuziqi.setChessbyBlue(b)
                        }
                    }
                }
            }
        }catch (ex:Exception){
            id_wuziqi.post{
                showToast("连接已断开")
                finish()
            }
        }
    }

    fun outPut(string: String){
//        LogUtil.LogMsg(this@CheckerActivity.javaClass,string)
        if(mSocket.isConnected){
        //指定发送的数据已经数据编码，编码统一，不然会乱码
            mSocket.outputStream.write(string.toByteArray());
            mSocket.outputStream.flush();
        }
    }
}




