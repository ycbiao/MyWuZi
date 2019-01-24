package com.example.ycb.mywuzi;

import android.app.Activity
import android.app.Application;
import android.content.Intent
import cdc.sed.yff.AdManager
import cdc.sed.yff.nm.cm.ErrorCode
import cdc.sed.yff.nm.sp.SpotManager
import cdc.sed.yff.nm.sp.SpotRequestListener
import com.example.ycb.mywuzi.activity.MainActivity
import com.example.ycb.mywuzi.widget.LogUtil
import java.util.ArrayList

/**
 * Created by biao on 2019/1/17.
 */
class MyApplication : Application(){

    private lateinit var activityList: ArrayList<Activity>

    companion object {
        lateinit var mInstance : MyApplication
        fun getInstance(): MyApplication{
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        activityList = arrayListOf()
    }


    /**
     *
     * @param activity activity堆栈
     */
    fun addActivityIntoTask(activity: Activity?) {
        if (activity != null) {
            activityList.add(activity)
        }
    }

    /** 移除activity堆栈  */
    fun removeActivityFromTask(act: Activity) {
        try {
            if (activityList.contains(act)) {
                activityList.remove(act)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun getActivityList(): List<Activity> {
        return activityList
    }

}
