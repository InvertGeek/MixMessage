package com.donut.mixmessage.util.objects

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.donut.mixmessage.util.common.getFileName

class MixFileSelector(activity: MixActivity) {
    private var fileSelector: ActivityResultLauncher<Array<String>>
    private var callback: (Uri, String) -> Unit = { _, _ -> }

    init {
        fileSelector = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                callback(it, it.getFileName())
            }
        }
    }

    fun unregister() {
        fileSelector.unregister()
    }


    fun openSelect(
        array: Array<String> = arrayOf("image/*"),
        callback: (data: Uri, fileName: String) -> Unit
    ) {
        this.callback = callback
        fileSelector.launch(array)
    }
}