package com.example.greenspace.sharedspace

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.greenspace.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SharedSpacesAdapter(private val sharedSpaces: List<SharedSpace>) :
    RecyclerView.Adapter<SharedSpacesAdapter.SharedSpaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedSpaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_space, parent, false)
        return SharedSpaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: SharedSpaceViewHolder, position: Int) {
        val space = sharedSpaces[position]
        holder.bind(space)
    }

    override fun getItemCount(): Int = sharedSpaces.size

    inner class SharedSpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val spaceName: TextView = itemView.findViewById(R.id.space_name)
        private val spaceLocation: TextView = itemView.findViewById(R.id.space_location)
        private val spaceDescription: TextView = itemView.findViewById(R.id.space_description)
        private val spaceOwner: TextView = itemView.findViewById(R.id.space_owner)
        private val spaceAvailableSlot: TextView = itemView.findViewById(R.id.space_slot)
        private val spaceUsers: TextView = itemView.findViewById(R.id.space_users)
        private val joinButton: Button = itemView.findViewById(R.id.button_join_space)

        @SuppressLint("SetTextI18n")
        fun bind(space: SharedSpace) {
            spaceName.text = space.name
            spaceLocation.text = space.location
            spaceDescription.text = space.description
            spaceOwner.text = "Owner: ${space.owner}"

            // Hide available slots field if slots are 0
            if (space.availableSlots > 0) {
                spaceAvailableSlot.text = "Available Slots: ${space.availableSlots}"
                spaceAvailableSlot.visibility = View.VISIBLE
            } else {
                spaceAvailableSlot.visibility = View.GONE
                joinButton.visibility = View.GONE // Hide join button if no slots are left
            }

            loadUsers(space.spaceId)

            joinButton.setOnClickListener {
                joinSharedSpace(space.spaceId)
            }
        }

        private fun loadUsers(spaceId: String) {
            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            spaceRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val usersList = document.get("users") as? List<String> ?: emptyList()

                    if (usersList.isNotEmpty()) {
                        spaceUsers.text = "Joined Users: ${usersList.joinToString(", ")}"
                        spaceUsers.visibility = View.VISIBLE
                    } else {
                        spaceUsers.visibility = View.GONE
                    }
                }
            }
        }

        private fun joinSharedSpace(spaceId: String) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(itemView.context, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val userName = user.displayName ?: "Unknown User"
            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(spaceRef)
                val currentSlots = snapshot.getLong("availableSlots") ?: return@runTransaction

                if (currentSlots > 0) {
                    transaction.update(spaceRef, "users", FieldValue.arrayUnion(userName))
                    transaction.update(spaceRef, "availableSlots", FieldValue.increment(-1))
                } else {
                    throw Exception("No available slots left")
                }
            }.addOnSuccessListener {
                Toast.makeText(itemView.context, "Joined space successfully!", Toast.LENGTH_SHORT).show()
                loadUsers(spaceId) // Refresh user list
                updateAvailableSlotsUI(spaceId) // Refresh available slots
            }.addOnFailureListener { e ->
                Toast.makeText(itemView.context, "Failed to join space: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        private fun updateAvailableSlotsUI(spaceId: String) {
            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            spaceRef.get().addOnSuccessListener { document ->
                val updatedSlots = document.getLong("availableSlots") ?: 0
                if (updatedSlots > 0) {
                    spaceAvailableSlot.text = "Available Slots: $updatedSlots"
                    spaceAvailableSlot.visibility = View.VISIBLE
                } else {
                    spaceAvailableSlot.visibility = View.GONE
                    joinButton.visibility = View.GONE
                }
            }
        }
    }
}
