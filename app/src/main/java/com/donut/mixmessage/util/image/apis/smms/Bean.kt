package com.donut.mixmessage.util.image.apis.smms

import com.google.gson.annotations.SerializedName


data class SMMSUploadResult(
    @SerializedName("success") var success: Boolean? = null,
    @SerializedName("code") var code: String? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("data") var data: SMMSData? = SMMSData(),
    @SerializedName("images") var images: String? = null,
    @SerializedName("RequestId") var requestId: String? = null
)

data class SMMSData(
    @SerializedName("file_id") var fileId: Int? = null,
    @SerializedName("width") var width: Int? = null,
    @SerializedName("height") var height: Int? = null,
    @SerializedName("filename") var filename: String? = null,
    @SerializedName("storename") var storeName: String? = null,
    @SerializedName("size") var size: Int? = null,
    @SerializedName("path") var path: String? = null,
    @SerializedName("hash") var hash: String? = null,
    @SerializedName("url") var url: String? = null,
    @SerializedName("delete") var delete: String? = null,
    @SerializedName("page") var page: String? = null
)