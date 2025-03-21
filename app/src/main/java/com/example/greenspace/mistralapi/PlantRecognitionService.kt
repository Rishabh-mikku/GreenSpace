package com.example.greenspace.mistralapi

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface PlantRecognitionService {
    @Multipart
    @POST("/identify-plant/")
    fun identifyPlant(@Part image: MultipartBody.Part): Call<PlantResponse>
}
