package io.fansir.uploader

import io.fansir.uploader.bean.ApkInfo

interface IAutoUpload<T> {
    fun upload(apkInfo: ApkInfo): Result<T>
}