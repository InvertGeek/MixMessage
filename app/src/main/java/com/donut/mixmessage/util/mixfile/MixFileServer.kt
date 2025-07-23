package com.donut.mixmessage.util.mixfile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.donut.mixfile.server.core.MixFileServer
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.uploaders.A1Uploader
import com.donut.mixfile.server.core.uploaders.A2Uploader
import com.donut.mixfile.server.core.uploaders.A3Uploader
import com.donut.mixfile.server.core.uploaders.js.JSUploader
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.mixfile.image.createBlankBitmap
import com.donut.mixmessage.util.mixfile.image.toGif

val DEFAULT_UPLOADER = A2Uploader
var JAVASCRIPT_UPLOADER_CODE by cachedMutableOf("", "JAVASCRIPT_UPLOADER_CODE")

val JavaScriptUploader
    get() = JSUploader(
        "JS自定义线路",
        scriptCode = JAVASCRIPT_UPLOADER_CODE
    )

fun showJSDocWindow() {
    MixDialogBuilder("JS自定义线路教程").apply {
        setContent {
            Text(
                """
                使用JS语法,支持部分ES6语法
                使用print代替console.log
                
                全局变量:
                IMAGE_DATA 加密后的图片base64
                HEAD_SIZE 原图片大小
                
                支持的函数列表:
                
                atob(base64) 解码base64,返回字符串
                
                btoa(字符串) 将字符串编码为base64
                
                hash(算法,base64) 支持MD5 SHA256等算法
                例如: hash("MD5",btoa("123"))
                
                appendBase64(base64,base64)
                拼接两串base64的二进制，返回base64
                
                encodeUrl,decodeUrl,url编码解码
                
                print(a,b,c) 在控制台输出内容
                
                submitForm(url,formData,headers) 提交表单
                使用 [文件数据(base64),文件名,mime类型] 代表文件
                返回base64格式响应体
                
                http(方法,url,body,headers) 发送http请求
                body需要为base64格式
                返回base64格式响应体
                
                setReferer(字符串) 设置下载时的referer请求头
                
                全局内存缓存:
                putCache(key,value,expire seconds) 
                getCache(key)
                value只支持字符串,expire为-1永不过期
                
                全局互斥锁:
                lock(key,func) 返回func执行结果
                例如lock("abc",()=>1)
                
                
                代码例子:
                const cookie = `cookie value`;
                const referer = "https://example.com/";
                //设置下载时的referer
                setReferer(referer);
                //使用[]数组包裹base64代表二进制文件数据,格式为: [文件数据,文件名,mime类型]
                const formData = {
                	file: [IMAGE_DATA,"1.gif","image/gif"]
                };
                const headers = {
                    referer,
                    cookie,
                };
                const responseBase64 = submitForm("https://example.com/api/upload", formData, headers);

                const data = JSON.parse(atob(responseBase64));
                const result = data.url.split('?')[0];
                print("上传成功,图片地址: ",result)
                //在最后一行填写图片地址表达式
                result;
            """.trimIndent()
            )
        }
        setDefaultNegative("关闭")
        show()
    }
}

fun openJavaScriptUploaderWindow() {
    MixDialogBuilder("JS自定义线路设置").apply {
        setContent {
            OutlinedTextField(
                value = JAVASCRIPT_UPLOADER_CODE,
                onValueChange = {
                    JAVASCRIPT_UPLOADER_CODE = it
                },
                minLines = 10,
                label = {
                    Text(text = "JavaScript代码")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        setPositiveButton("教程") {
            showJSDocWindow()
        }
        setDefaultNegative("关闭")
        show()
    }
}

val UPLOADERS get() = listOf(A1Uploader, A2Uploader, A3Uploader, JavaScriptUploader)

var MIXFILE_UPLOADER by cachedMutableOf(DEFAULT_UPLOADER.name, "mixfile_uploader")

fun getCurrentUploader() =
    UPLOADERS.firstOrNull { it.name.contentEquals(MIXFILE_UPLOADER) } ?: DEFAULT_UPLOADER

fun selectUploader() {
    MixDialogBuilder("上传线路").apply {
        setContent {
            SingleSelectItemList(
                items = UPLOADERS.map { it.name },
                getLabel = { it },
                currentOption = MIXFILE_UPLOADER
            ) { option ->
                MIXFILE_UPLOADER = option
                closeDialog()
            }
        }
        show()
    }
}

val server = object : MixFileServer(
    serverPort = 8014,
) {
    override val downloadTaskCount: Int
        get() = 5

    override val uploadTaskCount: Int
        get() = 10

    override val uploadRetryCount
        get() = 10

    override fun onError(error: Throwable) {

    }

    override fun getUploader(): Uploader {
        return getCurrentUploader()
    }


    override suspend fun genDefaultImage(): ByteArray {
        return createBlankBitmap().toGif()
    }

}