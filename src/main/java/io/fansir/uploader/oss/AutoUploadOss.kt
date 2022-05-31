package io.fansir.uploader.oss

import io.fansir.uploader.IAutoUpload
import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.util.Utils

class AutoUploadOss(
    private val config: OssConfig,
    private val dirName : String
) : IAutoUpload<String> {

    private fun checkDirName(){
        if (Utils.isEmpty(dirName)){
            Utils.stopTask("dirName不允许为空！")
        }
        if (dirName.startsWith("/")){
            Utils.stopTask("dirName不允许以/开始！")
        }
        if (dirName.endsWith("/")){
            Utils.stopTask("dirName不允许以/结尾！")
        }
    }

    override fun upload(apkInfo: ApkInfo): Result<String> {
        checkDirName()
        apkInfo.checkApkFile()
        Utils.info("开始上传APK：${apkInfo.apkFile.absolutePath}")
        Oss.uploadFile(config,dirName,apkInfo.apkFile){
            Utils.info("上传成功：${it}")
        }
        return Result.success("")
    }
}

/**
 * 发布到OSS
 */
fun ApkInfo.uploadOss(
    config : OssConfig,
    dirName : String,
) : Result<String> = AutoUploadOss(config = config,dirName = dirName).upload(this)