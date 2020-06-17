package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.user.UserId

class UserIdConverter {
    @TypeConverter
    fun fromUserId(userId: UserId?): String? {
        return userId?.id
    }

    @TypeConverter
    fun toUserId(id: String?): UserId? {
        return id?.let {
            UserId(it)
        }
    }
}
