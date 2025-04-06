package com.greenspaceapp.greenspace.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.greenspaceapp.greenspace.R
import com.greenspaceapp.greenspace.collab.AWSImageTipUploader

class UploadImageTip : AppCompatActivity() {
    private lateinit var selectedImageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var uploadButton: Button
    private lateinit var tipEditText: EditText
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collab_upload)

        selectedImageView = findViewById(R.id.selectedImage)
        selectImageButton = findViewById(R.id.selectImageButton)
        uploadButton = findViewById(R.id.uploadButton)
        tipEditText = findViewById(R.id.tipEditText)

        selectImageButton.setOnClickListener { pickImage() }
        uploadButton.setOnClickListener { uploadDataToAWS() }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            selectedImageView.setImageURI(imageUri)
        }
    }

    private fun uploadDataToAWS() {
        val tipText = tipEditText.text.toString().trim()
        if (imageUri == null || tipText.isEmpty()) {
            Toast.makeText(this, "Please select an image and enter a tip!", Toast.LENGTH_SHORT).show()
            return
        }

        val awsUploader = AWSImageTipUploader(this)
        awsUploader.uploadImage(imageUri!!) { imageUrl ->
            if (imageUrl != null) {
                awsUploader.saveTipToDynamoDB(tipText, imageUrl)
                Toast.makeText(this, "Upload Successful!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Upload Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
