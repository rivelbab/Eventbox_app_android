package com.eventbox.app.android.ui.event.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.lang.StringBuilder
import java.util.concurrent.TimeUnit
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.R
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.models.event.EventLocation
import com.eventbox.app.android.utils.extensions.withDefaultSchedulers
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import timber.log.Timber

const val SAVED_LOCATION = "LOCATION"
const val SAVED_LOCATION_LIST = "LOCATION_LIST"
const val SAVED_LOCATION_LIST_SIZE = 7
const val SEARCH_INTERVAL = 250L

class SearchLocationViewModel(
    private val eventService: EventService,
    private val preference: Preference,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private val mutableEventLocations = MutableLiveData<List<EventLocation>>()
    val eventLocations: LiveData<List<EventLocation>> = mutableEventLocations
    private val mutableShowShimmer = MutableLiveData<Boolean>()
    val showShimmer: LiveData<Boolean> = mutableShowShimmer
    private var geoCodingRequest: MapboxGeocoding? = null
    private val savedLocationList = mutableListOf<String>()

    val placeSuggestions = MutableLiveData<List<CarmenFeature>>()

    fun saveSearch(query: String) {
        preference.putString(SAVED_LOCATION, query)

        if (query == resource.getString(R.string.no_location)) return

        if (savedLocationList.size == SAVED_LOCATION_LIST_SIZE)
            savedLocationList.removeAt(SAVED_LOCATION_LIST_SIZE - 1)
        val index = savedLocationList.indexOf(query)
        if (index != -1) savedLocationList.removeAt(index)
            savedLocationList.add(0, query)

        val stringBuilder = StringBuilder()
        for (location in savedLocationList) {
            stringBuilder.append(location)
            if (location != savedLocationList.last()) stringBuilder.append(",")
        }
        preference.putString(SAVED_LOCATION_LIST, stringBuilder.toString())
    }

    fun getRecentLocationList(): List<String> {
        val locationsString = preference.getString(SAVED_LOCATION_LIST, "")
        if (!locationsString.isNullOrBlank()) {
            val locations = locationsString.split(",")
            savedLocationList.clear()
            savedLocationList.addAll(locations)
        }
        return savedLocationList
    }

    private fun loadPlaceSuggestions(query: String) {
        // Cancel Previous Call
        geoCodingRequest?.cancelCall()
        doAsync {
            geoCodingRequest = makeGeocodingRequest(query)
            val list = geoCodingRequest?.executeCall()?.body()?.features()
            uiThread { placeSuggestions.value = list }
        }
    }

    private fun makeGeocodingRequest(query: String) = MapboxGeocoding.builder()
        .accessToken(BuildConfig.MAPBOX_KEY)
        .query(query)
        .languages("en")
        .build()

    fun handlePlaceSuggestions(observableQuery: Observable<String>) {
        compositeDisposable += (
            observableQuery.debounce(SEARCH_INTERVAL, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    loadPlaceSuggestions(it)
                }
        )
    }

    fun loadEventsLocation() {
        compositeDisposable += eventService.getEventLocations()
            .withDefaultSchedulers()
            .doOnSubscribe {
                mutableShowShimmer.value = true
            }
            .doFinally {
                mutableShowShimmer.value = false
            }
            .subscribe({
                mutableEventLocations.value = it
            }, {
                Timber.e(it, "Error fetching events")
            })
    }

    override fun onCleared() {
        super.onCleared()
        geoCodingRequest?.cancelCall()
        compositeDisposable.dispose()
    }
}
