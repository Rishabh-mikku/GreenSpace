package com.greenspaceapp.greenspace.screens

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.greenspaceapp.greenspace.R
import com.greenspaceapp.greenspace.collab.TipData

class TipAdapter(private val tips: List<TipData>) : RecyclerView.Adapter<TipAdapter.TipViewHolder>() {

    class TipViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val tipTextView: TextView = view.findViewById(R.id.tipTextView)
        val tipImageView: ImageView = view.findViewById(R.id.tipImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tip, parent, false)
        return TipViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TipViewHolder, position: Int) {
        val tip = tips[position]
        holder.usernameTextView.text = tip.username
        holder.tipTextView.text = "Tip: ${tip.tipText}"
        Glide.with(holder.itemView.context).load(tip.imageUrl).into(holder.tipImageView)
    }

    override fun getItemCount(): Int = tips.size
}
