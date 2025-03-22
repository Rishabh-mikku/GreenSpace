package com.example.greenspace.screens

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.greenspace.R

class PlantInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_details)

        // Get data from Intent
        val plantInfo = intent.getStringExtra("PLANT_INFO") ?: "Plant Not Identified"
        val plantImageUrl = intent.getStringExtra("PLANT_IMAGE_URL") // Get Image URL

        // Display plant information
        displayPlantInfo(plantInfo)

        // Display plant image
        displayPlantImage(plantImageUrl)
    }

    /**
     * Function to parse and display plant details
     */
    private fun displayPlantInfo(plantInfo: String) {
        if (plantInfo == "Plant Not Identified") {
            findViewById<CardView>(R.id.cardNoPlant).visibility = View.VISIBLE
        } else {
            findViewById<CardView>(R.id.cardNoPlant).visibility = View.GONE

            val detailsMap = parsePlantInfo(plantInfo)

            setCardDetails(R.id.scientificNameCard, "Scientific Name", detailsMap["Scientific Name"])
            setCardDetails(R.id.commonNameCard, "Common Name", detailsMap["Common Name"])
            setCardDetails(R.id.familyCard, "Family", detailsMap["Family"])
            setCardDetails(R.id.nativeHabitatCard, "Native Habitat", detailsMap["Native Habitat"])
            setCardDetails(R.id.geographicalDistributionCard, "Geographical Distribution", detailsMap["Geographical Distribution"])
            setCardDetails(R.id.physicalDescriptionCard, "Physical Description", detailsMap["Physical Description"])
            setCardDetails(R.id.growthConditionsCard, "Growth Conditions", detailsMap["Growth Conditions"])
            setCardDetails(R.id.usesCard, "Uses", detailsMap["Uses"])
            setCardDetails(R.id.interestingFactsCard, "Interesting Facts", detailsMap["Interesting Facts"])
            setCardDetails(R.id.conservationStatusCard, "Conservation Status", detailsMap["Conservation Status"])
            setCardDetails(R.id.gardenTipsCard, "Garden Tips", detailsMap["Steps to Grow in a Garden"])
        }
    }

    /**
     * Function to display the plant image using Glide
     */
    private fun displayPlantImage(imageUrl: String?) {
        val imageView = findViewById<ImageView>(R.id.plantImageView)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.plant_image_placeholder) // Show a placeholder while loading
                .error(R.drawable.error_image) // Show an error image if loading fails
                .into(imageView)
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE // Hide ImageView if no image URL
        }
    }

    /**
     * Function to update a CardView's title and text dynamically
     */
    private fun setCardDetails(cardId: Int, title: String, detail: String?) {
        val cardView = findViewById<View>(cardId)
        cardView.findViewById<TextView>(R.id.tvTitle).text = title
        cardView.findViewById<TextView>(R.id.tvDetail).text = detail ?: "Not Available"

        // Apply padding and style dynamically
        cardView.setBackgroundResource(R.drawable.rounded_card)
    }

    /**
     * Function to parse the response string into a key-value map
     */
    /**
     * Function to parse the response string into a key-value map
     */
    private fun parsePlantInfo(response: String): Map<String, String> {
        val details = mutableMapOf<String, String>()
        val lines = response.split("\n").map { it.trim() }
        var currentKey = ""

        for (line in lines) {
            if (line.contains(":") && !line.startsWith("-")) { // New key found
                val parts = line.split(":", limit = 2)
                currentKey = parts[0].trim()
                details[currentKey] = parts.getOrNull(1)?.trim() ?: ""
            } else if (currentKey.isNotEmpty()) {
                // Append to previous key if it's part of multiline data (like Growth Conditions)
                details[currentKey] += "\n$line"
            }
        }

        return details
    }
}
