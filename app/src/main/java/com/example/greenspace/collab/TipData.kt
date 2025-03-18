package com.example.greenspace.collab

import android.content.Context
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive
import com.example.greenspace.SharedPreference
import java.util.*

data class TipData(
    var tipId: String = UUID.randomUUID().toString(),
    var userEmail: String = "",
    var tipText: String = "",
    var imageUrl: String = "",
    var timestamp: Long = System.currentTimeMillis(),
    var username: String = "" // Remove function call in default value
) {
    fun toDynamoDBDocument(): Document {
        val doc = Document()
        doc["tipId"] = Primitive(tipId)
        doc["userEmail"] = Primitive(userEmail)
        doc["tipText"] = Primitive(tipText)
        doc["imageUrl"] = Primitive(imageUrl)
        doc["timestamp"] = Primitive(timestamp)
        doc["username"] = Primitive(username)
        return doc
    }

    companion object {
        fun createWithUsername(context: Context, userId: String, tipText: String, imageUrl: String): TipData {
            val username = SharedPreference.getUsername(context) ?: "Unknown User"
            val email = SharedPreference.getEmail(context) ?: "Unknown Email"
            return TipData(userEmail = email, tipText = tipText, imageUrl = imageUrl, username = username)
        }
    }
}
