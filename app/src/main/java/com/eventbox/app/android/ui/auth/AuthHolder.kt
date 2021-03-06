package com.eventbox.app.android.ui.auth

import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.utils.JWTUtils

private const val TOKEN_KEY = "TOKEN"

class AuthHolder(private val preference: Preference) {

    var token: String? = null
        get() {
            return preference.getString(TOKEN_KEY)
        }
        set(value) {
            if (value != null && JWTUtils.isExpired(value))
                throw IllegalStateException("Cannot set expired token")
            field = value
            preference.putString(TOKEN_KEY, value)
        }

    fun getAuthorization(): String? {
        if (!isLoggedIn())
            return null
        return "JWT $token"
    }

    fun isLoggedIn(): Boolean {
        if (token == null || JWTUtils.isExpired(token)) {
            token = null
            return false
        }

        return true
    }

    fun getId(): String {
        return if (!isLoggedIn()) "" else JWTUtils.getIdentity(token)
    }
}
