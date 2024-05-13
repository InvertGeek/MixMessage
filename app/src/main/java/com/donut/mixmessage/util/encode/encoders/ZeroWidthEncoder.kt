package com.donut.mixmessage.util.encode.encoders;

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.kv
import com.donut.mixmessage.service.inputAndSendText
import com.donut.mixmessage.ui.component.common.MaterialDialogBuilder
import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import kotlin.random.Random


object ZeroWidthEncoder : AlphabetCoder(
    listOf(
        '\uFE00', //VARIATION SELECTOR-1
        '\uFE01', //VARIATION SELECTOR-2
        '\uFE02', //VARIATION SELECTOR-3
        '\uFE03', //VARIATION SELECTOR-4
        '\uFE04', //VARIATION SELECTOR-5
        '\uFE05', //VARIATION SELECTOR-6
        '\uFE06', //VARIATION SELECTOR-7
        '\uFE07', //VARIATION SELECTOR-8
        '\uFE08', //VARIATION SELECTOR-9
        '\uFE09', //VARIATION SELECTOR-10
        '\uFE0A', //VARIATION SELECTOR-11
        '\uFE0B', //VARIATION SELECTOR-12
        '\uFE0C', //VARIATION SELECTOR-13
        '\uFE0D', //VARIATION SELECTOR-14
        '\uFE0E', //VARIATION SELECTOR-15
        '\uFE0F', //VARIATION SELECTOR-16
    )
) {

    override val name = "空位编码"
    var encodeResultPrefix by mutableStateOf(
        kv.decodeString(
            "zero_width_encode_result_prefix",
        ) ?: "x%r%r%r"
    )

    fun setShiftEncodeResultPrefix(prefix: String) {
        kv.encode("zero_width_encode_result_prefix", prefix)
        encodeResultPrefix = prefix
    }

    override fun generatePrefix(): String{
        return encodeResultPrefix.replace(Regex("%r")) {
            Random.nextInt(10).toString()
        }.replace(Regex("%e")) {
            EmojiEncoder.replaceMap.keys.random()
        }
    }


}
