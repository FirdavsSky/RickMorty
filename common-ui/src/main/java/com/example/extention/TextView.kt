package com.example.extention

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.conts.CharacterColors
import com.conts.CharacterStrings

fun TextView.setStatus(status: String) {
    val color = when (status) {
        CharacterStrings.STATUS_ALIVE -> ContextCompat.getColor(context, CharacterColors.STATUS_ALIVE)
        CharacterStrings.STATUS_DEAD -> ContextCompat.getColor(context, CharacterColors.STATUS_DEAD)
        else -> ContextCompat.getColor(context, CharacterColors.STATUS_UNKNOWN)
    }

    this.text = status

    val backgroundDrawable = this.background
    if (backgroundDrawable is GradientDrawable) {
        backgroundDrawable.mutate()
        backgroundDrawable.setColor(color)
        this.background = backgroundDrawable
    }
}
