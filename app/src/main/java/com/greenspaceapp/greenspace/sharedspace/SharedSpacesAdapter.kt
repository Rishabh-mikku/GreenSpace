package com.greenspaceapp.greenspace.sharedspace

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.greenspaceapp.greenspace.R
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
        private val unjoinButton: Button = itemView.findViewById(R.id.button_unjoin_space)

        @SuppressLint("SetTextI18n")
        fun bind(space: SharedSpace) {
            spaceName.text = space.name
            spaceLocation.text = space.location
            spaceDescription.text = space.description
            spaceOwner.text = "Owner: ${space.owner}"

            loadUsersAndSlots(space.spaceId)

            joinButton.setOnClickListener {
                checkAndJoinSharedSpace(space.spaceId)
            }

            unjoinButton.setOnClickListener {
                unjoinSharedSpace(space.spaceId)
            }
        }

        private fun loadUsersAndSlots(spaceId: String) {
            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            spaceRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val usersList = document.get("users") as? List<String> ?: emptyList()
                    val availableSlots = document.getLong("availableSlots")?.toInt() ?: 0
                    val user = FirebaseAuth.getInstance().currentUser?.displayName ?: ""

                    spaceUsers.text = "Joined Users: ${usersList.joinToString(", ")}"
                    spaceAvailableSlot.text = "Available Slots: $availableSlots"

                    joinButton.visibility = if (usersList.contains(user) || availableSlots == 0) View.GONE else View.VISIBLE
                    unjoinButton.visibility = if (usersList.contains(user)) View.VISIBLE else View.GONE
                }
            }
        }

        private fun checkAndJoinSharedSpace(spaceId: String) {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val userName = user.displayName ?: "Unknown User"

            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            spaceRef.get().addOnSuccessListener { document ->
                val usersList = document.get("users") as? List<String> ?: emptyList()
                val availableSlots = document.getLong("availableSlots")?.toInt() ?: 0

                if (usersList.contains(userName)) {
                    Toast.makeText(itemView.context, "Already Joined", Toast.LENGTH_SHORT).show()
                } else if (availableSlots > 0) {
                    joinSharedSpace(spaceId, userName)
                } else {
                    Toast.makeText(itemView.context, "No available slots", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun joinSharedSpace(spaceId: String, userName: String) {
            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            spaceRef.update(
                "users", FieldValue.arrayUnion(userName),
                "availableSlots", FieldValue.increment(-1)
            ).addOnSuccessListener {
                Toast.makeText(itemView.context, "Joined space successfully!", Toast.LENGTH_SHORT).show()
                loadUsersAndSlots(spaceId)
            }
        }

        private fun unjoinSharedSpace(spaceId: String) {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val userName = user.displayName ?: "Unknown User"

            val db = FirebaseFirestore.getInstance()
            val spaceRef = db.collection("shared_spaces").document(spaceId)

            spaceRef.update(
                "users", FieldValue.arrayRemove(userName),
                "availableSlots", FieldValue.increment(1)
            ).addOnSuccessListener {
                Toast.makeText(itemView.context, "Unjoined successfully!", Toast.LENGTH_SHORT).show()
                loadUsersAndSlots(spaceId)
            }
        }
    }
}
