@file:Suppress("MemberVisibilityCanBePrivate")

package com.donut.mixmessage.util.objects

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isTrue

open class MixActivity(private val id: String) : ComponentActivity() {

    init {
        referenceCache[id] = mutableSetOf()
    }

    var isActive = false;

    companion object {
        const val MAIN_ID = "main"
        val referenceCache = mutableMapOf<String, MutableSet<MixActivity>>()
        fun getContext(id: String) = referenceCache[id]?.firstOrNull { it.isActive }

        fun getMainContext() = getContext(MAIN_ID)

        fun firstActiveActivity(): MixActivity? {
            return referenceCache.values.flatten().firstOrNull { it.isActive }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        referenceCache[id]?.remove(this)
    }

    override fun onPause() {
        isActive = false
        super.onPause()
    }


    override fun onResume() {
        isActive = true
        referenceCache[id]?.add(this)
        isAccessibilityServiceEnabled().isTrue {
            catchError {
                AccessibilityApi.requireBaseAccessibility()
            }
        }
        super.onResume()
    }

    fun checkOverlayPermission() {
        Settings.canDrawOverlays(this).isFalse {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
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
