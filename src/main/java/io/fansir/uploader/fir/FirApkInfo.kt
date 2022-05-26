package io.fansir.uploader.fir

import com.google.gson.annotations.SerializedName

class FirCert {
    var id: String? = null
    var type: String? = null
    @SerializedName("short")
    var shortUrl: String? = null
    var cert: FirCertBinary? = null
}

class FirCertBinary {
    var icon: FirCertUrl? = null
    var binary: FirCertUrl? = null
}

class FirCertUrl {
    var key: String? = null
    var token: String? = null
    @SerializedName("upload_url")
    var uploadUrl: String? = null
}

class FirCompleted {
    @SerializedName("is_completed")
    var isCompleted = false
}