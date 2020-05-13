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
    fun getEvent(id: Long): Flowable<Event>

    @Query("SELECT * FROM Event WHERE id = :id")
    fun getEventObjectById(id: Long): Event

    @Query("SELECT * FROM event WHERE id = :eventId")
    fun getEventById(eventId: Long): Single<Event>

    @Query("SELECT * from Event WHERE id in (:ids)")
    fun getEventWithIds(ids: List<Long>): Flowable<List<Event>>

    @Query("UPDATE Event SET favorite = :favorite WHERE id = :eventId")
    fun setFavorite(eventId: Long, favorite: Boolean)

    @Query("SELECT * from Event WHERE favorite = 1")
    fun getFavoriteEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE favorite = 1 AND id in (:ids)")
    fun getFavoriteEventWithinIds(ids: List<Long>): Single<List<Event>>

    @Query("UPDATE Event SET interested = :interested WHERE id = :eventId")
    fun setInterested(eventId: Long, interested: Boolean)

    @Query("SELECT * from Event WHERE interested = 1")
    fun getInterestedEvents(): Flowable<List<Event>>

    @Query("SELECT * from Event WHERE interested = 1 AND id in (:ids)")
    fun getInterestedEventWithinIds(ids: List<Long>): Single<List<Event>>

    @Query("SELECT * from Event WHERE eventType = :eventType")
    fun getAllSimilarEvents(eventType: EventType): Flowable<List<Event>>

    @Query("UPDATE Event SET favorite = :favorite AND favoriteEventId = NULL")
    fun clearFavoriteEvents(favorite: Boolean = false)
}
