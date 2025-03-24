package com.example.greenspace.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.greenspace.R

class PlantInfo : AppCompatActivity() {

    private lateinit var backBtn: Button
    private lateinit var noCardPlant: TextView

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

        // Back button implementation
        backBtn = findViewById(R.id.btnBack)
        backBtn.setOnClickListener {
            startActivity(Intent(this, ImageCapture::class.java))
        }
    }

    /**
     * Function to parse and display plant details
     */
    private fun displayPlantInfo(plantInfo: String) {
        if (plantInfo.equals("Plant Not Identified.")) {
            noCardPlant = findViewById(R.id.tvNoPlantIdentified)
            noCardPlant.visibility = View.VISIBLE
            Log.d("PLANT INFO: ", "No Plant Identified")

            findViewById<CardView>(R.id.scientificNameCard).visibility = View.GONE
            findViewById<CardView>(R.id.commonNameCard).visibility = View.GONE
            findViewById<CardView>(R.id.familyCard).visibility = View.GONE
            findViewById<CardView>(R.id.nativeHabitatCard).visibility = View.GONE
            findViewById<CardView>(R.id.geographicalDistributionCard).visibility = View.GONE
            findViewById<CardView>(R.id.physicalDescriptionCard).visibility = View.GONE
            findViewById<CardView>(R.id.usesCard).visibility = View.GONE
            findViewById<CardView>(R.id.interestingFactsCard).visibility = View.GONE
            findViewById<CardView>(R.id.conservationStatusCard).visibility = View.GONE
            findViewById<CardView>(R.id.gardenTipsCard).visibility = View.GONE
            findViewById<CardView>(R.id.tempCard).visibility = View.GONE
            findViewById<CardView>(R.id.lightCard).visibility = View.GONE
            findViewById<CardView>(R.id.waterCard).visibility = View.GONE
            findViewById<CardView>(R.id.soilCard).visibility = View.GONE
            findViewById<CardView>(R.id.growthConditionsCard).visibility = View.GONE
        } else {
            findViewById<CardView>(R.id.tvNoPlantIdentified).visibility = View.GONE

            val detailsMap = parsePlantInfo(plantInfo)

            setCardDetails(R.id.scientificNameCard, "Scientific Name", detailsMap["Scientific Name"])
            setCardDetails(R.id.commonNameCard, "Common Name", detailsMap["Common Name"])
            setCardDetails(R.id.familyCard, "Family", detailsMap["Family"])
            setCardDetails(R.id.nativeHabitatCard, "Native Habitat", detailsMap["Native Habitat"])
            setCardDetails(R.id.geographicalDistributionCard, "Geographical Distribution", detailsMap["Geographical Distribution (Countries/Regions)"])
            setCardDetails(R.id.physicalDescriptionCard, "Physical Description", detailsMap["Physical Description"])
            setCardDetails(R.id.usesCard, "Uses", detailsMap["Uses"])
            setCardDetails(R.id.interestingFactsCard, "Interesting Facts", detailsMap["Interesting Facts"])
            setCardDetails(R.id.conservationStatusCard, "Conservation Status", detailsMap["Conservation Status"])
            setCardDetails(R.id.gardenTipsCard, "Steps to Grow in a Garden", detailsMap["Steps to Grow in a Garden"])

            // Set individual Growth Condition details with Emojis 🌡️ ☀️ 💧 🌱
            setCardDetails(R.id.tempCard, "Temperature 🌡️", detailsMap["Temperature"])
            setCardDetails(R.id.lightCard, "Light ☀️", detailsMap["Light"])
            setCardDetails(R.id.waterCard, "Water 💧", detailsMap["Water"])
            setCardDetails(R.id.soilCard, "Soil 🌱", detailsMap["Soil"])
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
        val cardView = findViewById<View>(cardId) as? CardView
        if (cardView == null) {
            Log.e("PlantInfo", "⚠️ CardView with ID $cardId not found!")
            return
        }

        when (cardId) {
            R.id.tempCard -> cardView.findViewById<TextView>(R.id.tvTemp)?.text = "$title: $detail 🌡️"
            R.id.lightCard -> cardView.findViewById<TextView>(R.id.tvLight)?.text = "$title: $detail ☀️"
            R.id.waterCard -> cardView.findViewById<TextView>(R.id.tvWater)?.text = "$title: $detail 💧"
            R.id.soilCard -> cardView.findViewById<TextView>(R.id.tvSoil)?.text = "$title: $detail 🌱"
            else -> {
                val titleView = cardView.findViewById<TextView>(R.id.tvTitle)
                val detailView = cardView.findViewById<TextView>(R.id.tvDetail)

                if (titleView == null || detailView == null) {
                    Log.e("PlantInfo", "⚠️ TextViews missing inside CardView $cardId!")
                    return
                }

                titleView.text = title
                detailView.text = detail ?: "Not Available"
            }
        }
    }

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
                // Append to previous key if it's part of multiline data
                details[currentKey] += "\n$line"
            }
        }

        // Extract Growth Conditions sub-attributes
        val growthConditions = details["Growth Conditions"] ?: ""
        details["Temperature"] = extractConditionDetail(growthConditions, "Temperature")
        details["Light"] = extractConditionDetail(growthConditions, "Light")
        details["Water"] = extractConditionDetail(growthConditions, "Water")
        details["Soil"] = extractConditionDetail(growthConditions, "Soil")

        return details
    }

    /**
     * Helper function to extract specific Growth Conditions details
     */
    private fun extractConditionDetail(growthConditions: String, keyword: String): String {
        val regex = Regex("$keyword:\\s*([^\n]+)", RegexOption.IGNORE_CASE)
        return regex.find(growthConditions)?.groupValues?.get(1)?.trim() ?: "Not Available"
    }

    fun backButton() {
        startActivity(Intent(this, ImageCapture::class.java))
    }
}
