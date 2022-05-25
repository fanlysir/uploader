package io.fansir.uploader.bean

sealed class UploadResult {
    object Success
    class Fail(val e : Exception)
}