package com.example.greenspace.plantnetapi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object ImageCropper {

    fun cropCenterSquare(bitmap: Bitmap): Bitmap {
        val size = bitmap.width.coerceAtMost(bitmap.height)
        val xOffset = (bitmap.width - size) / 2
        val yOffset = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, xOffset, yOffset, size, size)
    }

    fun cropTopHalf(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height / 2
        return Bitmap.createBitmap(bitmap, 0, 0, width, height)
    }

    fun cropUsingBoundingBox(bitmap: Bitmap, boundingBox: Rect): Bitmap {
        return Bitmap.createBitmap(
            bitmap,
            boundingBox.left,
            boundingBox.top,
            boundingBox.width(),
            boundingBox.height()
        )
    }

    fun saveCroppedBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "cropped_image.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        return Uri.fromFile(file)
    }

    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}
