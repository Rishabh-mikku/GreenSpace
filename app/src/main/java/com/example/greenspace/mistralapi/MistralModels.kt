package com.example.greenspace.mistralapi

import com.google.gson.annotations.SerializedName

// Request Model
data class MistralRequest(
    val model: String,
    val messages: List<MessageRequest>,
    val temperature: Double = 0.7
)

data class MessageRequest(
    val role: String,
    val content: List<Content>
)

data class Content(
    val type: String,
    val text: String? = null,
    val image_url: String? = null
)

// Response Model (FIXED)
data class MistralResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: MessageResponse
)

data class MessageResponse(
    val role: String,

    // ✅ Fix: `content` is a String, NOT a List
    @SerializedName("content")
    val content: String
)
