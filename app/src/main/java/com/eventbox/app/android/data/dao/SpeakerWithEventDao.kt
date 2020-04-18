package com.eventbox.app.android.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eventbox.app.android.models.speakers.Speaker
import com.eventbox.app.android.models.speakers.SpeakerWithEvent

@Dao
interface SpeakerWithEventDao {
    @Query("""
        SELECT speaker.* FROM speaker
        INNER JOIN speakerwithevent ON
        speaker.id = speakerwithevent.speaker_id
        WHERE speakerwithevent.event_id = :eventID
        """)
    fun getSpeakerWithEventId(eventID: Long): LiveData<List<Speaker>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(join: SpeakerWithEvent)
}
