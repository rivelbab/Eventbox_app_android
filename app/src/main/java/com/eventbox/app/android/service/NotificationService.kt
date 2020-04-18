package com.eventbox.app.android.service

import com.eventbox.app.android.data.dao.NotificationDao
import com.eventbox.app.android.models.notification.Notification
import com.eventbox.app.android.networks.api.NotificationApi
import io.reactivex.Single

class NotificationService(
    private val notificationApi: NotificationApi,
    private val notificationDao: NotificationDao
) {

    fun getNotifications(userId: Long): Single<List<Notification>> {
        return notificationDao.getNotifications()
            .onErrorResumeNext {
                notificationApi.getNotifications(userId).map {
                    notificationDao.insertNotifications(it)
                    it
                }
            }
    }

    fun syncNotifications(userId: Long): Single<List<Notification>> {
        return notificationApi.getNotifications(userId).map {
            notificationDao.insertNotifications(it)
            it
        }
    }

    fun updateNotification(notification: Notification): Single<Notification> {
        return notificationApi.updateNotification(notification.id, notification).map {
            notificationDao.insertNotification(it)
            it
        }
    }
}
