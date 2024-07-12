package com.donut.mixmessage

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.objects.MixActivity
import com.donut.mixmessage.util.objects.MixFileSelector

var ACS_NOTIFY by cachedMutableOf(true, "acs_service_notify")

class MainActivity : MixActivity(MAIN_ID) {
    override fun onResume() {
        checkOverlayPermission()
        super.onResume()
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