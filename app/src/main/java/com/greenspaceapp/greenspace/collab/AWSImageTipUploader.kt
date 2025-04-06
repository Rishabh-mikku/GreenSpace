package com.greenspaceapp.greenspace.collab

import android.content.Context
import android.net.Uri
import android.util.Log
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.s3.AmazonS3Client
import com.greenspaceapp.greenspace.SharedPreference
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.concurrent.Executors

class AWSImageTipUploader(private val context: Context) {

    private val credentialsProvider = CognitoCachingCredentialsProvider(
        context, "us-east-1:521ff514-cbe0-4791-a818-7510a4b2c9c7", Regions.US_EAST_1
    )

    private val s3Client = AmazonS3Client(credentialsProvider)
    private val dynamoDBClient = AmazonDynamoDBClient(credentialsProvider)

    private val transferUtility = TransferUtility.builder()
        .context(context)
        .s3Client(s3Client)
        .build()

    private val executor = Executors.newSingleThreadExecutor()

    fun uploadImage(imageUri: Uri, callback: (String?) -> Unit) {
        val file = getFileFromUri(imageUri) ?: run {
            Log.e("AWSUpload", "Failed to convert URI to File")
            callback(null)
            return
        }

        val key = "uploads/${UUID.randomUUID()}.jpg"
        val uploadObserver = transferUtility.upload("greenspace-user-upload-images", key, file)

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                if (state == TransferState.COMPLETED) {
                    val imageUrl = "https://greenspace-user-upload-images.s3.us-east-1.amazonaws.com/$key"
                    callback(imageUrl)
                } else if (state == TransferState.FAILED) {
                    Log.e("AWSUpload", "Upload failed")
                    callback(null)
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}
            override fun onError(id: Int, ex: Exception?) {
                Log.e("AWSUpload", "Error: ${ex?.localizedMessage}")
                callback(null)
            }
        })
    }

    fun saveTipToDynamoDB(tipText: String, imageUrl: String) {
        executor.execute {
            try {
                val table = Table.loadTable(dynamoDBClient, "greenspace-tip-upload")
                val username = SharedPreference.getUsername(context) ?: "Anonymous"
                val userEmail = SharedPreference.getEmail(context) ?: "Anonymous"
                val tipData = TipData(
                    userEmail = userEmail,
                    tipText = tipText,
                    imageUrl = imageUrl,
                    username = username
                )

                val item = tipData.toDynamoDBDocument()
                table.putItem(item)
                Log.d("DynamoDB", "Tip saved successfully")
            } catch (e: Exception) {
                Log.e("DynamoDB", "Error saving tip: ${e.localizedMessage}")
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            inputStream?.close()
            tempFile
        } catch (e: Exception) {
            Log.e("FileConversion", "Error converting URI to File: ${e.localizedMessage}")
            null
        }
    }
}
