package com.eventbox.app.android.data

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.eventbox.app.android.EventboxEntryPoint

class Resource {

    private val context by lazy {
        EventboxEntryPoint.appContext
    }

    fun getString(@StringRes resId: Int) = context?.getString(resId)

    fun getString(@StringRes resId: Int, vararg args: Any?) = context?.getString(resId, args)

    fun getColor(@ColorRes resId: Int) = context?.resources?.getColor(resId)
}
