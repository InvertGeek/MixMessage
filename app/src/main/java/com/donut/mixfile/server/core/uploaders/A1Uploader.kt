package com.donut.mixfile.server.core.uploaders

import com.alibaba.fastjson2.JSONArray
import com.alibaba.fastjson2.to
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.utils.add
import com.donut.mixfile.server.core.utils.fileFormHeaders
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData

object A1Uploader : Uploader("线路A1") {

    override val referer: String
        get() = "wf︆︈︇︄︇︄︇︀︇︃︃︊︂️︂️︆︋︆︆︂︎︆︆︆︌︆︁︇︃︆︈︂︎︆︃︆︎︂️ey".sCode


    override suspend fun doUpload(fileData: ByteArray, client: HttpClient): String {
        val result =
            client.submitFormWithBinaryData(
                "${referer}service/upload",
                formData {
                    add("flag", "")
                    add("FileUploadForm[file]", fileData, fileFormHeaders())
                }) {
            }.body<String>().to<JSONArray>()
        if (result.isEmpty()) {
            throw Exception("上传失败")
        }
        val data = result.getJSONObject(0)
        val url = data.getString("url") ?: throw Exception("上传失败")
        return "https:${url}"
    }


}