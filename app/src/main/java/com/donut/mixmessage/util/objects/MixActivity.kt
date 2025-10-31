@file:Suppress("MemberVisibilityCanBePrivate")

package com.donut.mixmessage.util.objects

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import com.donut.mixmessage.ui.routes.settings.routes.ALLOW_SCREENSHOT
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.encode.updateRoundKeys

open class MixActivity(private val id: String) : ComponentActivity() {

    init {
        referenceCache[id] = mutableSetOf()
    }

    var isActive = false
    var lastPause = System.currentTimeMillis()
    lateinit var fileSelector: MixFileSelector

    companion object {
        const val MAIN_ID = "main"
        val referenceCache = mutableMapOf<String, MutableSet<MixActivity>>()
        fun getContext(id: String) = referenceCache[id]?.firstOrNull { it.isActive }

        fun getMainContext() = getContext(MAIN_ID)

        fun firstActiveActivity(): MixActivity? {
            return referenceCache.values.flatten().maxByOrNull {
                if (it.isActive) Long.MAX_VALUE else it.lastPause
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileSelector = MixFileSelector(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        fileSelector.unregister()
        referenceCache[id]?.remove(this)
    }

    fun allowScreenshot() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun forbidScreenshot() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun refreshAllowScreenShot() {
        if (ALLOW_SCREENSHOT) {
            allowScreenshot()
        } else {
            forbidScreenshot()
        }
    }

    override fun onPause() {
        isActive = false
        lastPause = System.currentTimeMillis()
        super.onPause()
    }


    override fun onResume() {
        isActive = true
        referenceCache[id]?.add(this)
        isAccessibilityServiceEnabled().isTrue {
            catchError {
//                AccessibilityApi.requireBaseAccessibility()
            }
        }
        refreshAllowScreenShot()
        updateRoundKeys()
        super.onResume()
    }

    fun checkOverlayPermission() {
        Settings.canDrawOverlays(this).isFalse {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:$packageName".toUri())
            startActivity(intent)
        }
    }

    // 检查无障碍权限
    fun checkAccessibilityPermission() {
        isAccessibilityServiceEnabled().isFalse {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityService = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return accessibilityService?.contains(packageName).isTrue()
    }
}
