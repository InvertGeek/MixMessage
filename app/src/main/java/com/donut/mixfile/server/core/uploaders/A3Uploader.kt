package com.donut.mixfile.server.core.uploaders

import com.alibaba.fastjson2.JSONObject
import com.alibaba.fastjson2.to
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.utils.add
import com.donut.mixfile.server.core.utils.fileFormHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData

object A3Uploader : Uploader("线路A3") {

    override val referer: String
        get() = ""

    override suspend fun doUpload(fileData: ByteArray, client: HttpClient): String {
        val result =
            client.submitFormWithBinaryData(
                "https://chatbot.weixin.qq.com/weixinh5/webapp/pfnYYEumBeFN7Yb3TAxwrabYVOa4R9/cos/upload",
                formData {
                    add("media", fileData, fileFormHeaders())
                }) {
            }.body<String>().to<JSONObject>()

        return result.getString("url")
    }
}
