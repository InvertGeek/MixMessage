package com.donut.mixmessage

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import com.donut.mixmessage.service.IS_ACS_ENABLED
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isFalseAnd
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.objects.MixActivity
import com.donut.mixmessage.util.objects.MixFileSelector

var ACS_NOTIFY by cachedMutableOf(true, "acs_service_notify")


class MainActivity : MixActivity(MAIN_ID) {

    private var notified = false

    override fun onResume() {
        checkOverlayPermission()
        super.onResume()
        notified.isFalse {
            notified = true
            IS_ACS_ENABLED.isFalseAnd(ACS_NOTIFY) {
                MixDialogBuilder("提示").apply {
                    setContent {
                        Text(text = "无障碍权限未开启,是否进入设置?")
                        LaunchedEffect(IS_ACS_ENABLED) {
                            IS_ACS_ENABLED.isTrue {
                                closeDialog()
                            }
                        }
                    }
                    setDefaultNegative()
                    setPositiveButton("确定") {
                        checkAccessibilityPermission()
                        closeDialog()
                    }
                    show()
                }
            }
        }
    }

    companion object {
        lateinit var mixFileSelector: MixFileSelector
    }

    override fun onDestroy() {
        super.onDestroy()
        mixFileSelector.unregister()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        mixFileSelector = MixFileSelector(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }
}