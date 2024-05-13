package com.donut.mixmessage.util.common

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.donut.mixmessage.app


var toast: Toast? = null

fun showToast(msg: String, context: Context = app) {
    val handler = Handler(Looper.getMainLooper())
    // Post a message to the main thread's message queue
    handler.post {
        toast?.cancel()
        toast = Toast.makeText(context, msg, Toast.LENGTH_LONG)
        toast?.show()
    }
}