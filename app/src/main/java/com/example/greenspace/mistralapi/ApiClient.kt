package com.example.greenspace.mistralapi

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://api.mistral.ai/"
    private const val API_KEY = "jKAQHnO8k2KoQnfKi8TeebaqMoHYhxmo"  // 🔹 Replace with actual API key

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain: Interceptor.Chain ->
            val request: Request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $API_KEY")  // ✅ Fix: Use Interceptor for headers
                .addHeader("Content-Type", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    val instance: MistralService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // ✅ Fix: Attach client with headers
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MistralService::class.java)
    }
}
