package com.example.launcher

import android.graphics.drawable.Drawable

data class AppItem(
    val packageName: String,
    val label: String,
    val className: String,
    val icon: Drawable? = null,
    val isFavorite: Boolean = false,
    val isHiddenInFocus: Boolean = false
)
