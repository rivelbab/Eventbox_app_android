package com.eventbox.app.android.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import java.lang.IllegalArgumentException
import com.eventbox.app.android.common.SingleLiveEvent
import com.eventbox.app.android.search.location.LocationService

class GeoLocationViewModel(private val locationService: LocationService) : ViewModel() {
    private val mutableLocation = SingleLiveEvent<String>()
    val location: LiveData<String> = mutableLocation
    private val mutableErrorMessage = SingleLiveEvent<String>()
    val errorMessage: SingleLiveEvent<String> = mutableErrorMessage
    private val compositeDisposable = CompositeDisposable()

    fun configure() {
        compositeDisposable += locationService.getAdministrativeArea()
            .subscribe({
                mutableLocation.value = it
            }, {
                mutableErrorMessage.value = if (it is IllegalArgumentException) "No area found"
                                            else "Something went wrong"
            })
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
