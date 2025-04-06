package com.greenspaceapp.greenspace.S3Upload

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.regions.Regions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class S3Uploader(private val context: Context) {

    private val BUCKET_NAME = "greenspace-images"
    private val IDENTITY_POOL_ID = "us-east-1:521ff514-cbe0-4791-a818-7510a4b2c9c7"

    private val credentialsProvider: CognitoCachingCredentialsProvider by lazy {
        CognitoCachingCredentialsProvider(
            context,
            IDENTITY_POOL_ID,
            Regions.US_EAST_1
        )
    }

    private val s3Client: AmazonS3Client by lazy {
        AmazonS3Client(credentialsProvider)
    }

    private val transferUtility: TransferUtility by lazy {
        TransferNetworkLossHandler.getInstance(context)
        TransferUtility.builder()
            .s3Client(s3Client)
            .context(context.applicationContext)
            .build()
    }

    // 🔹 Updated: Upload Image from InputStream
    suspend fun uploadImage(inputStream: InputStream, imageName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = com.amazonaws.services.s3.model.ObjectMetadata().apply {
                    contentType = "image/jpeg"
                }

                // ✅ Upload Image
                s3Client.putObject(BUCKET_NAME, imageName, inputStream, metadata)

                // ✅ Construct the Public S3 URL
                val s3Url = "https://$BUCKET_NAME.s3.amazonaws.com/$imageName"

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Upload Successful: $s3Url", Toast.LENGTH_SHORT).show()
                }

                Log.d("S3 Upload", "Uploaded to: $s3Url") // Debug log
                s3Url  // ✅ Return the URL
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("S3 Upload", "Upload failed: ${e.message}", e)
                null  // ✅ Return null if upload fails
            }
        }
    }

}
