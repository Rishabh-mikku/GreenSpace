package com.example.greenspace.plantnetapi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.core.net.toUri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.FileOutputStream

object ImageCropper {

    fun cropDetectedObject(context: Context, imageUri: Uri, onResult: (Uri?) -> Unit) {
        val image: InputImage = InputImage.fromFilePath(context, imageUri)

        // Use ML Kit's built-in object detection
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()

        val detector = ObjectDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { detectedObjects ->
                if (detectedObjects.isNotEmpty()) {
                    val boundingBox: Rect = detectedObjects[0].boundingBox // Get first detected object
                    val originalBitmap = image.bitmapInternal

                    val croppedBitmap = originalBitmap?.let {
                        Bitmap.createBitmap(
                            it,
                            boundingBox.left.coerceAtLeast(0),
                            boundingBox.top.coerceAtLeast(0),
                            boundingBox.width().coerceAtMost(originalBitmap.width),
                            boundingBox.height().coerceAtMost(originalBitmap.height)
                        )
                    }

                    // Save and return cropped image
                    val croppedUri = croppedBitmap?.let { saveBitmapToCache(context, it) }
                    onResult(croppedUri)
                } else {
                    onResult(null) // No object detected
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "cropped_image.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        return file.toUri()
    }
}
