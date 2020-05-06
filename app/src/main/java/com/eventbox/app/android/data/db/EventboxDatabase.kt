package com.eventbox.app.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eventbox.app.android.models.attendees.Attendee
import com.eventbox.app.android.utils.converter.AttendeeConverter
import com.eventbox.app.android.data.dao.AttendeeDao
import com.eventbox.app.android.utils.converter.ListAttendeeConverter
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.data.dao.UserDao
import com.eventbox.app.android.utils.converter.UserIdConverter
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.data.dao.EventDao
import com.eventbox.app.android.utils.converter.EventIdConverter
import com.eventbox.app.android.models.payment.Tax
import com.eventbox.app.android.data.dao.TaxDao
import com.eventbox.app.android.models.event.EventTopic
import com.eventbox.app.android.utils.converter.EventTopicConverter
import com.eventbox.app.android.data.dao.EventTopicsDao
import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.data.dao.FeedbackDao
import com.eventbox.app.android.models.notification.Notification
import com.eventbox.app.android.data.dao.NotificationDao
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.data.dao.OrderDao
import com.eventbox.app.android.models.settings.Settings
import com.eventbox.app.android.data.dao.SettingsDao
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.data.dao.TicketDao
import com.eventbox.app.android.utils.converter.TicketIdConverter

@Database(entities = [Event::class, User::class, Ticket::class, Attendee::class,
    EventTopic::class, Order::class, CustomForm::class, Feedback::class,
    Notification::class, Settings::class, Tax::class], version = 1)
@TypeConverters(
    EventIdConverter::class, EventTopicConverter::class,
    TicketIdConverter::class, UserIdConverter::class,
    AttendeeConverter::class, ListAttendeeConverter::class)
abstract class EventboxDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketDao(): TicketDao

    abstract fun attendeeDao(): AttendeeDao

    abstract fun eventTopicsDao(): EventTopicsDao

    abstract fun orderDao(): OrderDao

    abstract fun feedbackDao(): FeedbackDao

    abstract fun notificationDao(): NotificationDao

    abstract fun settingsDao(): SettingsDao

    abstract fun taxDao(): TaxDao
}
