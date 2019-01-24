package com.example.ycb.mywuzi.base

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import cdc.sed.yff.nm.sp.SpotManager
import com.example.ycb.mywuzi.MyApplication

/**
 * Created by biao on 2019/1/22.
 */
open class BaseActivity : AppCompatActivity() {

    fun showToast(string: String){
        Toast.makeText(this,string,Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyApplication.getInstance().addActivityIntoTask(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        MyApplication.getInstance().removeActivityFromTask(this)
        Log.v("TITAndroid", "$localClassName goodbye")

    }

    override fun finish() {
        MyApplication.getInstance().removeActivityFromTask(this)

        if(MyApplication.getInstance().getActivityList().size == 1){
            SpotManager.getInstance(this).onDestroy();
            SpotManager.getInstance(this).onAppExit();
        }
        super.finish()
    }
}