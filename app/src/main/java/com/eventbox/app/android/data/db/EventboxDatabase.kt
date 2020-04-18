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
import com.eventbox.app.android.utils.converter.EventSubTopicConverter
import com.eventbox.app.android.models.payment.Tax
import com.eventbox.app.android.data.dao.TaxDao
import com.eventbox.app.android.models.event.EventTopic
import com.eventbox.app.android.utils.converter.EventTopicConverter
import com.eventbox.app.android.data.dao.EventTopicsDao
import com.eventbox.app.android.utils.converter.EventTypeConverter
import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.data.dao.FeedbackDao
import com.eventbox.app.android.models.notification.Notification
import com.eventbox.app.android.data.dao.NotificationDao
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.data.dao.OrderDao
import com.eventbox.app.android.models.session.Session
import com.eventbox.app.android.data.dao.SessionDao
import com.eventbox.app.android.utils.converter.MicroLocationConverter
import com.eventbox.app.android.utils.converter.SessionTypeConverter
import com.eventbox.app.android.utils.converter.TrackConverter
import com.eventbox.app.android.models.settings.Settings
import com.eventbox.app.android.data.dao.SettingsDao
import com.eventbox.app.android.models.event.SocialLink
import com.eventbox.app.android.data.dao.SocialLinksDao
import com.eventbox.app.android.models.speakers.Proposal
import com.eventbox.app.android.models.speakers.SpeakersCall
import com.eventbox.app.android.utils.converter.SpeakersCallConverter
import com.eventbox.app.android.data.dao.SpeakersCallDao
import com.eventbox.app.android.utils.converter.ListSpeakerIdConverter
import com.eventbox.app.android.models.speakers.Speaker
import com.eventbox.app.android.data.dao.SpeakerDao
import com.eventbox.app.android.models.speakers.SpeakerWithEvent
import com.eventbox.app.android.data.dao.SpeakerWithEventDao
import com.eventbox.app.android.models.sponsor.Sponsor
import com.eventbox.app.android.data.dao.SponsorDao
import com.eventbox.app.android.models.sponsor.SponsorWithEvent
import com.eventbox.app.android.data.dao.SponsorWithEventDao
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.data.dao.TicketDao
import com.eventbox.app.android.utils.converter.TicketIdConverter

@Database(entities = [Event::class, User::class, SocialLink::class, Ticket::class, Attendee::class,
    EventTopic::class, Order::class, CustomForm::class, Speaker::class, SpeakerWithEvent::class, Sponsor::class,
    SponsorWithEvent::class, Session::class, SpeakersCall::class, Feedback::class, Notification::class,
    Settings::class, Proposal::class, Tax::class], version = 9)
@TypeConverters(
    EventIdConverter::class, EventTopicConverter::class, EventTypeConverter::class,
    EventSubTopicConverter::class, TicketIdConverter::class, MicroLocationConverter::class, UserIdConverter::class,
    AttendeeConverter::class, ListAttendeeConverter::class, SessionTypeConverter::class, TrackConverter::class,
    SpeakersCallConverter::class, ListSpeakerIdConverter::class)
abstract class EventboxDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    abstract fun userDao(): UserDao

    abstract fun ticketDao(): TicketDao

    abstract fun socialLinksDao(): SocialLinksDao

    abstract fun attendeeDao(): AttendeeDao

    abstract fun speakerDao(): SpeakerDao

    abstract fun speakerWithEventDao(): SpeakerWithEventDao

    abstract fun eventTopicsDao(): EventTopicsDao

    abstract fun orderDao(): OrderDao

    abstract fun sponsorDao(): SponsorDao

    abstract fun sponsorWithEventDao(): SponsorWithEventDao

    abstract fun sessionDao(): SessionDao

    abstract fun speakersCallDao(): SpeakersCallDao

    abstract fun feedbackDao(): FeedbackDao

    abstract fun notificationDao(): NotificationDao

    abstract fun settingsDao(): SettingsDao

    abstract fun taxDao(): TaxDao
}
