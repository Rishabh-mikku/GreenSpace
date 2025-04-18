package com.greenspaceapp.greenspace.sharedspace

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.greenspaceapp.greenspace.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateSharedSpaceActivity : AppCompatActivity() {

    private lateinit var spaceName: EditText
    private lateinit var spaceLocation: EditText
    private lateinit var spaceDescription: EditText
    private lateinit var availableSlots: EditText
    private lateinit var ownerName: EditText
    private lateinit var createButton: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_shared_space)

        spaceName = findViewById(R.id.edit_text_space_name)
        spaceLocation = findViewById(R.id.edit_text_space_location)
        spaceDescription = findViewById(R.id.edit_text_space_description)
        availableSlots = findViewById(R.id.edit_text_available_slots)
        createButton = findViewById(R.id.button_create_space)
        ownerName = findViewById(R.id.edit_text_owner)

        createButton.setOnClickListener {
            createSharedSpace()
        }
    }

    private fun createSharedSpace() {
        val name = spaceName.text.toString().trim()
        val location = spaceLocation.text.toString().trim()
        val description = spaceDescription.text.toString().trim()
        val slots = availableSlots.text.toString().trim()
        val owner = ownerName.text.toString().trim()

        if (name.isEmpty() || location.isEmpty() || description.isEmpty() || slots.isEmpty() || owner.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"
        val spaceId = db.collection("shared_spaces").document().id
        val sharedSpace = SharedSpace(
            spaceId = spaceId,
            name = name,
            location = location,
            description = description,
            owner = owner,
            availableSlots = slots.toInt(),
            users = listOf()
        )

        db.collection("shared_spaces").document(spaceId)
            .set(sharedSpace)
            .addOnSuccessListener {
                Toast.makeText(this, "Shared space created!", Toast.LENGTH_SHORT).show()
                finish()  // Close the activity after creation
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
