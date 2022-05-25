package io.fansir.uploader.pgyer

import io.fansir.uploader.IAutoUpload
import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.http.HttpClient
import io.fansir.uploader.util.Utils

class AutoUploadPgy(
    private val apiKey : String,
    private val uploadUrl : String = "https://www.pgyer.com/apiv2/app/upload",
) : IAutoUpload<String> {
    override fun uploadApk(apkInfo: ApkInfo): Result<String> {
        apkInfo.checkApkFile()
        val changeLog = if (Utils.isEmpty(apkInfo.changeLog)) "发现了新版本，请去升级一下吧~" else apkInfo.changeLog
        val pgyApkInfo : PgyApkInfo? = HttpClient(uploadUrl)
            .addParams("_api_key",apiKey)
            .addFile("file",apkInfo.apkFile){ percent->
                val p = Utils.parseFloat(percent)
                if (p % 10 == 0f){
                    Utils.info("上传进度：${percent}%")
                }
            }
            .addParams("buildInstallType","1")
            .addParams("buildUpdateDescription",changeLog)
            .start(PgyApkInfo::class.java)
        return if (pgyApkInfo?.code == 0){
            Result.success(pgyApkInfo.data?.fullUrl?:"")
        }else{
            Utils.stopTask("上传蒲公英失败！${pgyApkInfo?.message}")
            Result.failure(Exception(pgyApkInfo?.message))
        }
    }
}