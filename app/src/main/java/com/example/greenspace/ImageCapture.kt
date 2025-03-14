package com.example.greenspace

import S3Uploader
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class ImageCapture : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var profileBtn: ImageButton

    // Initialize S3Uploader
    private lateinit var s3Uploader: S3Uploader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)

        imageView = findViewById(R.id.scannerFrame)
        profileBtn = findViewById(R.id.btnProfile)
        s3Uploader = S3Uploader(this)

        checkAndRequestPermissions()

        imageView.setOnClickListener {
            showImageSourceDialog()
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileInfo::class.java))
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Capture from Camera", "Select from Gallery")
        AlertDialog.Builder(this)
            .setTitle("Choose Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> cameraLauncher.launch(null)
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun processImage(bitmap: Bitmap) {
        try {
            val compressedBitmap = compressImage(bitmap)
            imageView.setImageBitmap(compressedBitmap)
            uploadImageToS3(compressedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processImageUri(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            processImage(bitmap)  // Call the updated function
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressImage(bitmap: Bitmap): Bitmap {
        val maxSize = 1024
        val width: Int
        val height: Int
        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun uploadImageToS3(bitmap: Bitmap) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(this@ImageCapture, "✅ Upload started!", Toast.LENGTH_SHORT).show()
                withContext(Dispatchers.IO) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val imageName = "img_${timeStamp}.jpg"

                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                    val inputStream: InputStream = stream.toByteArray().inputStream()

                    s3Uploader.uploadImage(inputStream, imageName)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ImageCapture, "❌ Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { !it }) {
            Toast.makeText(this, "All permissions are required!", Toast.LENGTH_LONG).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { processImage(it) }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImageUri(it) }
    }
}
