package com.donut.mixmessage.ui.component.routes.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.MaterialDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.routes.settings.SettingBox
import com.donut.mixmessage.ui.theme.LightColorScheme
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.calculateMD5
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_PASSWORD
import com.donut.mixmessage.util.encode.LAST_DECODE
import com.donut.mixmessage.util.encode.PASSWORDS
import com.donut.mixmessage.util.encode.addPassword
import com.donut.mixmessage.util.encode.clearAllPassword
import com.donut.mixmessage.util.encode.exportAllPassword
import com.donut.mixmessage.util.encode.importPasswords
import com.donut.mixmessage.util.encode.removePassword
import com.donut.mixmessage.util.encode.xxtea.XXTEA

var LOCK_PASSWORD by cachedMutableOf("", "lock_password")

var LOCK_CACHE by cachedMutableOf("", "lock_cache")

var LOCK_TIMEOUT by cachedMutableOf(600L, "lock_timeout")

var ENABLE_AUTO_LOCK by cachedMutableOf(false, "lock_enable")

fun startLock(force: Boolean = false) {
    if (LOCK_PASSWORD.isEmpty()) {
        return
    }
    if (System.currentTimeMillis() - LAST_DECODE < LOCK_TIMEOUT * 1000 && !force) {
        return
    }

    if (!ENABLE_AUTO_LOCK && !force) {
        return
    }

    DEFAULT_PASSWORD = XXTEA.encryptToBase64String(DEFAULT_PASSWORD, LOCK_PASSWORD)

    PASSWORDS.forEach {
        if (it.contentEquals("123")) {
            return@forEach
        }
        removePassword(it)
        addPassword(XXTEA.encryptToBase64String(it, LOCK_PASSWORD))
    }

    LOCK_CACHE = XXTEA.encryptToBase64String(LOCK_PASSWORD, LOCK_PASSWORD)
    LOCK_PASSWORD = ""
    showToast("锁定成功")
}

fun tryUnlock(password: String) {
    if (XXTEA.decryptBase64StringToString(LOCK_CACHE, password) != password) {
        showToast("密码错误")
        return
    }
    LOCK_CACHE = ""
    LOCK_PASSWORD = password
    LAST_DECODE = System.currentTimeMillis()

    PASSWORDS.forEach {
        if (it.contentEquals("123")) {
            return@forEach
        }
        removePassword(it)
        addPassword(XXTEA.decryptBase64StringToString(it, password))
    }

    DEFAULT_PASSWORD = XXTEA.decryptBase64StringToString(DEFAULT_PASSWORD, password)

    showToast("解锁成功")
}

fun ignoreLock() {
    LOCK_CACHE = ""
    LOCK_PASSWORD = ""
    showToast("已忽略锁定")
}


@OptIn(ExperimentalLayoutApi::class)
fun showPasswordsDialog() {
    MaterialDialogBuilder("密钥列表").apply {
        setPositiveButton("添加密钥") {
            openAddPasswordDialog()
        }
        setContent {
            val passList = PASSWORDS.toList().reversed()
            SingleSelectItemList(passList, DEFAULT_PASSWORD) {
                openPasswordDialog(it)
            }
        }
        show()
    }
}

