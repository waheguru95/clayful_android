package com.health.clayful.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.health.clayful.R

object ObjectConverter {

    @JvmStatic
    @BindingAdapter("android:setLoadingImage")
    fun ImageView.loadImage(value : String) {
        Glide.with(this).load(R.drawable.loading_anim).into(this)
    }
}