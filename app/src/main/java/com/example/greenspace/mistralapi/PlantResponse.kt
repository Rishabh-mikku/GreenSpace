package com.example.greenspace.mistralapi

data class PlantResponse(
    val identified_plant: String,
    val scientific_name: String?,
    val family: String?,
    val common_name: String?,
    val habitat: String?,
    val distribution: List<String>?,
    val image_url: String?
)