@Composable
fun LockSettings() {
    SettingBox {

        Text(text = "密钥锁定设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "超过一定时间没有使用解密功能，自动加密所有密钥(需要开启无障碍)",
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
        OutlinedTextField(value = LOCK_TIMEOUT.toString(), onValueChange = {
            LOCK_TIMEOUT = it.toLongOrNull()?.coerceAtLeast(10) ?: 10
        }, label = {
            Text(text = "自动锁定时间(秒)")
        },
            enabled = !ENABLE_AUTO_LOCK,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(value = LOCK_PASSWORD, onValueChange = {
            ENABLE_AUTO_LOCK = false
            LOCK_PASSWORD = it
        }, label = {
            Text(text = "解锁密码${if (ENABLE_AUTO_LOCK) "(已哈希)" else ""}")
        },
            enabled = !ENABLE_AUTO_LOCK,
            modifier = Modifier.fillMaxWidth()
        )
        CommonSwitch(
            checked = ENABLE_AUTO_LOCK,
            text = "启用自动锁定:",
            onCheckedChangeListener = { enabled ->
                if (enabled) {
                    if (LOCK_PASSWORD.isEmpty()) {
                        return@CommonSwitch showToast("解锁密码不能为空")
                    }
                    MaterialDialogBuilder("是否确认启用?").apply {
                        setContent {
                            Text(text = "请确认已经牢记当前密码,锁定后会清除,丢失无法找回")
                        }
                        setPositiveButton("确认") {
                            ENABLE_AUTO_LOCK = true
                            LAST_DECODE = System.currentTimeMillis()
                            LOCK_PASSWORD = LOCK_PASSWORD.calculateMD5(100)
                            closeDialog()
                            showToast("已启用自动锁定")
                        }
                        show()
                    }
                    return@CommonSwitch
                }
                LOCK_PASSWORD = ""
                ENABLE_AUTO_LOCK = false
            },
            "请牢记密码,锁定后会清除,丢失无法找回"
        )
        Button(onClick = {
            MaterialDialogBuilder("确认锁定?").apply {
                setPositiveButton("确认") {
                    if (LOCK_PASSWORD.isEmpty()) {
                        showToast("解锁密码不能为空")
                        closeDialog()
                        return@setPositiveButton
                    }
                    startLock(true)
                    closeDialog()
                }
                show()
            }
        }) {
            Text(text = "立即锁定")
        }
    }
}

@Composable
fun Unlock() {
    Text(text = "密钥已被锁定", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Button(onClick = {
        var inputValue by mutableStateOf("")
        MaterialDialogBuilder("输入密码").apply {
            setContent {
                OutlinedTextField(value = inputValue, onValueChange = {
                    inputValue = it
                }, label = {
                    Text(text = "输入解锁密码")
                }, modifier = Modifier.fillMaxWidth())
            }
            setPositiveButton("确认") {
                tryUnlock(inputValue.calculateMD5(100))
                closeDialog()
            }
            show()
        }
    }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "立即解锁")
    }
    Button(onClick = {
        MaterialDialogBuilder("确定忽略锁定?").apply {
            setContent {
                Text(text = "忽略后,密钥列表将会可以访问,但是所有已经加密的密钥无法还原")
            }
            setPositiveButton("确认") {
                ignoreLock()
                closeDialog()
            }
            show()
        }
    }, modifier = Modifier.fillMaxWidth()) {
        Text(text = "忽略锁定")
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Passwords() {
    if (LOCK_CACHE.isNotEmpty()) {
        Unlock()
        return
    }
    SettingBox {
        Text(text = "密钥列表", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "解密时会自动尝试所有密钥",
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
        FlowRow(horizontalArrangement = Arrangement.SpaceAround) {
            Button(
                onClick = {
                    showPasswordsDialog()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(text = "查看密钥列表")
            }
            Button(onClick = {
                openCommonConfirmDialog("清空所有密钥") {
                    clearAllPassword()
                    showToast("已清空所有密钥")
                }
            }) {
                Text(text = "清空密钥")
            }
            Button(onClick = {
                openCommonConfirmDialog("导出所有密钥到剪贴板") {
                    exportAllPassword()
                    showToast("已导出所有密钥到剪贴板")
                }
            }) {
                Text(text = "导出密钥")
            }
            Button(onClick = {
                openCommonConfirmDialog("从剪贴板导入密钥") {
                    importPasswords().also {
                        showToast("导入了${it}条密钥")
                    }
                }
            }) {
                Text(text = "导入密钥")
            }
        }
        Text(
            text = """
        介绍: 移位加密结果最短,只比原内容多出三个字符
        即使使用相同密钥加密相同内容每次结果都会不相同
        并且只有密钥正确才能解码出原信息
        默认只会加密信息中的中文英文和数字,特殊字符不会进行加密
        如果需要请开启设置中的严格模式(不支持移位加密和随机结果)
        空位加密使用的是不可见字符(字符宽度为0)
    """.trimIndent(), color = LightColorScheme.primary
        )
    }
    LockSettings()
}