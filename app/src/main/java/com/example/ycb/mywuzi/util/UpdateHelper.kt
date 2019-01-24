package com.example.ycb.mywuzi.util

import android.content.Context
import android.os.AsyncTask
import cdc.sed.yff.update.AppUpdateInfo
import cdc.sed.yff.AdManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.support.v4.content.FileProvider
import android.widget.Toast
import com.example.ycb.mywuzi.R
import java.io.File

/**
 * Created by biao on 2019/1/22.
 */
class UpdateHelper(val mContext: Context) : AsyncTask<Void, Void, AppUpdateInfo>() {

    override fun doInBackground(vararg params: Void?): AppUpdateInfo?{
        try {
            // 在 doInBackground 中调用 AdManager 的 checkAppUpdate 即可从有米服务器获得应用更新信息。
            return AdManager.getInstance(mContext).syncCheckAppUpdate()
            // 此方法务必在非 UI 线程调用，否则有可能不成功。
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: AppUpdateInfo?) {
        super.onPostExecute(result)
        try {
            if (result == null || result.url == null) {
//                 如果 AppUpdateInfo 为 null 或它的 url 属性为 null，则可以判断为没有新版本。
                Toast.makeText(mContext, "当前版本已经是最新版", Toast.LENGTH_SHORT).show()
                return
            }

            // 这里简单示例使用一个对话框来显示更新信息
            AlertDialog.Builder(mContext)
                .setTitle("发现新版本")
                .setMessage(result.updateTips) // 这里是版本更新信息
                .setNegativeButton("马上升级",
                    DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        mContext.startActivity(intent)
                        // ps：这里示例点击“马上升级”按钮之后简单地调用系统浏览器进行新版本的下载，
                        // 但强烈建议开发者实现自己的下载管理流程，这样可以获得更好的用户体验。
                    })
                .setPositiveButton("下次再说", DialogInterface.OnClickListener { dialog, which -> dialog.cancel() }).create().show()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

//    fun downLoad(context: Context,string: String){
//        var downloadDialog = DownloadDialog(context)
//        downloadDialog.show()
//        val result = StringBuilder()
//        val downloadUrl = string
//        val AppName = context.resources.getString(R.string.app_name)
//        var appMainPath = Environment.getExternalStorageDirectory().path + File.separator + AppName + File.separator
//        var apkDownLoadPath = appMainPath + "apk" + File.separator
//        val fileName: String
//        val index = downloadUrl.lastIndexOf("/")
//        if (index > 0) {
//            fileName = downloadUrl.substring(index + 1)
//        } else {
//            fileName = AppName + "_" + System.currentTimeMillis()
//        }
//        // 删除旧文件
//        deleteFile(apkDownLoadPath + fileName)
//
//        //下载安装包
//        val downloadHelper = HttpHelper()
//        downloadHelper.simpleDownload(
//            downloadUrl, apkDownLoadPath,
//            fileName,
//            false,
//            false,
//            3,
//            object : HttpHelper.onDownloadStatueListener {
//                override fun onDownloading(total: Int, current: Int) {
//                    downloadDialog.setProcess(current/total * 100)
//                    if(current == total){
//                        downloadDialog.dismiss()
//                        //打开APK程序代码
//                        val intent = Intent()
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        intent.action = Intent.ACTION_VIEW
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)//7.0适配需要给外部应用赋予此权限以获取传入的URI
//                            intent.setDataAndType(
//                                FileProvider.getUriForFile(mContext, "cn.ysbang.ysbgy", File(result.toString())),
//                                "application/vnd.android.package-archive"
//                            )
//                        } else {
//                            intent.setDataAndType(Uri.fromFile(File(result.toString())), "application/vnd.android.package-archive")
//                        }
//                        mContext.startActivity(intent)
//                    }
//                }
//
//                override fun onDownloadFinished(path: String) {
//                    result.append(path)
//                }
//
//                override fun onDownloadError(error: String) {
//
//                }
//            })
//    }
//
//    fun deleteFile(fileName: String): Boolean {
//        try {
//            val file = File(fileName)
//            if (file.isFile && file.exists()) {
//                file.delete()
//                println("删除单个文件" + fileName + "成功！")
//                return true
//            } else {
//                println("删除单个文件" + fileName + "失败！")
//                return false
//            }
//        } catch (var2: Exception) {
//            return false
//        }

//    }

}