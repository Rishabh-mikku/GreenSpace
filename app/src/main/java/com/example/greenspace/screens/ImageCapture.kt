package com.example.greenspace.screens

import com.example.greenspace.S3Upload.S3Uploader
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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.media3.common.util.Log
import com.example.greenspace.R
import com.example.greenspace.mistralapi.ApiClient
import com.example.greenspace.mistralapi.MistralRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import com.example.greenspace.mistralapi.Content
import com.example.greenspace.mistralapi.MessageRequest


class ImageCapture : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var profileBtn: ImageButton
    private lateinit var s3Uploader: S3Uploader
    private lateinit var plantNetUploader: PlantNetUploader
    private lateinit var imageUri: Uri
    private lateinit var collabBtn: ImageButton
    private lateinit var tipsImageBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_window)

        imageView = findViewById(R.id.scannerFrame)
        profileBtn = findViewById(R.id.btnProfile)
        s3Uploader = S3Uploader(this)
        plantNetUploader = PlantNetUploader(this)
        collabBtn = findViewById(R.id.btnAdd)
        tipsImageBtn = findViewById(R.id.btnTipsImage)

        setupImageUri()
        checkAndRequestPermissions()

        imageView.setOnClickListener {
            showImageSourceDialog()
        }

        profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileInfo::class.java))
        }

        collabBtn.setOnClickListener {
            startActivity(Intent(this, UploadImageTip::class.java))
        }

        tipsImageBtn.setOnClickListener {
            startActivity(Intent(this, ViewTipsActivity::class.java))
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
        uploadToS3AndPlantRecognition(resizedUri)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun uploadToS3AndPlantRecognition(imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Toast.makeText(this@ImageCapture, "Uploading to S3...", Toast.LENGTH_SHORT).show()

                val s3ImageUrl: String? = withContext(Dispatchers.IO) {
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val imageName = "img_${timeStamp}.jpg"

                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
                    if (inputStream == null) {
                        Log.e("S3 Upload", "Failed to open input stream")

                    } else {
                        s3Uploader.uploadImage(inputStream, imageName)  // ✅ Get the S3 URL
                    }
                }?.toString()

                if (!s3ImageUrl.isNullOrEmpty()) {
                    Log.d("S3 Upload", "Successfully uploaded: $s3ImageUrl")
                    Toast.makeText(this@ImageCapture, "S3 Upload Complete", Toast.LENGTH_SHORT).show()
                    uploadToMistralAPI(s3ImageUrl)  // ✅ Pass correct URL to Mistral API
                } else {
                    Log.e("S3 Upload", "S3 URL is null or empty")
                    Toast.makeText(this@ImageCapture, "S3 Upload Failed", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Log.e("S3 Upload", "Exception: ${e.message}", e)
                Toast.makeText(this@ImageCapture, "S3 Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun uploadToMistralAPI(s3ImageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = MistralRequest(
                    model = "pixtral-12b-2409",
                    messages = listOf(
                        MessageRequest(
                            role = "user",
                            content = listOf(
                                Content(type = "text", text = """
                              Identify this plant, leaf, or flower from the image and provide only the requested details in the exact format below. Ensure responses are factual, concise, and free from extra details.  

                              Common Name:  
                              Scientific Name:  
                              Family:  
                              Native Habitat:  
                              Geographical Distribution (Countries/Regions):  
                              Physical Description:  
                              Growth Conditions:  
                                - Temperature: (e.g., 20-30°C, Prefers warm climates but can tolerate various temperatures)  
                                - Light:  
                                - Water:  
                                - Soil:  
                              Uses (Exactly 4):  
                                1.  
                                2.  
                                3.  
                                4.  
                              Interesting Facts (Exactly 4):  
                                1.  
                                2.  
                                3.  
                                4.  
                              Conservation Status:  

                              Steps to Grow in a Garden:  
                              (If the plant is "not suitable" for garden cultivation, respond with "Can't be grown in a garden.")  
                                
                              If the plant is unidentifiable, respond with "Plant Not Identified." 
                        """.trimIndent()),
                                Content(type = "image_url", image_url = s3ImageUrl)
                            )
                        )
                    )
                )

                val call = ApiClient.instance.getPlantInfo(request)
                val response = call.execute()

                if (response.isSuccessful) {
                    val mistralResponse = response.body()
                    val plantInfo = mistralResponse?.choices?.get(0)?.message?.content ?: "Plant Not Identified"

                    withContext(Dispatchers.Main) {
                        Log.d("PlantInfo", "Plant Info: $plantInfo")
                        sendResultToPlantInfo(plantInfo, s3ImageUrl) // Pass both plant info & image
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("MistralAPI", "Error: $errorMessage")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ImageCapture, "Mistral API Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MistralAPI", "Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ImageCapture, "Mistral API failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Function to send plant info and image URL to PlantInfo.kt
     */
    private fun sendResultToPlantInfo(plantInfo: String, imageUrl: String) {
        val intent = Intent(this, PlantInfo::class.java).apply {
            putExtra("PLANT_INFO", plantInfo)
            putExtra("PLANT_IMAGE_URL", imageUrl) // Send image URL
        }
        startActivity(intent)
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

    @SuppressLint("UnsafeOptInUsageError")
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

    private fun saveImageLocally(uri: Uri): String {
        val file = File(cacheDir, "uploaded_image.jpg")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return file.absolutePath  // Return the local file path
    }

}
