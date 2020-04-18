package com.eventbox.app.android.networks.api

import com.eventbox.app.android.models.settings.Settings
import io.reactivex.Single
import retrofit2.http.GET

interface SettingsApi {

    @GET("settings")
    fun getSettings(): Single<Settings>
}
