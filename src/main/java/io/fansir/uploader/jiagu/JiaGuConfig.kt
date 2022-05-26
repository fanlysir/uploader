package io.fansir.uploader.jiagu

import java.io.File

data class JiaGuConfig(
    val projectRootPath : String,//项目根地址
    val targetDir : String,//加固输出文件夹
    val jarPath : String,//加固jar路径
    val jksPath : String,//签名文件路径
    val KeystorePassword : String,//签名密码
    val alias : String,//别名
    val aliasPassword : String,//别名
    val mulPkgPath : String,//多渠道路径
){
    fun shFile() = File(projectRootPath,"jiagu.sh")
}