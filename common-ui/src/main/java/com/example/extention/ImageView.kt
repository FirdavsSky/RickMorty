package com.example.extention

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

fun ImageView.loadImageWithProgress(
    url: String?,
    progressBar: ProgressBar,
    placeholderRes: Int
) {
    progressBar.visibility = View.VISIBLE
    this.visibility = View.INVISIBLE

    Glide.with(this.context)
        .load(url)
        .placeholder(placeholderRes)
        .centerCrop()
        .listener(object : RequestListener<android.graphics.drawable.Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<android.graphics.drawable.Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility = View.GONE
                this@loadImageWithProgress.visibility = View.VISIBLE
                return true
            }

            override fun onResourceReady(
                resource: android.graphics.drawable.Drawable?,
                model: Any?,
                target: Target<android.graphics.drawable.Drawable>?,
                dataSource: com.bumptech.glide.load.DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility = View.GONE
                this@loadImageWithProgress.visibility = View.VISIBLE
                return false
            }
        })
        .into(this)
}
