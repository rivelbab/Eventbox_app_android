package com.eventbox.app.android.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.event.EventType
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface EventDao {
    @Insert(onConflict = REPLACE)
    fun insertEvents(events: List<Event>)

    @Insert(onConflict = REPLACE)
    fun insertEvent(event: Event)

    @Query("DELETE FROM Event")
    fun deleteAll()

    @Query("SELECT * from Event ORDER BY startsAt DESC")
    fun getAllEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE id = :id")
    fun getEvent(id: String): Flowable<Event>

    @Query("SELECT * FROM Event WHERE id = :id")
    fun getEventObjectById(id: String): Event

    @Query("SELECT * FROM event WHERE id = :eventId")
    fun getEventById(eventId: String): Single<Event>

    @Query("SELECT * from Event WHERE id in (:ids)")
    fun getEventWithIds(ids: List<String>): Flowable<List<Event>>

    @Query("UPDATE Event SET favorite = :favorite WHERE id = :eventId")
    fun setFavorite(eventId: String, favorite: Boolean)

    @Query("SELECT * from Event WHERE favorite = 1")
    fun getFavoriteEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE favorite = 1 AND id in (:ids)")
    fun getFavoriteEventWithinIds(ids: List<String>): Single<List<Event>>

    @Query("SELECT * from Event WHERE eventType = :eventType")
    fun getAllSimilarEvents(eventType: EventType): Flowable<List<Event>>

    @Query("UPDATE Event SET favorite = :favorite AND favoriteEventId = NULL")
    fun clearFavoriteEvents(favorite: Boolean = false)
}
