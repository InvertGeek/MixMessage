package com.donut.mixmessage.ui.routes.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.LabelSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.nav.MixNavPage
import com.donut.mixmessage.ui.routes.settings.SettingBox
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.genRandomString
import com.donut.mixmessage.util.common.hashToMD5String
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_PASSWORD
import com.donut.mixmessage.util.encode.LAST_DECODE
import com.donut.mixmessage.util.encode.PASSWORDS
import com.donut.mixmessage.util.encode.USE_TIME_LOCK
import com.donut.mixmessage.util.encode.clearAllPassword
import com.donut.mixmessage.util.encode.decryptAESBase64
import com.donut.mixmessage.util.encode.encryptAESBase64
import com.donut.mixmessage.util.encode.exportAllPassword
import com.donut.mixmessage.util.encode.getPassStringList
import com.donut.mixmessage.util.encode.getPasswordIndex
import com.donut.mixmessage.util.encode.importPasswords
import com.donut.mixmessage.util.encode.manualAddPassword
import okhttp3.internal.toLongOrDefault
import java.util.Date

var LOCK_PASSWORD by cachedMutableOf("", "lock_password")

var LOCK_CACHE by cachedMutableOf("", "lock_cache")

var LOCK_TIMEOUT by cachedMutableOf(600L, "lock_timeout")

var ENABLE_AUTO_LOCK by cachedMutableOf(false, "lock_enable")
var DEFAULT_PASS_CACHE by cachedMutableOf(DEFAULT_PASSWORD, "default_pass_lock_cache")

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

    DEFAULT_PASS_CACHE = encryptAESBase64(DEFAULT_PASSWORD, LOCK_PASSWORD)

    DEFAULT_PASSWORD = "123"

    PASSWORDS.forEach {
        if (it.value.contentEquals("123")) {
            return@forEach
        }
        it.updateValue(encryptAESBase64(it.value, LOCK_PASSWORD))
    }

    LOCK_CACHE = encryptAESBase64(LOCK_PASSWORD, LOCK_PASSWORD)
    LOCK_PASSWORD = ""
    showToast("锁定成功")
}

fun tryUnlock(password: String) {
    decryptAESBase64(LOCK_CACHE, password).isEmpty().isTrue {
        showToast("密码错误")
        return
    }
    LOCK_CACHE = ""
    LOCK_PASSWORD = password
    LAST_DECODE = System.currentTimeMillis()

    PASSWORDS.forEach {
        if (it.value.contentEquals("123")) {
            return@forEach
        }
        it.updateValue(decryptAESBase64(it.value, password))
    }

    DEFAULT_PASSWORD = decryptAESBase64(DEFAULT_PASS_CACHE, password)

    showToast("解锁成功")
}

fun ignoreLock() {
    ENABLE_AUTO_LOCK = false
    LOCK_CACHE = ""
    LOCK_PASSWORD = ""
    showToast("已忽略锁定")
}


@OptIn(ExperimentalLayoutApi::class)
fun showPasswordsDialog() {
    MixDialogBuilder("密钥列表").apply {
        setContent {
            val passList = getPassStringList()

            SingleSelectItemList(passList, DEFAULT_PASSWORD, {
                "#${getPasswordIndex(it)} $it"
            }) {
                openPasswordDialog(it)
            }
        }
        setBottomContent {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelSwitch(checked = USE_TIME_LOCK, label = "时间锁: ") {
                    if (it) {
                        MixDialogBuilder("确定开启?").apply {
                            setContent {
                                Text(
                                    text = """
                                            开启后当天加密的内容只会在当天自动解密成功,
                                            会在当前密钥后方添加当前日期后再进行加密,
                                            解密时会自动在所有密钥后方添加日期尝试解密(默认)
                                        """.trimIndent().replace("\n", " ")
                                )
                            }
                            setDefaultNegative()
                            setPositiveButton("确定") {
                                USE_TIME_LOCK = true
                                closeDialog()
                            }
                            show()
                        }
                        return@LabelSwitch
                    }
                    USE_TIME_LOCK = false
                }
                Button(onClick = { openAddPasswordDialog() }) {
                    Text(text = "添加密钥")
                }
            }
        }
        show()
    }
}

