package com.greenspaceapp.greenspace.mistralapi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MistralService {
    @POST("v1/chat/completions")
    fun getPlantInfo(@Body request: MistralRequest): Call<MistralResponse>
}
