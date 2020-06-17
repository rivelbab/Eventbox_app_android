package com.eventbox.app.android.adapters

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.eventbox.app.android.R
import com.eventbox.app.android.utils.nullToEmpty
import com.eventbox.app.android.utils.stripHtml
import timber.log.Timber

/**
 * Toggle Visibility of the view depending upn the value is null or not.
 */
@BindingAdapter("hideIfEmpty")
fun setHideIfEmpty(view: View, value: String?) {
    view.visibility = if (!value.isNullOrEmpty()) View.VISIBLE else View.GONE
}

/**
 * Set Html Text by chaging the value using String Extensions in Utils.
 */
@BindingAdapter("strippedHtml")
fun TextView.setStrippedHtml(value: String?) {
    this.text = value.nullToEmpty().stripHtml()?.trim()
}

@BindingAdapter("avatarUrl")
fun setAvatarUrl(imageView: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_person_black)
        .into(imageView, object : Callback {
            override fun onSuccess() {
                imageView.tag = "image_loading_success"
            }

            override fun onError(e: Exception?) {
                Timber.e(e)
            }
        })
}

@BindingAdapter("eventImage")
fun setEventImage(imageView: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.header)
        .into(imageView)
}

@BindingAdapter("isFavorite")
fun setFavorite(fab: FloatingActionButton, isFavorite: Boolean) {
    fab.setImageResource(
        if (isFavorite) R.drawable.ic_baseline_favorite else R.drawable.ic_baseline_favorite_border)
}

@BindingAdapter("expiredTicket")
fun setExpired(imageView: ImageView, isExpired: Boolean) {
    if (isExpired) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0F)
        imageView.colorFilter = ColorMatrixColorFilter(matrix)
    }
}