@Composable
fun LockSettings() {
    SettingBox {
        var lockTime by remember {
            mutableStateOf("")
        }
        Text(text = "密钥锁定设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "超过一定时间没有使用解密功能，自动加密所有密钥(需要开启无障碍)",
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
        OutlinedTextField(
            value = if (ENABLE_AUTO_LOCK) LOCK_TIMEOUT.toString() else lockTime,
            onValueChange = {
                lockTime = it
            },
            label = {
                Text(text = "自动锁定时间(秒)")
            },
            maxLines = 1,
            enabled = !ENABLE_AUTO_LOCK,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = LOCK_PASSWORD, onValueChange = {
                ENABLE_AUTO_LOCK = false
                LOCK_PASSWORD = it
            },
            label = {
                Text(text = "解锁密码${if (ENABLE_AUTO_LOCK) "(已哈希)" else ""}")
            },
            maxLines = 1,
            enabled = !ENABLE_AUTO_LOCK,
            modifier = Modifier.fillMaxWidth()
        )
        CommonSwitch(
            checked = ENABLE_AUTO_LOCK,
            text = "启用自动锁定:",
            description = "请牢记密码,锁定后会清除,丢失无法找回"

        ) { enabled ->
            if (enabled) {
                if (LOCK_PASSWORD.isEmpty()) {
                    return@CommonSwitch showToast("解锁密码不能为空")
                }
                if (lockTime.toLongOrNull().isNull()) {
                    showToast("请输入正确的时间")
                    return@CommonSwitch
                }
                MixDialogBuilder("是否确认启用?").apply {
                    setContent {
                        Text(text = "请确认已经牢记当前密码,锁定后会清除,丢失无法找回")
                    }
                    setPositiveButton("确认") {
                        LOCK_TIMEOUT = lockTime.toLong().coerceAtLeast(10)
                        ENABLE_AUTO_LOCK = true
                        LAST_DECODE = System.currentTimeMillis()
                        LOCK_PASSWORD = LOCK_PASSWORD.hashToMD5String(100)
                        closeDialog()
                        showToast("已启用自动锁定")
                    }
                    show()
                }
                return@CommonSwitch
            }
            LOCK_PASSWORD = ""
            ENABLE_AUTO_LOCK = false
        }
        Button(onClick = {
            ENABLE_AUTO_LOCK.isFalse {
                showToast("请先开启自动锁定!")
                return@Button
            }
            MixDialogBuilder("确认锁定?").apply {
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "密钥已被锁定",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Button(onClick = {
                var inputValue by mutableStateOf("")
                MixDialogBuilder("输入密码").apply {
                    setContent {
                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = {
                                inputValue = it
                            },
                            label = {
                                Text(text = "输入解锁密码")
                            },
                            maxLines = 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    setPositiveButton("确认") {
                        tryUnlock(inputValue.hashToMD5String(100))
                        closeDialog()
                    }
                    show()
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "立即解锁", fontSize = 20.sp)
            }
            OutlinedButton(onClick = {
                MixDialogBuilder("确定忽略锁定?").apply {
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
                Text(text = "忽略锁定", fontSize = 20.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
val Passwords = MixNavPage(gap = 20.dp) {
    SettingBox {
        Text(text = "密钥列表", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "解密时会自动尝试所有密钥",
            fontSize = 14.sp,
            color = Color(0xFFAAAAAA)
        )
        FlowRow(horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    showPasswordsDialog()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp)
            ) {
                Text(text = "查看密钥列表")
            }
            OutlinedButton(
                onClick = {
                    MixDialogBuilder("轮换密钥").apply {
                        var name by mutableStateOf("")
                        var hour by mutableLongStateOf(12)
                        setContent {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    maxLines = 1,
                                    value = name,
                                    onValueChange = {
                                        name = it
                                    },
                                    label = {
                                        Text(text = "密钥备注")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = hour.toString(),
                                    onValueChange = {
                                        hour = it.toLongOrDefault(1).coerceAtLeast(1)
                                    },
                                    label = {
                                        Text(text = "轮换周期(小时)")
                                    },
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    color = Color.Gray,
                                    text = """
                                        轮换密钥会定期自动刷新
                                        使用轮换密钥请确保系统时间正确
                                        一旦系统时间超过正确时间一定数值,轮换密钥的状态将不可回滚
                                        根据密钥的当前状态计算出密钥的下一个状态
                                        相比于时间锁(单纯的添加日期在密钥后方进行加密解密)
                                        轮换的方式具有不可逆的向前安全性(通过当前状态无法反推上一个状态)
                                        如果状态已经更新,是无法通过简单的修改日期的方式回退状态的
                                        特性: 具有缓存,更新下一状态后,会临时缓存上一状态,防止在状态即将更新时发送的信息无法解密
                                        具有预测,解密时同样会预测下一状态,生成下一状态的密钥尝试解密,防止对方时间提前更新状态导致无法解密信息
                                        也就是单个轮换密钥会使用三个解密密钥,分别是,过去(缓存的上一个状态),现在,和未来(动态计算的下一状态)的密钥
                                        具体效果为: 轮换时间为1小时的轮换密钥,在发送信息2小时后,发送的信息将无法解密
                                        缺点: 不具有向后安全性,一旦当前状态密钥泄漏,可通过当前状态预测以后所有的密钥
                                    """.trimIndent()
                                )
                            }
                        }
                        setPositiveButton("添加") {
                            if (name.isBlank()) {
                                showToast("备注不能为空")
                                return@setPositiveButton
                            }
                            if (manualAddPassword("_r:${name}:s:${genRandomString(44)}:${Date().time / 1000}:${hour}:0")) {
                                closeDialog()
                                showPasswordsDialog()
                            }
                        }
                        show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Text(text = "生成轮换密钥")
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
        介绍: 移位加密中文内容结果最短,固定比原内容多出4个字符
        但是只会加密信息中的中文英文和数字,特殊字符不会进行加密
        安全性较低,容易被暴力破解,推荐使用其他加密
        空位加密使用的是不可见字符(字符宽度为0)
    """.trimIndent(), color = colorScheme.primary
        )
    }
    LockSettings()
}
