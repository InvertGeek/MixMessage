package com.donut.mixmessage.util.objects

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.donut.mixmessage.util.common.getFileName
import com.donut.mixmessage.util.image.toByteArray

class MixFileSelector(activity: MixActivity) {
    private var fileSelector: ActivityResultLauncher<Array<String>>
    private var callback: (ByteArray, String) -> Unit = { _, _ -> }

    init {
        fileSelector = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            it?.let {
                callback(it.toByteArray(), it.getFileName())
            }
        }
    }

    fun unregister(){
        fileSelector.unregister()
    }


    fun openSelect(
        array: Array<String> = arrayOf("image/*"),
        callback: (data: ByteArray, fileName: String) -> Unit
    ) {
        this.callback = callback
        fileSelector.launch(array)
    }
}