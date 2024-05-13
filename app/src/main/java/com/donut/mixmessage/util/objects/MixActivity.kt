package com.donut.mixmessage.util.objects

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import com.donut.mixmessage.currentActivity

open class MixActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        currentActivity = this
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onResume() {
        currentActivity = this
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentActivity = this
        super.onCreate(savedInstanceState)
    }
}
