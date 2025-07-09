package com.donut.mixmessage.util.mixfile.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.createBitmap
import java.io.ByteArrayOutputStream
import kotlin.random.Random

fun createBlankBitmap(
    width: Int = Random.nextInt(50, 100),
    height: Int = Random.nextInt(50, 100),
): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
    return bitmap
}

fun Bitmap.compressToByteArray(
    useWebp: Boolean = true,
): ByteArray {
    val bitmap = this
    val stream = ByteArrayOutputStream()

    if (useWebp) {
        bitmap.compress(Bitmap.CompressFormat.WEBP, 0, stream)
    } else {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream)
    }

    return stream.toByteArray()
}

fun Bitmap.toGif(): ByteArray {
    val bos = ByteArrayOutputStream()
    val encoder = AnimatedGifEncoder()
    encoder.start(bos)
    encoder.addFrame(this)
    encoder.finish()
    return bos.toByteArray()
}