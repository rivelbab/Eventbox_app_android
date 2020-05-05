package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eventbox.app.android.models.event.SocialLink
import io.reactivex.Flowable

@Dao
interface SocialLinksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSocialLinks(socialLinks: List<SocialLink>)

    @Query("DELETE FROM SocialLink")
    fun deleteAll()

    @Query("SELECT * from SocialLink WHERE event = :eventId")
    fun getAllSocialLinks(eventId: Long): Flowable<List<SocialLink>>
}