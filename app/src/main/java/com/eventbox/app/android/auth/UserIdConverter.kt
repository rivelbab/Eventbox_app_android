package com.eventbox.app.android.auth

import androidx.room.TypeConverter

class UserIdConverter {
    @TypeConverter
    fun fromUserId(userId: UserId?): Long? {
        return userId?.id
    }

    @TypeConverter
    fun toUserId(id: Long?): UserId? {
        return id?.let {
            UserId(it)
        }
    }
}
