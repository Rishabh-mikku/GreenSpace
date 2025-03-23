package com.example.greenspace.sharedspace

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.greenspace.R
import com.example.greenspace.sharedspace.SharedSpace
import com.google.firebase.firestore.FirebaseFirestore

class SharedSpacesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SharedSpacesAdapter
    private val sharedSpacesList = mutableListOf<SharedSpace>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_spaces)

        recyclerView = findViewById(R.id.recycler_view_shared_spaces)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchSharedSpaces()
    }

    private fun fetchSharedSpaces() {
        db.collection("shared_spaces")
            .get()
            .addOnSuccessListener { documents ->
                sharedSpacesList.clear()
                for (document in documents) {
                    val space = document.toObject(SharedSpace::class.java)
                    sharedSpacesList.add(space)
                }
                adapter = SharedSpacesAdapter(sharedSpacesList)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting shared spaces: ", exception)
            }
    }
}
