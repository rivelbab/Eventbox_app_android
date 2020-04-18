package com.eventbox.app.android.service

import com.eventbox.app.android.data.dao.SettingsDao
import com.eventbox.app.android.models.settings.Settings
import com.eventbox.app.android.networks.api.SettingsApi
import io.reactivex.Single

class SettingsService(
    private val settingsApi: SettingsApi,
    private val settingsDao: SettingsDao
) {

    fun fetchSettings(): Single<Settings> {
        return settingsApi.getSettings().map {
            settingsDao.insertSettings(it)
            it
        }.flatMap {
            settingsDao.getSettings()
        }
    }
}
