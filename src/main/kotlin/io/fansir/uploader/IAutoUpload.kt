package io.fansir.uploader

import io.fansir.uploader.bean.ApkInfo
import io.fansir.uploader.bean.UploadResult

interface IAutoUpload<T> {
    fun uploadApk(apkInfo: ApkInfo): Result<T>
}