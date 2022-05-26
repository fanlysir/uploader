package io.fansir.uploader.fir

import io.fansir.uploader.IAutoUpload
import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.http.HttpClient
import io.fansir.uploader.util.Utils
import java.io.File

class AutoUploadFir(
    private val apiToken: String,
    private val uploadUrl: String,
) : IAutoUpload<String> {

    override fun upload(apkInfo: ApkInfo): Result<String> {
        apkInfo.checkApkFile()
        apkInfo.checkIconFile()
        val firCert: FirCert? = HttpClient(uploadUrl)
            .addParams("type", "android")
            .addParams("bundle_id", apkInfo.packageName)
            .addParams("api_token", apiToken)
            .start(FirCert::class.java)
        return if (firCert?.cert?.binary != null && firCert.cert?.icon != null) {
            Utils.info("获取fir上传凭证成功，开始上传文件！")
            uploadApkIcon(apkInfo, firCert)
        } else {
            Utils.stopTask("获取fir上传凭证失败！")
            Result.failure(Exception("获取fir上传凭证失败！"))
        }
    }

    private fun uploadApkIcon(apkInfo: ApkInfo, firCert: FirCert): Result<String> {
        Utils.info("开始上传图标：${apkInfo.iconPath}")
        val firCompleted = HttpClient(firCert.cert?.icon?.uploadUrl)
            .addParams("key", firCert.cert?.icon?.key)
            .addParams("token", firCert.cert?.icon?.token)
            .addFile("file", File(apkInfo.iconPath)) { percent ->
                Utils.info("上传进度：${percent}%")
            }.start(FirCompleted::class.java)
        return if (firCompleted?.isCompleted == true) {
            Utils.info("图标上传成功！")
            uploadApk(apkInfo, firCert)
        } else {
            Result.failure(Exception("图标上传成功"))
        }
    }

    private fun uploadApk(apkInfo: ApkInfo, firCert: FirCert): Result<String> {
        Utils.info("开始上传APK：${apkInfo.apkFile.absolutePath}")
        val firCompleted = HttpClient(firCert.cert?.binary?.uploadUrl)
            .addParams("key", firCert.cert?.binary?.key)
            .addParams("token", firCert.cert?.binary?.token)
            .addParams("x:name", apkInfo.appName)
            .addParams("x:changelog", apkInfo.changeLog)
            .addParams("x:version", apkInfo.versionName)
            .addParams("x:build", apkInfo.versionCode)
            .addFile("file", apkInfo.apkFile) { percent ->
                val p = Utils.parseFloat(percent)
                if (p % 10 == 0f){
                    Utils.info("上传进度：${percent}%")
                }
            }.start(FirCompleted::class.java)
        return if (firCompleted?.isCompleted == true) {
            Result.success("http://d.alphaqr.com/${firCert.shortUrl}")
        } else {
            Utils.stopTask("APK上传失败")
            Result.failure(Exception("APK上传失败"))
        }
    }
}


/**
 * 发布到Fir
 */
fun ApkInfo.uploadFir(
    apiToken: String,
    uploadUrl: String = "http://api.bq04.com/apps"
): Result<String> = AutoUploadFir(
    apiToken = apiToken,
    uploadUrl = uploadUrl
).upload(this)