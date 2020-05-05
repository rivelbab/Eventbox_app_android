package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import com.eventbox.app.android.models.event.EventTopic

@Dao
interface EventTopicsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEventTopics(eventTopic: List<EventTopic?>)

    @Query("SELECT * from EventTopic")
    fun getAllEventTopics(): Flowable<List<EventTopic>>

    @Query("DELETE FROM EventTopic")
    fun deleteAll()
}