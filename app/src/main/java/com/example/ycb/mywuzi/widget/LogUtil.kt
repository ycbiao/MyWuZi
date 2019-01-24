package com.example.ycb.mywuzi.widget

import android.content.Intent
import android.util.Log
import org.jetbrains.annotations.Nullable
import java.lang.Exception
import java.util.*
import kotlin.reflect.KClass

/**
 * Created by biao on 2019/1/17.
 */
class LogUtil {
    companion object {
        private var isLog = false
        fun setIsLog(b: Boolean) {
            isLog = b
        }
        fun <T :Any>LogMsg(cls: Class<out T>?, msg :String?){
            if (!isLog)
                return

            try {
                var className :String? = "LogUtil"
                if (cls != null) {
                    className = cls.simpleName
                }
                if(msg != null){
                    print("\u001B[42;30m #################################################### \u001B[0m\n")
                    Log.i(className, "－－－－－－－－－－－－－－－－－－－－$className  MSG  －－－－－－－－－－－－－－－－－－－－")
                    Log.i(className, "| \n")
                    val msgLines = msg.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (oneLine in msgLines) {
                        Log.i(className, "| $oneLine")
                    }
                    Log.i(className, "| \n")
                    Log.i(className, "－－－－－－－－－－－－－－－－－－－－$className  MSG END  －－－－－－－－－－－－－－－－－－－－")
//                System.out.print("####################################################\n");
                    print("\u001B[42;30m #################################################### \u001B[0m\n")
                }else{
                    Log.i(className, "$className is log a message!")
                }

            }catch (e :Exception){
                Log.e(LogUtil::class.java.simpleName, e.message)
            }
        }
    }
}