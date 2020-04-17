package com.eventbox.app.android

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eventbox.app.android.attendees.Attendee
import com.eventbox.app.android.attendees.AttendeeConverter
import com.eventbox.app.android.attendees.AttendeeDao
import com.eventbox.app.android.attendees.ListAttendeeConverter
import com.eventbox.app.android.attendees.forms.CustomForm
import com.eventbox.app.android.auth.User
import com.eventbox.app.android.auth.UserDao
import com.eventbox.app.android.auth.UserIdConverter
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventDao
import com.eventbox.app.android.event.EventIdConverter
import com.eventbox.app.android.event.subtopic.EventSubTopicConverter
import com.eventbox.app.android.event.tax.Tax
import com.eventbox.app.android.event.tax.TaxDao
import com.eventbox.app.android.event.topic.EventTopic
import com.eventbox.app.android.event.topic.EventTopicConverter
import com.eventbox.app.android.event.topic.EventTopicsDao
import com.eventbox.app.android.event.types.EventTypeConverter
import com.eventbox.app.android.feedback.Feedback
import com.eventbox.app.android.feedback.FeedbackDao
import com.eventbox.app.android.notification.Notification
import com.eventbox.app.android.notification.NotificationDao
import com.eventbox.app.android.order.Order
import com.eventbox.app.android.order.OrderDao
import com.eventbox.app.android.sessions.Session
import com.eventbox.app.android.sessions.SessionDao
import com.eventbox.app.android.sessions.microlocation.MicroLocationConverter
import com.eventbox.app.android.sessions.sessiontype.SessionTypeConverter
import com.eventbox.app.android.sessions.track.TrackConverter
import com.eventbox.app.android.settings.Settings
import com.eventbox.app.android.settings.SettingsDao
import com.eventbox.app.android.social.SocialLink
import com.eventbox.app.android.social.SocialLinksDao
import com.eventbox.app.android.speakercall.Proposal
import com.eventbox.app.android.speakercall.SpeakersCall
import com.eventbox.app.android.speakercall.SpeakersCallConverter
import com.eventbox.app.android.speakercall.SpeakersCallDao
import com.eventbox.app.android.speakers.ListSpeakerIdConverter
import com.eventbox.app.android.speakers.Speaker
import com.eventbox.app.android.speakers.SpeakerDao
import com.eventbox.app.android.speakers.SpeakerWithEvent
import com.eventbox.app.android.speakers.SpeakerWithEventDao
import com.eventbox.app.android.sponsor.Sponsor
import com.eventbox.app.android.sponsor.SponsorDao
import com.eventbox.app.android.sponsor.SponsorWithEvent
import com.eventbox.app.android.sponsor.SponsorWithEventDao
import com.eventbox.app.android.ticket.Ticket
import com.eventbox.app.android.ticket.TicketDao
import com.eventbox.app.android.ticket.TicketIdConverter

@Database(entities = [Event::class, User::class, SocialLink::class, Ticket::class, Attendee::class,
    EventTopic::class, Order::class, CustomForm::class, Speaker::class, SpeakerWithEvent::class, Sponsor::class,
    SponsorWithEvent::class, Session::class, SpeakersCall::class, Feedback::class, Notification::class,
    Settings::class, Proposal::class, Tax::class], version = 9)
@TypeConverters(EventIdConverter::class, EventTopicConverter::class, EventTypeConverter::class,
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
