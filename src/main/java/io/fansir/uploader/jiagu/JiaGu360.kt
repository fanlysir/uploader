package io.fansir.uploader.jiagu

import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.util.Utils
import java.io.File

class JiaGu360(
    private val config: JiaGuConfig
) {

    fun start(apkInfo: ApkInfo) : ApkInfo{
        var file : File? = null
        kotlin.runCatching {
            createSh(apkInfo)
            val chmodProcess = Runtime.getRuntime().exec("chmod 777 ${config.shFile().absolutePath}")
            chmodProcess.waitFor()
            Utils.info("开始加固：${apkInfo.apkFile.absolutePath}")
            val jiaGuProcess: Process = Runtime.getRuntime().exec(config.shFile().absolutePath)
            jiaGuProcess.waitFor()
        }.onSuccess {
            file = Utils.findApkFile(File(config.targetDir))
            deleteSh()
        }.onFailure {
            deleteSh()
        }
        return if (file != null){
            apkInfo.copy(apkFile = file!!)
        }else{
            Utils.stopTask("加固失败")
            apkInfo
        }
    }

    private fun createSh(apkInfo: ApkInfo){
        kotlin.runCatching {
            deleteSh()
            val execCode = StringBuilder().apply {
                append("rm -rf ").append(config.targetDir).append("\n")
                append("mkdir ").append(config.targetDir).append("\n")
                append("java -jar ").append(config.jarPath).append(" -importsign ")
                append(config.jksPath).append(" ")
                append(config.KeystorePassword).append(" ")
                append(config.alias).append(" ")
                append(config.aliasPassword).append("\n")
                if (Utils.isNotEmpty(config.mulPkgPath)){
                    append("java -jar ").append(config.jarPath).append(" -importmulpkg ")
                    append(config.mulPkgPath).append("\n")
                }
                append("java -jar ").append(config.jarPath).append(" -jiagu ")
                append(apkInfo.apkFile.absolutePath).append(" ").append(config.jarPath)
                append(" -autosign -automulpkg -config-crashlog-x86-analyse-piracy")
                Utils.info(toString())
            }.toString()
            val shFile = config.shFile()
            shFile.createNewFile()
            Utils.writeText(shFile,execCode)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun deleteSh(){
        if (config.shFile().exists()){
            config.shFile().delete()
        }
    }

}