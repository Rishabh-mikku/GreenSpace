package com.example.greenspace.plantnetapi

import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface PlantNetService {
    @Multipart
    @POST("v2/identify/{project}")  // ✅ Correct endpoint
    suspend fun identifyPlant(
        @Path("project") project: String,  // ✅ Path parameter
        @Part images: List<MultipartBody.Part>,  // ✅ Multiple images
        @Part organs: List<MultipartBody.Part>,  // ✅ Organ list
        @Query("api-key") apiKey: String
    ): PlantNetResponse
}

data class PlantNetResponse(
    val results: List<PlantIdentificationResult>
)

data class PlantIdentificationResult(
    val score: Float,           // Confidence Score
    val species: Species
)

data class Species(
    val scientificNameWithoutAuthor: String,
    val commonNames: List<String>,
    val family: Family,                // Plant Family
    val wikipedia: Wikipedia            // Wikipedia Details
)

data class Wikipedia(
    val en: String?                     // English Wikipedia link
)

data class Family(
    val scientificNameWithoutAuthor: String
)

object RetrofitInstance {
    private const val BASE_URL = "https://my-api.plantnet.org/"

    val api: PlantNetService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlantNetService::class.java)
    }
}
