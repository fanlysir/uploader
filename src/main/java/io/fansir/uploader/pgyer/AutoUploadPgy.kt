package io.fansir.uploader.pgyer

import io.fansir.uploader.IAutoUpload
import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.http.HttpClient
import io.fansir.uploader.util.Utils

class AutoUploadPgy(
    private val apiKey : String,
    private val uploadUrl : String,
) : IAutoUpload<String> {
    override fun upload(apkInfo: ApkInfo): Result<String> {
        apkInfo.checkApkFile()
        Utils.info("开始上传文件到蒲公英：${apkInfo.apkFile.absolutePath}")
        val pgyApkInfo : PgyApkInfo? = HttpClient(uploadUrl)
            .addParams("_api_key",apiKey)
            .addFile("file",apkInfo.apkFile){ percent->
                val p = Utils.parseFloat(percent)
                if (p % 10 == 0f){
                    Utils.info("上传进度：${percent}%")
                }
            }
            .addParams("buildInstallType","1")
            .addParams("buildUpdateDescription",apkInfo.changeLog)
            .start(PgyApkInfo::class.java)
        return if (pgyApkInfo?.code == 0){
            Result.success(pgyApkInfo.data?.fullUrl?:"")
        }else{
            Utils.stopTask("上传蒲公英失败！${pgyApkInfo?.message}")
            Result.failure(Exception(pgyApkInfo?.message))
        }
    }
}

/**
 * 发布到蒲公英
 */
fun ApkInfo.uploadPgy(
    apiKey : String,
    uploadUrl : String = "https://www.pgyer.com/apiv2/app/upload",
) : Result<String> = AutoUploadPgy(apiKey = apiKey,uploadUrl = uploadUrl).upload(this)