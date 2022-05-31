package io.fansir.uploader.oss

import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.event.ProgressEvent
import com.aliyun.oss.event.ProgressEventType
import com.aliyun.oss.event.ProgressListener
import com.aliyun.oss.model.UploadFileRequest
import io.fansir.uploader.util.Utils
import java.io.File
import java.util.*

object Oss {

    fun uploadFile(config: OssConfig, dirName: String, file: File, success: (String) -> Unit) {
        if (!file.exists()) {
            Utils.stopTask("上传文件不存在：${file.absolutePath}")
            return
        }
        if (!file.isFile) {
            Utils.stopTask("不支持上传非文件：${file.absolutePath}")
            return
        }
        val builder = OSSClientBuilder().build(
            config.endpoint, config.accessKeyId, config.accessKeySecret
        )
        val key = "${dirName}/${file.name}"
        kotlin.runCatching {
            val request = UploadFileRequest(config.bucketName, key).apply {
                uploadFile = file.absolutePath
                taskNum = 5
                partSize = (1024 * 1024 * 4).toLong()
                isEnableCheckpoint = true
                progressListener = object : ProgressListener {
                    var totalUploadSize: Long = 0
                    var currentUploadSize: Long = 0
                    override fun progressChanged(progressEvent: ProgressEvent) {
                        when (progressEvent.eventType) {
                            ProgressEventType.TRANSFER_STARTED_EVENT -> {
                                Utils.info("开始上传文件：${file.absolutePath}")
                            }
                            ProgressEventType.REQUEST_CONTENT_LENGTH_EVENT -> {
                                totalUploadSize = progressEvent.bytes
                                Utils.info("文件大小：${totalUploadSize}")
                            }
                            ProgressEventType.REQUEST_BYTE_TRANSFER_EVENT -> {
                                currentUploadSize += progressEvent.bytes
                                Utils.info("上传进度：${(currentUploadSize * 100.0 / totalUploadSize).toInt()}%")
                            }
                            ProgressEventType.TRANSFER_COMPLETED_EVENT -> {
                                currentUploadSize += progressEvent.bytes
                            }
                            else -> {
                            }
                        }
                    }
                }
            }
            builder.uploadFile(request)
            val current = 1643180354032
            val expiration = Date(current + 3600L * 1000 * 24 * 365 * 10)
            val downloadUrl = builder.generatePresignedUrl(config.bucketName, key, expiration)
            downloadUrl.toString()
        }.onSuccess { url ->
            builder.shutdown()
            success.invoke(url)
        }.onFailure { e ->
            builder.shutdown()
            Utils.stopTask(e.message)
        }
    }

}