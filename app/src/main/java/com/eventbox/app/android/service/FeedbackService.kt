package com.eventbox.app.android.service

import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.networks.api.FeedbackApi
import com.eventbox.app.android.data.dao.FeedbackDao
import io.reactivex.Single

class FeedbackService(
    private val feedbackDao: FeedbackDao,
    private val feedbackApi: FeedbackApi
) {
    fun getFeedbackUnderEventFromDb(eventId: String): Single<List<Feedback>> =
        feedbackDao.getAllFeedbackUnderEvent(eventId)

    fun getEventFeedback(id: String): Single<List<Feedback>> =
        feedbackApi.getEventFeedback(id).doOnSuccess {
            feedbackDao.insertFeedback(it)
        }

    fun submitFeedback(feedback: Feedback): Single<Feedback> =
        feedbackApi.postfeedback(feedback).doOnSuccess {
            feedbackDao.insertSingleFeedback(it)
        }
}
