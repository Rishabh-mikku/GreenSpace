package com.example.greenspace.sharedspace

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
        private val joinButton: Button = itemView.findViewById(R.id.button_join_space)

        fun bind(space: SharedSpace) {
            spaceName.text = space.name
            spaceLocation.text = space.location

            joinButton.setOnClickListener {
                joinSharedSpace(space.spaceId)
            }
        }

        private fun joinSharedSpace(spaceId: String) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()

            db.collection("shared_spaces").document(spaceId)
                .update("users", FieldValue.arrayUnion(userId))
                .addOnSuccessListener {
                    Toast.makeText(itemView.context, "Joined space successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(itemView.context, "Failed to join space.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
