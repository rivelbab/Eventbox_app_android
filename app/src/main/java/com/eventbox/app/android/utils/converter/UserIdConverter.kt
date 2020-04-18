package com.eventbox.app.android.utils.converter

import androidx.room.TypeConverter
import com.eventbox.app.android.models.user.UserId

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
