package com.example.characters.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.characters.R
import com.example.domain.model.CharacterModel

class CharacterAdapter(
    private val onItemClick: (CharacterModel) -> Unit
) : PagingDataAdapter<CharacterModel, CharacterViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CharacterModel>() {
            override fun areItemsTheSame(oldItem: CharacterModel, newItem: CharacterModel) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CharacterModel, newItem: CharacterModel) = oldItem == newItem
        }
    }
}

class CharacterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val iv = itemView.findViewById<ImageView>(R.id.ivAvatar)
    private val tvName = itemView.findViewById<TextView>(R.id.tvName)
    private val tvMeta = itemView.findViewById<TextView>(R.id.tvMeta)
    private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatusBadge)

    fun bind(c: CharacterModel) {
        tvName.text = c.name
        tvMeta.text = "${c.species} | ${c.gender}"
        tvStatus.text = c.status
        // Coil
        Glide.with(iv.context)
            .load(c.image)
            .centerCrop()
            .into(iv)
    }
}
