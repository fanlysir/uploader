package io.fansir.uploader.pgyer

class PgyApkInfo {
    var code = 0
    var message: String? = null
    var data: PygApkData? = null
}

class PygApkData {
    var buildShortcutUrl: String? = null
    val fullUrl: String
        get() = "https://www.pgyer.com/$buildShortcutUrl"
}