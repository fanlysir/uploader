package io.fansir.uploader.bean

import io.fansir.uploader.util.Utils
import java.io.File

data class ApkInfo(
    val versionName : String,
    val versionCode : String,
    val changeLog : String = "",
    val apkFile : File
){
    fun checkApkFile(){
        if (!apkFile.exists() || !apkFile.isFile){
            Utils.stopTask("上传文件不存在！【${apkFile.absolutePath}】")
        }
    }
}