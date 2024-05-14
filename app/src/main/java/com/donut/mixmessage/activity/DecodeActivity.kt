package com.donut.mixmessage.activity

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.donut.mixmessage.app
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.component.common.CommonColumn
import com.donut.mixmessage.ui.theme.MixMessageTheme
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.encode.decodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.objects.MixActivity


@Composable
fun DialogContainer(content: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = {
            currentActivity.finish()
        },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {

        Card(
            modifier = Modifier
                .systemBarsPadding()
                .heightIn(0.dp, 600.dp)
        ) {
            CommonColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
            ) {
                content()
            }
        }
    }
}

class DecodeActivity : MixActivity("decode") {

    companion object {
        var decodeText: CoderResult? by mutableStateOf(null)
        var LAST_FORCE_CLOSE = 0L
        var IS_ACTIVE by mutableStateOf(false)
    }

    override fun onPause() {
        super.onPause()
        IS_ACTIVE = false
        finish()
    }

    override fun onResume() {
        IS_ACTIVE = true
        super.onResume()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setContent {
            LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            MixMessageTheme {
                // A surface container using the 'background' color from the theme
                decodeText?.let { DecodeTextDialog(it) }
            }
        }
    }

}


fun openDecodeDialog(text: String = "", result: CoderResult? = null) {
    if (System.currentTimeMillis() - DecodeActivity.LAST_FORCE_CLOSE < 3000) {
        return
    }
    val decodeResult = decodeText(text)
    if (decodeResult.isFail && result.isNull()) {
        return
    }
    DecodeActivity.decodeText = decodeResult
    val intent = Intent(app, DecodeActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    ContextCompat.startActivity(app, intent, null)
}
