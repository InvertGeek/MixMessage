package com.donut.mixmessage.ui.routes.settings.routes

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.nav.MixNavPage
import com.donut.mixmessage.ui.nav.NavTitle
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.copyWithDialog
import com.donut.mixmessage.util.common.hashSHA256
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.RSAUtil

val RSAPage = MixNavPage(
    gap = 10.dp,
    displayNavBar = false,
    useTransition = true,
) {
    NavTitle(title = "非对称加密设置", showBackIcon = true)
    Text(
        text = """
        非对称加密采用RSA算法,
        会为你生成独一无二的公钥和私钥,
        在解密弹窗输入框输入rsa这三个字符,即可出现发送公钥按钮
        将公钥发送给他人,他人可使用公钥加密发送内容
        加密的数据只有你使用私钥才能解密
        即使有人监听聊天,得知了公钥也得知加密算法
        依然无法解密使用你的公钥加密的内容
        推荐用来发送对称加密使用的密钥
        在下方可查看密钥对的公钥(私钥隐藏无法查看)
    """.trimIndent()
    )
    val fingerPrint = RSAUtil.publicKey.encoded.hashSHA256()
    Text(
        text = "当前公钥指纹(点击复制): $fingerPrint",
        color = colorScheme.primary,
        modifier = Modifier.clickable {
            fingerPrint.copyWithDialog()
        })
    Text(
        text = "当前密钥对公钥(点击重新生成): ${RSAUtil.publicKeyStr}",
        color = colorScheme.primary,
        modifier = Modifier.clickable {
            MixDialogBuilder("确定重新生成密钥对?").apply {
                setPositiveButton("确定") {
                    RSAUtil.regenerateKeyPair()
                    showToast("重新生成密钥对成功")
                    closeDialog()
                }
                setDefaultNegative()
                show()
            }
        })
}