package com.donut.mixmessage.util.objects

import androidx.activity.ComponentActivity
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isAccessibilityServiceEnabled

open class MixActivity : ComponentActivity() {

    override fun onResume() {
        currentActivity = this
        if (isAccessibilityServiceEnabled()) {
            catchError {
                AccessibilityApi.requireBaseAccessibility()
            }
        }
        super.onResume()
    }
}
