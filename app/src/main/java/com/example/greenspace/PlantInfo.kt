package com.example.greenspace

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PlantInfo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_info)

        val imageView: ImageView = findViewById(R.id.plantImageView)
        val scientificNameTextView: TextView = findViewById(R.id.tvScientificName)
        val commonNamesTextView: TextView = findViewById(R.id.tvCommonNames)
        val backButton: Button = findViewById(R.id.btnBack)

        // Get data from intent
        val imagePath: String? = intent.getStringExtra("image_path")  // Get file path
        val scientificName: String? = intent.getStringExtra("scientific_name")
        val commonNames: String? = intent.getStringExtra("common_names")

        // Load image from file path
        if (!imagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)  // Set the image correctly
        }

        scientificNameTextView.text = "Scientific Name: $scientificName"
        commonNamesTextView.text = "Common Names: $commonNames"

        backButton.setOnClickListener {
            val intent = Intent(this, ImageCapture::class.java)
            startActivity(intent)
            finish()
        }
    }
}
