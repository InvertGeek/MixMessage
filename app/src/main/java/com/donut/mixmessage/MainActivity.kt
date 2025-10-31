package com.donut.mixmessage

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.objects.MixActivity

var ACS_NOTIFY by cachedMutableOf(true, "acs_service_notify")

class MainActivity : MixActivity(MAIN_ID) {
    override fun onResume() {
        checkOverlayPermission()
        super.onResume()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContent()
        }
    }
}