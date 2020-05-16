package com.eventbox.app.android.ui.event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import androidx.paging.RxPagedListBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import com.eventbox.app.android.R
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.models.user.UserId
import com.eventbox.app.android.ui.common.SingleLiveEvent
import com.eventbox.app.android.networks.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.data.dataSource.SimilarEventsDataSourceFactory
import com.eventbox.app.android.models.event.FavoriteEvent
import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.service.*
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import timber.log.Timber
import java.io.File

class EventDetailsViewModel(
    private val eventService: EventService,
    private val authHolder: AuthHolder,
    private val authService: AuthService,
    private val feedbackService: FeedbackService,
    private val resource: Resource,
    private val mutableConnectionLiveData: MutableConnectionLiveData,
    private val config: PagedList.Config
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val connection: LiveData<Boolean> = mutableConnectionLiveData

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress

    private val mutableUser = MutableLiveData<User>()
    val user: LiveData<User> = mutableUser

    private val mutablePopMessage = SingleLiveEvent<String>()
    val popMessage: SingleLiveEvent<String> = mutablePopMessage

    private val mutableEvent = MutableLiveData<Event>()
    val event: LiveData<Event> = mutableEvent
    private val mutableCreatedEvent = MutableLiveData<Event>()
    val createdEvent: LiveData<Event> = mutableCreatedEvent

    private val mutableEventFeedback = MutableLiveData<List<Feedback>>()
    val eventFeedback: LiveData<List<Feedback>> = mutableEventFeedback
    private val mutableFeedbackProgress = MutableLiveData<Boolean>()
    val feedbackProgress: LiveData<Boolean> = mutableFeedbackProgress
    private val mutableSubmittedFeedback = MutableLiveData<Feedback>()
    val submittedFeedback: LiveData<Feedback> = mutableSubmittedFeedback

    private val mutableSimilarEvents = MutableLiveData<PagedList<Event>>()
    val similarEvents: LiveData<PagedList<Event>> = mutableSimilarEvents
    private val mutableSimilarEventsProgress = MediatorLiveData<Boolean>()
    val similarEventsProgress: MediatorLiveData<Boolean> = mutableSimilarEventsProgress

    private var eventImageTemp = MutableLiveData<File>()
    var avatarUpdated = false
    var encodedImage: String? = null
    var eventAvatar: String? = null

    fun isLoggedIn() = authHolder.isLoggedIn()

    fun getId() = authHolder.getId()

    fun getLoggedInUserName(): String {
        var username: String = resource.getString(R.string.app_name).toString()
        compositeDisposable += authService.getProfile()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({ user ->
                username = user.name.toString()
            }) {
                Timber.e(it, "Failure")
                mutablePopMessage.value = resource.getString(R.string.failure)
            }
        return username
    }

    fun fetchEventFeedback(id: String) {
        if (id == "") return

        compositeDisposable += feedbackService.getEventFeedback(id)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableFeedbackProgress.value = true
            }.doFinally {
                mutableFeedbackProgress.value = false
            }
            .subscribe({
                mutableEventFeedback.value = it
            }, {
                Timber.e(it, "Error fetching events feedback")
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.feedback))
            })
    }

    fun createEvent(
        name: String, description: String, location: String,
        startAt: String, endAt: String, privacy: String
    ) {

        val event = Event(
            name = name, description = description,
            locationName = location, startsAt = startAt,
            endsAt = endAt, privacy = privacy, ownerName = getLoggedInUserName(),
            originalImageUrl = eventAvatar.toString()
        )

        compositeDisposable += eventService.createEvent(event)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutablePopMessage.value = resource.getString(R.string.create_event_success_message)
                mutableCreatedEvent.value = it
            }, {
                mutableProgress.value = false
                mutablePopMessage.value = resource.getString(R.string.create_event_fail_message)
            })
    }

    fun submitFeedback(comment: String, rating: Float?, eventId: String) {
        val feedback = Feedback(
            rating = rating.toString(), comment = comment,
            event = EventId(eventId), user = UserId(
                getId()
            )
        )
        compositeDisposable += feedbackService.submitFeedback(feedback)
            .withDefaultSchedulers()
            .subscribe({
                mutablePopMessage.value = resource.getString(R.string.feedback_submitted)
                mutableSubmittedFeedback.value = it
            }, {
                mutablePopMessage.value = resource.getString(R.string.error_submitting_feedback)
            })
    }



    fun fetchSimilarEvents(eventId: String, typeId: String, location: String?) {
        if (eventId == "") return

        val sourceFactory =
            SimilarEventsDataSourceFactory(
                compositeDisposable,
                typeId,
                eventId,
                mutableSimilarEventsProgress,
                eventService
            )

        val similarEventPagedList = RxPagedListBuilder(sourceFactory, config)
            .setFetchScheduler(Schedulers.io())
            .buildObservable()
            .cache()

        compositeDisposable += similarEventPagedList
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableSimilarEventsProgress.value = true
            }.subscribe({ events ->
                val currentPagedSimilarEvents = mutableSimilarEvents.value
                if (currentPagedSimilarEvents == null) {
                    mutableSimilarEvents.value = events
                } else {
                    currentPagedSimilarEvents.addAll(events)
                    mutableSimilarEvents.value = currentPagedSimilarEvents
                }
            }, {
                Timber.e(it, "Error fetching similar events")
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_section_message,
                    resource.getString(R.string.similar_events))
            })
    }

    fun loadEvent(id: String) {
        if (id == "") {
            mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += eventService.getEvent(id)
            .withDefaultSchedulers()
            .distinctUntilChanged()
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableProgress.value = false
                mutableEvent.value = it
            }, {
                mutableProgress.value = false
                Timber.e(it, "Error fetching event %s", id)
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun loadEventByIdentifier(identifier: String) {
        if (identifier.isEmpty()) {
            mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            return
        }
        compositeDisposable += eventService.getEventByIdentifier(identifier)
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableProgress.value = true
            }.doFinally {
                mutableProgress.value = false
            }.subscribe({
                mutableEvent.value = it
            }, {
                Timber.e(it, "Error fetching event")
                mutablePopMessage.value = resource.getString(R.string.error_fetching_event_message)
            })
    }

    fun setFavorite(event: Event, favorite: Boolean) {
        if (favorite) {
            addFavorite(event)
        } else {
            removeFavorite(event)
        }
    }

    private fun addFavorite(event: Event) {
        val favoriteEvent = FavoriteEvent(
            authHolder.getId(),
            EventId(event.id)
        )
        compositeDisposable += eventService.addFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutablePopMessage.value = resource.getString(R.string.add_event_to_shortlist_message)
            }, {
                mutablePopMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on adding like for event ID ${event.id}")
            })
    }

    private fun removeFavorite(event: Event) {
        val favoriteEventId = event.favoriteEventId ?: return

        val favoriteEvent = FavoriteEvent(
            favoriteEventId,
            EventId(event.id)
        )
        compositeDisposable += eventService.removeFavorite(favoriteEvent, event)
            .withDefaultSchedulers()
            .subscribe({
                mutablePopMessage.value = resource.getString(R.string.remove_event_from_shortlist_message)
            }, {
                mutablePopMessage.value = resource.getString(R.string.out_bad_try_again)
                Timber.d(it, "Fail on removing like for event ID ${event.id}")
            })
    }

    fun setEventTempFile(file: File) {
        eventImageTemp.value = file
    }

    fun getEventTempFile(): MutableLiveData<File> {
        return eventImageTemp
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
