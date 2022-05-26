package io.fansir.uploader.oss

import io.fansir.uploader.IAutoUpload
import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.util.Utils

class AutoUploadOss(
    private val config: OssConfig,
    private val dirName : String
) : IAutoUpload<String> {

    override fun upload(apkInfo: ApkInfo): Result<String> {
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