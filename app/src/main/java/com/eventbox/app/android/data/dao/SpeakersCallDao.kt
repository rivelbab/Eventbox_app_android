package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.eventbox.app.android.models.speakers.SpeakersCall
import io.reactivex.Single

@Dao
interface SpeakersCallDao {

    @Insert(onConflict = REPLACE)
    fun insertSpeakerCall(speakers: SpeakersCall)

    @Query("SELECT * from SpeakersCall WHERE id = :id")
    fun getSpeakerCall(id: Long): Single<SpeakersCall>
}
