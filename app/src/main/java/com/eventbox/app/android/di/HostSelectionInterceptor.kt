package com.eventbox.app.android.di

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.ui.settings.API_URL

class HostSelectionInterceptor(private val preference: Preference) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var original = chain.request()
        val httpUrl = preference.getString(API_URL)?.toHttpUrlOrNull()
        if (original.url.host == BuildConfig.DEFAULT_BASE_URL.toHttpUrlOrNull()?.host && httpUrl != null) {
            val newUrl =
                original.url.newBuilder()
                    .scheme(httpUrl.scheme)
                    .host(httpUrl.host)
                    .port(httpUrl.port)
                    .build()
            original = original.newBuilder()
                .url(newUrl)
                .build()
        }
        return chain.proceed(original)
    }
}
