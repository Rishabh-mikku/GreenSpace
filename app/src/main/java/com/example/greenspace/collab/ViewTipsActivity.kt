package com.example.greenspace.screens

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.dynamodbv2.document.Expression
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Primitive
import com.amazonaws.regions.Regions
import com.example.greenspace.R
import com.example.greenspace.collab.TipData
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient


class ViewTipsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TipAdapter
    private val tipList = mutableListOf<TipData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_tips)

        recyclerView = findViewById(R.id.recyclerViewTips)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = TipAdapter(tipList)
        recyclerView.adapter = adapter

        fetchTipsFromDynamoDB()
    }

    private fun fetchTipsFromDynamoDB() {
        Thread {
            try {
                val credentialsProvider = CognitoCachingCredentialsProvider(
                    this, "us-east-1:521ff514-cbe0-4791-a818-7510a4b2c9c7", Regions.US_EAST_1
                )

                val dynamoDBClient = AmazonDynamoDBClient(credentialsProvider)
                val table = Table.loadTable(dynamoDBClient, "greenspace-tip-upload")

                val scanFilter = Expression()  // ✅ Empty expression to fetch all records
                val scanResults = table.scan(scanFilter).allResults  // ✅ Get all results as a list

                tipList.clear()

                // ✅ Now we can iterate over scanResults since it's a List<Document>
                for (item in scanResults) {
                    val tipData = TipData(
                        tipId = item.get("tipId")?.asString() ?: "",
                        userEmail = item.get("userEmail")?.asString() ?: "",
                        tipText = item.get("tipText")?.asString() ?: "",
                        imageUrl = item.get("imageUrl")?.asString() ?: "",
                        timestamp = item.get("timestamp")?.asLong() ?: 0,
                        username = item.get("username")?.asString() ?: "Anonymous"
                    )
                    tipList.add(tipData)
                }

                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.e("DynamoDB", "Error fetching tips: ${e.localizedMessage}")
            }
        }.start()
    }

}
