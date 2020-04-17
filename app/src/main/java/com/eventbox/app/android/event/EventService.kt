package com.eventbox.app.android.event

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import com.eventbox.app.android.event.EventUtils
import java.util.Date
import com.eventbox.app.android.event.faq.EventFAQ
import com.eventbox.app.android.event.faq.EventFAQApi
import com.eventbox.app.android.event.location.EventLocation
import com.eventbox.app.android.event.location.EventLocationApi
import com.eventbox.app.android.event.topic.EventTopic
import com.eventbox.app.android.event.topic.EventTopicApi
import com.eventbox.app.android.event.topic.EventTopicsDao
import com.eventbox.app.android.event.types.EventType
import com.eventbox.app.android.event.types.EventTypesApi
import com.eventbox.app.android.favorite.FavoriteEvent
import com.eventbox.app.android.favorite.FavoriteEventApi
import com.eventbox.app.android.sessions.track.Track
import com.eventbox.app.android.speakercall.SpeakersCall
import com.eventbox.app.android.speakercall.SpeakersCallDao
import org.jetbrains.anko.collections.forEachWithIndex

class EventService(
    private val eventApi: EventApi,
    private val eventDao: EventDao,
    private val eventTopicApi: EventTopicApi,
    private val eventTopicsDao: EventTopicsDao,
    private val eventTypesApi: EventTypesApi,
    private val eventLocationApi: EventLocationApi,
    private val eventFAQApi: EventFAQApi,
    private val speakersCallDao: SpeakersCallDao,
    private val favoriteEventApi: FavoriteEventApi
) {

    fun getEventLocations(): Single<List<EventLocation>> {
        return eventLocationApi.getEventLocation()
    }

    fun getEventFAQs(id: Long): Single<List<EventFAQ>> {
        return eventFAQApi.getEventFAQ(id)
    }

    private fun getEventTopicList(eventsList: List<Event>): List<EventTopic?> {
        return eventsList
            .filter { it.eventTopic != null }
            .map { it.eventTopic }
            .toList()
    }

    fun getEventTypes(): Single<List<EventType>> {
        return eventTypesApi.getEventTypes()
    }

    fun getSearchEventsPaged(filter: String, sortBy: String, page: Int): Flowable<List<Event>> {
        return eventApi.searchEventsPaged(sortBy, filter, page).flatMapPublisher { eventsList ->
            eventsList.forEach {
                it.speakersCall?.let { sc -> speakersCallDao.insertSpeakerCall(sc) }
            }
            updateFavorites(eventsList)
        }
    }

    fun getFavoriteEvents(): Flowable<List<Event>> {
        return eventDao.getFavoriteEvents()
    }

    fun getEventsByLocationPaged(locationName: String?, page: Int, pageSize: Int = 5): Flowable<List<Event>> {
        val query = "[{\"name\":\"location-name\",\"op\":\"ilike\",\"val\":\"%$locationName%\"}," +
            "{\"name\":\"ends-at\",\"op\":\"ge\",\"val\":\"%${EventUtils.getTimeInISO8601(Date())}%\"}]"
        /*return eventApi.searchEventsPaged("name", query, page, pageSize).flatMapPublisher { apiList ->
            updateFavorites(apiList)
        }*/
        return eventApi.getAllEvents().flatMapPublisher { apiList ->
            updateFavorites(apiList)
        }
    }

    private fun updateFavorites(apiList: List<Event>): Flowable<List<Event>> {

        val ids = apiList.map { it.id }.toList()
        eventTopicsDao.insertEventTopics(getEventTopicList(apiList))
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

    fun getEvent(id: Long): Flowable<Event> {
        return eventDao.getEvent(id)
    }

    fun getEventByIdentifier(identifier: String): Single<Event> {
        return eventApi.getEventFromApi(identifier)
    }

    fun getEventById(eventId: Long): Single<Event> {
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

    fun getSimilarEventsPaged(id: Long, page: Int, pageSize: Int = 5): Flowable<List<Event>> {
        val filter = "[{\"name\":\"ends-at\",\"op\":\"ge\",\"val\":\"%${EventUtils.getTimeInISO8601(
            Date()
        )}%\"}]"
        return eventTopicApi.getEventsUnderTopicIdPaged(id, filter, page, pageSize)
            .flatMapPublisher {
                updateFavorites(it)
            }
    }

    fun getSpeakerCall(id: Long): Single<SpeakersCall> =
        speakersCallDao.getSpeakerCall(id).onErrorResumeNext {
            eventApi.getSpeakerCallForEvent(id).doAfterSuccess {
                speakersCallDao.insertSpeakerCall(it)
            }
        }

    fun fetchTracksUnderEvent(eventId: Long): Single<List<Track>> = eventApi.fetchTracksUnderEvent(eventId)
}
