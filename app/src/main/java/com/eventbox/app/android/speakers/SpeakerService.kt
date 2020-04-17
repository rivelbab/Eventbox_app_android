package com.eventbox.app.android.speakers

import io.reactivex.Flowable
import io.reactivex.Single
import com.eventbox.app.android.attendees.forms.CustomForm
import com.eventbox.app.android.auth.User

class SpeakerService(

    private val speakerApi: SpeakerApi,
    private val speakerDao: SpeakerDao,
    private val speakerWithEventDao: SpeakerWithEventDao

) {
    fun fetchSpeakersForEvent(id: Long, query: String): Single<List<Speaker>> {
        return speakerApi.getSpeakerForEvent(id, query).doOnSuccess { speakerList ->
            speakerList.forEach {
                speakerDao.insertSpeaker(it)
                speakerWithEventDao.insert(SpeakerWithEvent(id, it.id))
            }
        }
    }

    fun getSpeakerProfileOfEmailAndEvent(user: User, eventId: Long, query: String): Single<Speaker> =
        speakerDao.getSpeakerByEmailAndEvent(user.email ?: "", eventId)
            .onErrorResumeNext {
                speakerApi.getSpeakerForUser(user.id, query).map {
                    speakerDao.insertSpeakers(it)
                    it.first()
                }
            }

    fun addSpeaker(speaker: Speaker): Single<Speaker> =
        speakerApi.addSpeaker(speaker).doOnSuccess {
            speakerDao.insertSpeaker(it)
        }

    fun editSpeaker(speaker: Speaker): Single<Speaker> =
        speakerApi.updateSpeaker(speaker.id, speaker).doOnSuccess {
            speakerDao.insertSpeaker(it)
        }

    fun fetchSpeakerForSession(sessionId: Long): Single<List<Speaker>> =
        speakerApi.getSpeakersForSession(sessionId)
            .doOnSuccess {
                speakerDao.insertSpeakers(it)
            }

    fun fetchSpeaker(id: Long): Flowable<Speaker> {
        return speakerDao.getSpeaker(id)
    }

    fun getCustomFormsForSpeakers(id: Long): Single<List<CustomForm>> {
        val filter = """[{
                |   'and':[{
                |       'name':'form',
                |       'op':'eq',
                |       'val':'speaker'
                |    },{
                |       'name':'is-included',
                |       'op':'eq',
                |       'val':true
                |    }]
                |}]""".trimMargin().replace("'", "\"")
        return speakerApi.getCustomForms(id, filter)
    }
}
