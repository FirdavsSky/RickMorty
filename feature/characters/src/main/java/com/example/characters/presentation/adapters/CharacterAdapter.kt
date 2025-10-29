package com.example.characters.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.characters.R
import com.example.domain.model.CharacterModel

class CharacterAdapter: PagingDataAdapter<CharacterModel, CharacterViewHolder>(DIFF) {

    private var itemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)

        holder.itemView.setOnClickListener {
            itemClickListener?.onItemClick(item)
        }
    }
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<CharacterModel>() {
            override fun areItemsTheSame(oldItem: CharacterModel, newItem: CharacterModel) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CharacterModel, newItem: CharacterModel) = oldItem == newItem
        }
    }

    fun setListener(itemClickListener: ItemClickListener){
        this.itemClickListener = itemClickListener
    }


    fun interface ItemClickListener { fun onItemClick(character: CharacterModel) }
}