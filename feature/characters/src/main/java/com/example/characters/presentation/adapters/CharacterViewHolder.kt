package com.example.characters.presentation.adapters

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.characters.R
import com.example.domain.model.CharacterModel
import com.example.extention.loadImageWithProgress
import com.example.extention.setStatus

class CharacterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val iv = itemView.findViewById<ImageView>(R.id.ivAvatar)
    private val tvName = itemView.findViewById<TextView>(R.id.tvName)
    private val tvMeta = itemView.findViewById<TextView>(R.id.tvMeta)
    private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatusBadge)
    private val progressBarItem = itemView.findViewById<ProgressBar>(R.id.progressBarItem)

    fun bind(character: CharacterModel) {
        tvName.text = character.name
        tvMeta.text = "${character.species} | ${character.gender}"
        tvStatus.text = character.status


        tvStatus.setStatus(character.status)

        progressBarItem.visibility = View.VISIBLE
        iv.visibility = View.INVISIBLE

        iv.loadImageWithProgress(
            character.image,
            progressBarItem,
            com.example.common_ui.R.drawable.rick_morty_placeholder
        )
    }
}