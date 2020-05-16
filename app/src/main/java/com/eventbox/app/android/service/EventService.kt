package com.eventbox.app.android.service

import com.eventbox.app.android.data.dao.EventDao
import com.eventbox.app.android.models.event.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import com.eventbox.app.android.utils.EventUtils
import java.util.Date
import com.eventbox.app.android.networks.api.*
import org.jetbrains.anko.collections.forEachWithIndex

class EventService(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val eventLocationApi: EventLocationApi,
    private val eventTypesApi: EventTypesApi,
    private val eventFAQApi: EventFAQApi,
    private val favoriteEventApi: FavoriteEventApi
) {

    fun createEvent(event: Event): Single<Event> {
        return  eventApi.createEvent(event).map {
            eventDao.insertEvent(it)
            it
        }
    }

    fun getEventLocations(): Single<List<EventLocation>> {
        return eventLocationApi.getEventLocation()
    }

    fun getEventFAQs(id: String): Single<List<EventFAQ>> {
        return eventFAQApi.getEventFAQ(id)
    }

    fun getEventTypes(): Single<List<EventType>> {
        return eventTypesApi.getEventTypes()
    }

    fun getAllEvents(filter: String, sortBy: String, page: Int): Flowable<List<Event>> {
        return eventApi.getAllEvents().flatMapPublisher { eventsList ->
            updateFavorites(eventsList)
        }
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocationPaged(page: Int, pageSize: Int = 5): Flowable<List<Event>> {
        val query = "[]"
        return eventApi.searchEventsPaged("name", query, page, pageSize).flatMapPublisher { apiList ->
            updateFavorites(apiList)
        }
    }

    private fun updateFavorites(apiList: List<Event>): Flowable<List<Event>> {

        val ids = apiList.map { it.id }.toList()

        return eventDao.getFavoriteEventWithinIds(ids)
            .flatMapPublisher { favEvents ->
                val favEventIdsList = favEvents.map { it.id }
                val favEventFavIdsList = favEvents.map { it.favoriteEventId }
                apiList.map {
                    val index = favEventIdsList.indexOf(it.id)
                    if (index != -1) {
                        it.favorite = true
                        it.favoriteEventId = favEventFavIdsList[index]
                    }
                }
                eventDao.insertEvents(apiList)
                val eventIds = apiList.map { it.id }.toList()
                eventDao.getEventWithIds(eventIds)
            }
    }

    fun getEvent(id: String): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun getEventByIdentifier(identifier: String): Single<Event> {
        return eventApi.getEventFromApi(identifier)
    }

    fun getEventById(eventId: String): Single<Event> {
        return eventDao.getEventById(eventId)
            .onErrorResumeNext {
                eventApi.getEventFromApi(eventId.toString()).map {
                    eventDao.insertEvent(it)
                    it
                }
            }
    }

    fun getEventsWithQuery(query: String): Single<List<Event>> {
        return eventApi.eventsByQuery(query).map {
            eventDao.insertEvents(it)
            it
        }
    }

    fun loadFavoriteEvent(): Single<List<FavoriteEvent>> = favoriteEventApi.getFavorites()

    fun saveFavoritesEventFromApi(favIdsList: List<FavoriteEvent>): Single<List<Event>> {
        val idsList = favIdsList.filter { it.event != null }.map { it.event!!.id }
        val query = """[{
                |   'and':[{
                |       'name':'id',
                |       'op':'in',
                |       'val': $idsList
                |    }]
                |}]""".trimMargin().replace("'", "\"")
        return eventApi.eventsWithQuery(query).map {
            it.forEachWithIndex { index, event ->
                event.favoriteEventId = favIdsList[index].id
                event.favorite = true
                eventDao.insertEvent(event)
            }
            it
        }
    }

    fun addFavorite(favoriteEvent: FavoriteEvent, event: Event) =
        favoriteEventApi.addFavorite(favoriteEvent).map {
            event.favoriteEventId = it.id
            event.favorite = true
            eventDao.insertEvent(event)
            it
        }

    fun removeFavorite(favoriteEvent: FavoriteEvent, event: Event): Completable =
        favoriteEventApi.removeFavorite(event.id).andThen {
            event.favorite = false
            event.favoriteEventId = null
            eventDao.insertEvent(event)
        }

    fun getSimilarEventsPaged(id: String, page: Int, pageSize: Int = 5): Flowable<List<Event>> {
        val filter = "[{\"name\":\"ends-at\",\"op\":\"ge\",\"val\":\"%${EventUtils.getTimeInISO8601(
            Date()
        )}%\"}]"
        return eventApi.getEventsUnderTypeIdPaged(id, filter, page, pageSize)
            .flatMapPublisher {
                updateFavorites(it)
            }
    }
}
