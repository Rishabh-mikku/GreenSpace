package com.example.greenspace.sharedspace

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.greenspace.R
import com.example.greenspace.sharedspace.SharedSpace
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions

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
        private val joinButton: Button = itemView.findViewById(R.id.button_join_space)

        @SuppressLint("SetTextI18n")
        fun bind(space: SharedSpace) {
            spaceName.text = space.name
            spaceLocation.text = space.location
            spaceDescription.text = space.description
            spaceOwner.text = space.owner
            spaceAvailableSlot.text = space.availableSlots.toString()

            joinButton.setOnClickListener {
                joinSharedSpace(space.spaceId)
            }
        }

        private fun joinSharedSpace(spaceId: String) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(itemView.context, "Error: User not authenticated", Toast.LENGTH_SHORT).show()
                return
            }

            val userId = user.uid
            val userName = user.displayName ?: "Unknown User"  // Fallback in case name is null
            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            val userData = mapOf(
                "uid" to userId,
                "name" to userName
            )

            db.runTransaction { transaction ->
                val snapshot = transaction.get(spaceRef)
                val currentSlots = snapshot.getLong("availableSlots") ?: return@runTransaction

                if (currentSlots > 0) {
                    // Update the users list as an array of user objects (UID + Name)
                    transaction.update(spaceRef, "users", FieldValue.arrayUnion(userData))
                    transaction.update(spaceRef, "availableSlots", FieldValue.increment(-1))
                } else {
                    throw Exception("No available slots left")
                }
            }.addOnSuccessListener {
                Toast.makeText(itemView.context, "Joined space successfully!", Toast.LENGTH_SHORT).show()
                spaceAvailableSlot.text = (spaceAvailableSlot.text.toString().toInt() - 1).toString()
            }.addOnFailureListener { e ->
                Toast.makeText(itemView.context, "Failed to join space: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }


    }
}
