package com.example.greenspace.plantnetapi

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PlantNetUploader(private val context: Context) {
    private val apiService = RetrofitInstance.api
    private val API_KEY = "2b10KnA7SLJgt01N4V3ZVJJYgu"

    suspend fun identifyPlant(imageUris: List<Uri>): PlantNetResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val imagesParts = mutableListOf<MultipartBody.Part>()
                val organsParts = mutableListOf<MultipartBody.Part>()

                for (uri in imageUris) {
                    val imageFile = uriToFile(uri)
                    val requestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    imagesParts.add(MultipartBody.Part.createFormData("images", imageFile.name, requestBody))

                    // Add corresponding organ
                    val organRequestBody = MultipartBody.Part.createFormData("organs", "leaf")  // Change as needed
                    organsParts.add(organRequestBody)
                }

                val project = "all" // ✅ Use a valid project or "all"

                val response = apiService.identifyPlant(project, imagesParts, organsParts, API_KEY)
                response
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }
}
