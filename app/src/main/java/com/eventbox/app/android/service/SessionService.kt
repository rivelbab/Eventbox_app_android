package com.eventbox.app.android.service

import io.reactivex.Single
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.models.session.Session
import com.eventbox.app.android.networks.api.SessionApi
import com.eventbox.app.android.data.dao.SessionDao
import com.eventbox.app.android.models.speakers.Proposal

class SessionService(
    private val sessionApi: SessionApi,
    private val sessionDao: SessionDao
) {
    fun fetchSessionForEvent(id: Long): Single<List<Session>> {
        return sessionApi.getSessionsForEvent(id)
            .doOnSuccess { sessions ->
                sessionDao.deleteCurrentSessions()
                sessionDao.insertSessions(sessions)
            }
        }

    fun fetchSession(id: Long): Single<Session> =
        sessionApi.getSessionById(id)

    fun createSession(proposal: Proposal): Single<Session> =
        sessionApi.createSession(proposal).doOnSuccess {
            sessionDao.insertSession(it)
        }

    fun updateSession(sessionId: Long, proposal: Proposal): Single<Session> =
        sessionApi.updateSession(sessionId, proposal).doOnSuccess {
            sessionDao.insertSession(it)
        }

    fun getSessionsUnderSpeakerAndEvent(speakerId: Long, query: String): Single<List<Session>> =
        sessionApi.getSessionsUnderSpeaker(speakerId, filter = query)

    fun getCustomFormsForSessions(id: Long): Single<List<CustomForm>> {
        val filter = """[{
                |   'and':[{
                |       'name':'form',
                |       'op':'eq',
                |       'val':'session'
                |    },{
                |       'name':'is-included',
                |       'op':'eq',
                |       'val':true
                |    }]
                |}]""".trimMargin().replace("'", "\"")
        return sessionApi.getCustomForms(id, filter)
    }
}
