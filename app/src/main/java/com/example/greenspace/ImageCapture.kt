package com.example.greenspace

import S3Uploader
import com.example.greenspace.plantnetapi.PlantNetUploader
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.media3.common.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.greenspace.plantnetapi.ImageCropper

class ImageCapture : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var profileBtn: ImageButton
    private lateinit var s3Uploader: S3Uploader
    private lateinit var plantNetUploader: PlantNetUploader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)

        imageView = findViewById(R.id.scannerFrame)
        profileBtn = findViewById(R.id.btnProfile)
        s3Uploader = S3Uploader(this)
        plantNetUploader = PlantNetUploader(this)

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

    private fun processImageUri(uri: Uri) {
        imageView.setImageURI(uri)
        uploadToS3AndPlantNet(uri)
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
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val loadingMessage = "Identifying plant..."
                Toast.makeText(this@ImageCapture, loadingMessage, Toast.LENGTH_SHORT).show()
                Log.d("PlantNet", loadingMessage)

                val bitmap = withContext(Dispatchers.IO) {
                    ImageCropper.uriToBitmap(this@ImageCapture, imageUri)
                }

                if (bitmap != null) {
                    val croppedBitmap = ImageCropper.cropCenterSquare(bitmap)  // Apply cropping
                    val croppedUri = ImageCropper.saveCroppedBitmapToCache(this@ImageCapture, croppedBitmap)

                    val result = withContext(Dispatchers.IO) {
                        plantNetUploader.identifyPlant(listOf(croppedUri))  // ✅ Upload cropped image
                    }

                    if (result != null && result.results.isNotEmpty()) {
                        val firstResult = result.results.first()
                        val scientificName = firstResult.species.scientificNameWithoutAuthor
                        val commonNames = firstResult.species.commonNames

                        val commonNameText = if (commonNames.isNotEmpty()) {
                            commonNames.joinToString(", ")
                        } else {
                            "Unknown"
                        }

                        val message = "Plant: $scientificName\nCommon Name(s): $commonNameText"
                        Toast.makeText(this@ImageCapture, message, Toast.LENGTH_LONG).show()
                        Log.d("PlantNet", message)
                    } else {
                        val noPlantMessage = "No plant identified"
                        Toast.makeText(this@ImageCapture, noPlantMessage, Toast.LENGTH_LONG).show()
                        Log.d("PlantNet", noPlantMessage)
                    }
                } else {
                    Toast.makeText(this@ImageCapture, "Failed to process image", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                val errorMessage = "PlantNet API failed: ${e.localizedMessage}"
                Toast.makeText(this@ImageCapture, errorMessage, Toast.LENGTH_LONG).show()
                Log.e("PlantNet", errorMessage, e)
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
        bitmap?.let {
            val uri = saveBitmapToCache(it)  // Convert Bitmap to Uri
            processImageUri(uri)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { processImageUri(it) }
    }

    private fun saveBitmapToCache(bitmap: android.graphics.Bitmap): Uri {
        val file = File(cacheDir, "temp_image.jpg")
        file.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, it) }
        return Uri.fromFile(file)
    }
}
