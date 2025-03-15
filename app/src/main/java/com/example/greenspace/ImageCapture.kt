package com.example.greenspace

import S3Uploader
import com.example.greenspace.plantnetapi.PlantNetUploader
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.FileProvider
import androidx.media3.common.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.greenspace.plantnetapi.ImageCropper

class ImageCapture : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var profileBtn: ImageButton
    private lateinit var s3Uploader: S3Uploader
    private lateinit var plantNetUploader: PlantNetUploader
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)

        imageView = findViewById(R.id.scannerFrame)
        profileBtn = findViewById(R.id.btnProfile)
        s3Uploader = S3Uploader(this)
        plantNetUploader = PlantNetUploader(this)

        setupImageUri()
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
                    0 -> cameraLauncher.launch(imageUri)
                    1 -> galleryLauncher.launch("image/*")
                }
            }
            .show()
    }

    private fun processImageUri(uri: Uri) {
        val resizedUri = resizeAndCompressImage(uri) ?: uri  // Use resized image if successful
        imageView.setImageURI(resizedUri)
        uploadToS3AndPlantNet(resizedUri)
    }

    private fun uploadToS3AndPlantNet(imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(this@ImageCapture, "Uploading to S3...", Toast.LENGTH_SHORT).show()

                withContext(Dispatchers.IO) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val imageName = "img_${timeStamp}.jpg"

                    val inputStream: InputStream = contentResolver.openInputStream(imageUri)!!
                    s3Uploader.uploadImage(inputStream, imageName)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ImageCapture, "S3 Upload Complete", Toast.LENGTH_SHORT).show()
                    uploadToPlantNet(imageUri)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ImageCapture, "S3 Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun uploadToPlantNet(imageUri: Uri) {
        ImageCropper.cropDetectedObject(this, imageUri) { croppedUri ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val finalUri = croppedUri ?: imageUri
                    Toast.makeText(this@ImageCapture, "Identifying plant...", Toast.LENGTH_SHORT).show()
                    Log.d("PlantNet", "Identifying plant...")

                    val result = withContext(Dispatchers.IO) {
                        plantNetUploader.identifyPlant(listOf(finalUri))
                    }

                    if (result != null && result.results.isNotEmpty()) {
                        val firstResult = result.results.first()
                        val scientificName = firstResult.species.scientificNameWithoutAuthor
                        val commonNames = firstResult.species.commonNames.joinToString(", ")

                        Toast.makeText(this@ImageCapture, "Plant: $scientificName\nCommon Name(s): $commonNames", Toast.LENGTH_LONG).show()
                        Log.d("PlantNet", "Identified: $scientificName, Common Names: $commonNames")
                    } else {
                        Toast.makeText(this@ImageCapture, "No plant identified", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ImageCapture, "PlantNet API failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    Log.e("PlantNet", "Error: ${e.localizedMessage}", e)
                }
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
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            processImageUri(imageUri)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImageUri(it) }
    }

    private fun setupImageUri() {
        val file = File(cacheDir, "captured_image.jpg")
        file.parentFile?.mkdirs()
        imageUri = FileProvider.getUriForFile(this, "com.example.greenspace.provider", file)
    }

    private fun resizeAndCompressImage(uri: Uri): Uri? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            val maxSize = 1024
            val resizedBitmap = resizeBitmap(originalBitmap, maxSize)
            saveBitmapToCache(resizedBitmap)
        } catch (e: Exception) {
            Log.e("ImageProcessing", "Error resizing image: ${e.message}", e)
            null
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "temp_image.jpg")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
        }
        return FileProvider.getUriForFile(this, "com.example.greenspace.provider", file)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
        return Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
    }
}
