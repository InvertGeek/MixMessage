package com.donut.mixmessage.util.image.apis.bb

import com.google.gson.annotations.SerializedName

data class BBUploadBean(

    @SerializedName("env") var env: Env = Env(),
    @SerializedName("credential") var credential: Credential = Credential(),
    @SerializedName("resList") var resList: ArrayList<String> = arrayListOf()

)

data class Env(

    @SerializedName("domain") var domain: String? = null,
    @SerializedName("publicEndpoint") var publicEndpoint: String? = null,
    @SerializedName("bucket") var bucket: String? = null,
    @SerializedName("endpoint") var endpoint: String? = null,
    @SerializedName("cdnDomain") var cdnDomain: String? = null

)

data class Credential(
    @SerializedName("securityToken") var securityToken: String? = null,
    @SerializedName("accessKeySecret") var accessKeySecret: String? = null,
    @SerializedName("accessKeyId") var accessKeyId: String? = null,
    @SerializedName("expiration") var expiration: String? = null
)