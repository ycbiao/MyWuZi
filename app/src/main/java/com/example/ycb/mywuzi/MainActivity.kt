package com.example.ycb.mywuzi

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.example.ycb.mywuzi.widget.WZQPanel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var alertBuilder: AlertDialog.Builder
    private lateinit var alertDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //游戏结束时弹出对话框
        alertBuilder = AlertDialog.Builder(this@MainActivity)
        alertBuilder?.setPositiveButton("再来一局",
            DialogInterface.OnClickListener { dialog, which -> id_wuziqi?.restartGame() })
        alertBuilder?.setNegativeButton("退出游戏",
            DialogInterface.OnClickListener { dialog, which -> this@MainActivity.finish() })
        alertBuilder?.setCancelable(false)
        alertBuilder?.setTitle("此局结束")

        id_wuziqi.setOnGameStatusChangeListener(object : OnGameStatusChangeListener() {
            override fun onGameOver(gameWinResult: Int) {
                when (gameWinResult) {
                    WZQPanel.WHITE_WIN -> alertBuilder?.setMessage("白棋胜利!")
                    WZQPanel.BLACK_WIN -> alertBuilder?.setMessage("黑棋胜利!")
                    WZQPanel.NO_WIN -> alertBuilder?.setMessage("和棋!")
                }
                alertDialog = alertBuilder?.create()
                alertDialog?.show()
            }
        })

        initAD()
    }

    private fun initAD() {
        MobileAds.initialize(this, getString(R.string.ADMOB_APP_ID));

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
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
