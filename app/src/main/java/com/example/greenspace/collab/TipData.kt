package com.example.greenspace.collab

import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.DynamoDBEntry
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive
import java.util.*

data class TipData(
    var tipId: String = UUID.randomUUID().toString(),
    var userId: String = "",
    var tipText: String = "",
    var imageUrl: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    fun toDynamoDBDocument(): Document {
        val doc = Document()
        doc["tipId"] = Primitive(tipId) // Convert String to DynamoDBEntry
        doc["userId"] = Primitive(userId)
        doc["tipText"] = Primitive(tipText)
        doc["imageUrl"] = Primitive(imageUrl)
        doc["timestamp"] = Primitive(timestamp) // Convert Long to Primitive
        return doc
    }
}
