package com.donut.mixmessage.util.common

import android.content.Context
import android.widget.Toast
import com.donut.mixmessage.app
import com.donut.mixmessage.appScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


var toast: Toast? = null

fun showToast(msg: String, context: Context = app) {
    appScope.launch(Dispatchers.Main) {
        toast?.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_LONG).apply {
            show()
        }
    }
}
