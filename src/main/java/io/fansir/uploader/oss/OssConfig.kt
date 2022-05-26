package io.fansir.uploader.oss

data class OssConfig (
    val accessKeyId : String,
    val accessKeySecret : String,
    val endpoint : String,
    val bucketName : String,
)