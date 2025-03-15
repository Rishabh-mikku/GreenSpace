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
import com.bumptech.glide.Glide
import org.w3c.dom.Text

class PlantInfo : AppCompatActivity() {
    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_info)

        val imageView: ImageView = findViewById(R.id.plantImageView)
        val scientificNameTextView: TextView = findViewById(R.id.tvScientificName)
        val commonNamesTextView: TextView = findViewById(R.id.tvCommonNames)
        val backButton: Button = findViewById(R.id.btnBack)
        val familyTextView: TextView = findViewById(R.id.tvFamily)
        val confidenceTextView: TextView = findViewById(R.id.tvConfidence)
        val wikiButton: Button = findViewById(R.id.btnWiki)

        // Get data from intent
        val imagePath: String? = intent.getStringExtra("image_path")  // Get file path
        val scientificName: String? = intent.getStringExtra("scientific_name")
        val commonNames: String? = intent.getStringExtra("common_names")
        val family: String? = intent.getStringExtra("family")
        val confidence: Float = intent.getFloatExtra("confidence", -1.0f) // Default to 0.0%
        val wikiLink: String? = intent.getStringExtra("wiki_link")

        // Load image from file path
        if (!imagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imageView.setImageBitmap(bitmap)  // Set the image correctly
        }

        scientificNameTextView.text = "Scientific Name: $scientificName"
        commonNamesTextView.text = "Common Names: $commonNames"
        familyTextView.text = "Family: ${family ?: "Not available"}"
        confidenceTextView.text = if (confidence >= 0) {
            "Confidence: ${"%.2f".format(confidence)}%"
        } else {
            "Confidence: Not available"
        }
        if (!wikiLink.isNullOrEmpty() && wikiLink != "Not available") {
            wikiButton.visibility = View.VISIBLE  // Show button only if link is available
            wikiButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(wikiLink))
                startActivity(intent)
            }
        } else {
            wikiButton.visibility = View.GONE  // Hide button if no link is available
        }

        backButton.setOnClickListener {
            val intent = Intent(this, ImageCapture::class.java)
            startActivity(intent)
            finish()
        }
    }
}
