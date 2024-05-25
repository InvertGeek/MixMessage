package com.donut.mixmessage.util.image.apis.bfs

import com.google.gson.annotations.SerializedName

data class BFSResponse(

    @SerializedName("code") var code: Int? = null,
    @SerializedName("message") var message: String? = null,
    @SerializedName("ttl") var ttl: Int? = null,
    @SerializedName("data") var data: Data? = Data()

)

data class Data(

    @SerializedName("image_url") var imageUrl: String? = null,
    @SerializedName("image_width") var imageWidth: Int? = null,
    @SerializedName("image_height") var imageHeight: Int? = null,
    @SerializedName("img_size") var imgSize: Double? = null

)