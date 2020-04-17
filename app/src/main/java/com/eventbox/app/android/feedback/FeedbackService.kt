package com.eventbox.app.android.feedback

import com.eventbox.app.android.feedback.Feedback
import com.eventbox.app.android.feedback.FeedbackApi
import com.eventbox.app.android.feedback.FeedbackDao
import io.reactivex.Single

class FeedbackService(
    private val feedbackDao: FeedbackDao,
    private val feedbackApi: FeedbackApi
) {
    fun getFeedbackUnderEventFromDb(eventId: Long): Single<List<Feedback>> =
        feedbackDao.getAllFeedbackUnderEvent(eventId)

    fun getEventFeedback(id: Long): Single<List<Feedback>> =
        feedbackApi.getEventFeedback(id).doOnSuccess {
            feedbackDao.insertFeedback(it)
        }

    fun submitFeedback(feedback: Feedback): Single<Feedback> =
        feedbackApi.postfeedback(feedback).doOnSuccess {
            feedbackDao.insertSingleFeedback(it)
        }
}
