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
    val species: Species
)

data class Species(
    val scientificNameWithoutAuthor: String,
    val commonNames: List<String>
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
