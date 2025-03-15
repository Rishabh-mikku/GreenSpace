package com.example.greenspace

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PlantInfo : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_info)

        val imageView: ImageView = findViewById(R.id.plantImageView)
        val scientificNameTextView: TextView = findViewById(R.id.tvScientificName)
        val commonNamesTextView: TextView = findViewById(R.id.tvCommonNames)
        val familyTextView: TextView = findViewById(R.id.tvFamily)
        val confidenceTextView: TextView = findViewById(R.id.tvConfidence)
        val wikiButton: Button = findViewById(R.id.btnWiki)
        val backButton: Button = findViewById(R.id.btnBack)
        val noPlantTextView: TextView = findViewById(R.id.tvNoPlant)

        val scientificName: String? = intent.getStringExtra("scientific_name")
        val imagePath: String? = intent.getStringExtra("image_path")

        if (scientificName == "No Plant Identified") {
            // If confidence is below 15%, show "No Plant Identified"
            // Load image from file path
            if (!imagePath.isNullOrEmpty()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                imageView.setImageBitmap(bitmap)
            }
            noPlantTextView.text = "No Plant Identified"
            scientificNameTextView.visibility = View.GONE
            commonNamesTextView.visibility = View.GONE
            familyTextView.visibility = View.GONE
            confidenceTextView.visibility = View.GONE
            wikiButton.visibility = View.GONE
        } else {

            // Get data from intent
            val commonNames: String? = intent.getStringExtra("common_names")
            val family: String? = intent.getStringExtra("family")
            val confidence: Float = intent.getFloatExtra("confidence", -1.0f)
            val wikiLink: String? = intent.getStringExtra("wiki_link")

            // Load image from file path
            if (!imagePath.isNullOrEmpty()) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                imageView.setImageBitmap(bitmap)
            }

            noPlantTextView.visibility = View.GONE
            scientificNameTextView.text = "Scientific Name: $scientificName"
            commonNamesTextView.text = "Common Names: $commonNames"
            familyTextView.text = "Family: ${family ?: "Not available"}"
            confidenceTextView.text = if (confidence >= 0) {
                "Confidence: ${"%.2f".format(confidence)}%"
            } else {
                "Confidence: Not available"
            }

            // Show Wikipedia button only if link is available
            if (!wikiLink.isNullOrEmpty() && wikiLink != "Not available") {
                wikiButton.visibility = View.VISIBLE
                wikiButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wikiLink))
                    startActivity(intent)
                }
            } else {
                wikiButton.visibility = View.GONE
            }
        }

        // Back button logic
        backButton.setOnClickListener {
            val intent = Intent(this, ImageCapture::class.java)
            startActivity(intent)
            finish()
        }
    }
}
