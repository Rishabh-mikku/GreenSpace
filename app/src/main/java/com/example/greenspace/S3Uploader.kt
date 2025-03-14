import android.content.Context
import android.widget.Toast
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class S3Uploader(private val context: Context) {

    private val ACCESS_KEY = "AKIA4KXZOD4DLZD2HT77"
    private val SECRET_KEY = "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"
    private val BUCKET_NAME = "greenspace-images"
    private val REGION = "us-east-1"

    private val s3Client: AmazonS3Client by lazy {
        val credentials: AWSCredentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)
        AmazonS3Client(credentials, Region.getRegion(Regions.fromName(REGION)))
    }

    private val transferUtility: TransferUtility by lazy {
        TransferNetworkLossHandler.getInstance(context)
        TransferUtility.builder()
            .s3Client(s3Client)
            .context(context.applicationContext)
            .build()
    }

    // 🔹 Updated: Upload Image from InputStream
    suspend fun uploadImage(inputStream: InputStream, imageName: String) {
        withContext(Dispatchers.IO) { // Run on background thread
            try {
                val metadata = com.amazonaws.services.s3.model.ObjectMetadata().apply {
                    contentType = "image/jpeg"
                }

                s3Client.putObject(BUCKET_NAME, imageName, inputStream, metadata)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Upload Successful: $imageName", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Upload Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
