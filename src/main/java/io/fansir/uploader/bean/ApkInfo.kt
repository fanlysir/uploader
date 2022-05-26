package io.fansir.uploader.bean

import io.fansir.uploader.util.Utils
import java.io.File

data class ApkInfo(
    val versionName : String,
    val versionCode : String,
    val apkFile : File,
    val appName : String = "",
    val packageName : String = "",
    val iconPath : String = "",
    val changeLog : String = "发现了新版本，快去升级一下吧~"
){
    fun checkApkFile(){
        if (!apkFile.exists() || !apkFile.isFile){
            Utils.stopTask("上传文件不存在！【${apkFile.absolutePath}】")
        }
    }
    fun checkIconFile(){
        val file = File(iconPath)
        if (!file.exists() || !file.isFile){
            Utils.stopTask("上传文件不存在！【${iconPath}】")
        }
    }
}