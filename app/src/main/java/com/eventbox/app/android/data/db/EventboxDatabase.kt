package com.eventbox.app.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eventbox.app.android.data.dao.*
import com.eventbox.app.android.models.attendees.Attendee
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.models.payment.Tax
import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.models.notification.Notification
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.models.settings.Settings
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.utils.converter.*

@Database(entities = [Event::class, User::class, Ticket::class, Attendee::class,
    Order::class, CustomForm::class, Feedback::class,
    Notification::class, Settings::class, Tax::class], version = 1)
@TypeConverters(
    EventIdConverter::class, EventTypeConverter::class,
    TicketIdConverter::class, UserIdConverter::class,
    AttendeeConverter::class, ListAttendeeConverter::class)
abstract class EventboxDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketDao(): TicketDao

    abstract fun attendeeDao(): AttendeeDao

    abstract fun orderDao(): OrderDao

    abstract fun feedbackDao(): FeedbackDao

    abstract fun notificationDao(): NotificationDao

    abstract fun settingsDao(): SettingsDao

    abstract fun taxDao(): TaxDao
}
