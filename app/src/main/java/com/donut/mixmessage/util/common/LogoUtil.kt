package com.donut.mixmessage.util.common

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import com.donut.mixmessage.MainActivity
import com.donut.mixmessage.util.objects.MixActivity

class LogoUtil {
    enum class Logo(val packageName: String, val label: String) {
        TIKTOK("com.donut.mixmessage.tiktok", "抖音"),
        DEFAULT("com.donut.mixmessage.MainActivity", "默认"),
        BILIBILI("com.donut.mixmessage.bilibili", "哔哩哔哩"),
        CALCULATOR("com.donut.mixmessage.calculator", "计算器"),
        WECHAT("com.donut.mixmessage.wechat", "微信"),
        QQ("com.donut.mixmessage.qq", "QQ"),
        CLOCK("com.donut.mixmessage.clock", "时钟"),
        CALENDAR("com.donut.mixmessage.calendar", "日历"),
        RADIO("com.donut.mixmessage.radio", "收音机"),
    }

    companion object {

        fun changeLogo(logo: Logo) {
            val name = logo.packageName
            val context = MixActivity.getMainContext()!!
            val currentComponentName = context.componentName
            if (name.contentEquals(currentComponentName?.className)) {
                return
            }
            val pm = context.packageManager
            pm.setComponentEnabledSetting(
                ComponentName(context, name),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP
            )
            pm.setComponentEnabledSetting(
                currentComponentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP
            )
            reStartApp(pm)
        }

        private fun reStartApp(pm: PackageManager) {
            val am =
                MixActivity.getMainContext()!!
                    .getSystemService(Activity.ACTIVITY_SERVICE) as ActivityManager
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            val resolveInfos = pm.queryIntentActivities(intent, 0)
            for (resolveInfo in resolveInfos) {
                if (resolveInfo.activityInfo != null) {
                    am.killBackgroundProcesses(resolveInfo.activityInfo.packageName)
                }
            }
        }
    }
}