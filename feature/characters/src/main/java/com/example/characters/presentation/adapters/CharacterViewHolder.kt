package com.example.characters.presentation.adapters

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.conts.CharacterColors
import com.conts.CharacterStrings
import com.example.characters.R
import com.example.domain.model.CharacterModel

class CharacterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val iv = itemView.findViewById<ImageView>(R.id.ivAvatar)
    private val tvName = itemView.findViewById<TextView>(R.id.tvName)
    private val tvMeta = itemView.findViewById<TextView>(R.id.tvMeta)
    private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatusBadge)
    private val progressBarItem = itemView.findViewById<ProgressBar>(R.id.progressBarItem)

    fun bind(c: CharacterModel) {
        tvName.text = c.name
        tvMeta.text = "${c.species} | ${c.gender}"
        tvStatus.text = c.status

        val color = when (c.status) {
            CharacterStrings.STATUS_ALIVE -> ContextCompat.getColor(itemView.context, CharacterColors.STATUS_ALIVE)
            CharacterStrings.STATUS_DEAD -> ContextCompat.getColor(itemView.context, CharacterColors.STATUS_DEAD)
            CharacterStrings.STATUS_UNKNOWN -> ContextCompat.getColor(itemView.context, CharacterColors.STATUS_UNKNOWN)
            else -> ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
        }
        val drawable = tvStatus.background.mutate() as GradientDrawable
        drawable.setColor(color)
        tvStatus.background = drawable

        progressBarItem.visibility = View.VISIBLE
        iv.visibility = View.INVISIBLE

        Glide.with(iv.context)
            .load(c.image)
            .placeholder(com.example.common_ui.R.drawable.rick_morty_placeholder)
            .centerCrop()
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBarItem.visibility = View.GONE
                    iv.visibility = View.VISIBLE
                    return true
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBarItem.visibility = View.GONE
                    iv.visibility = View.VISIBLE
                    return false
                }
            })
            .into(iv)
    }
}