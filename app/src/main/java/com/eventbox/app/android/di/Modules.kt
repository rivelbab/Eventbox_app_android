package com.eventbox.app.android.di

import androidx.paging.PagedList
import androidx.room.Room
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.jasminb.jsonapi.ResourceConverter
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.eventbox.app.android.BuildConfig
import com.eventbox.app.android.EventboxDatabase
import com.eventbox.app.android.StartupViewModel
import com.eventbox.app.android.about.AboutEventViewModel
import com.eventbox.app.android.attendees.Attendee
import com.eventbox.app.android.attendees.AttendeeApi
import com.eventbox.app.android.attendees.AttendeeService
import com.eventbox.app.android.attendees.AttendeeViewModel
import com.eventbox.app.android.attendees.forms.CustomForm
import com.eventbox.app.android.auth.AuthApi
import com.eventbox.app.android.auth.AuthHolder
import com.eventbox.app.android.auth.AuthService
import com.eventbox.app.android.auth.AuthViewModel
import com.eventbox.app.android.auth.EditProfileViewModel
import com.eventbox.app.android.auth.LoginViewModel
import com.eventbox.app.android.auth.ProfileViewModel
import com.eventbox.app.android.auth.RequestAuthenticator
import com.eventbox.app.android.auth.SignUp
import com.eventbox.app.android.auth.SignUpViewModel
import com.eventbox.app.android.auth.SmartAuthViewModel
import com.eventbox.app.android.auth.User
import com.eventbox.app.android.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.data.Network
import com.eventbox.app.android.data.Preference
import com.eventbox.app.android.data.Resource
import com.eventbox.app.android.discount.DiscountApi
import com.eventbox.app.android.discount.DiscountCode
import com.eventbox.app.android.event.Event
import com.eventbox.app.android.event.EventApi
import com.eventbox.app.android.event.EventDetailsViewModel
import com.eventbox.app.android.event.EventId
import com.eventbox.app.android.event.EventService
import com.eventbox.app.android.event.EventsViewModel
import com.eventbox.app.android.event.faq.EventFAQ
import com.eventbox.app.android.event.faq.EventFAQApi
import com.eventbox.app.android.event.faq.EventFAQViewModel
import com.eventbox.app.android.event.location.EventLocation
import com.eventbox.app.android.event.location.EventLocationApi
import com.eventbox.app.android.event.subtopic.EventSubTopic
import com.eventbox.app.android.event.tax.Tax
import com.eventbox.app.android.event.tax.TaxApi
import com.eventbox.app.android.event.tax.TaxService
import com.eventbox.app.android.event.topic.EventTopic
import com.eventbox.app.android.event.topic.EventTopicApi
import com.eventbox.app.android.event.types.EventType
import com.eventbox.app.android.event.types.EventTypesApi
import com.eventbox.app.android.favorite.FavoriteEvent
import com.eventbox.app.android.favorite.FavoriteEventApi
import com.eventbox.app.android.favorite.FavoriteEventsViewModel
import com.eventbox.app.android.feedback.Feedback
import com.eventbox.app.android.feedback.FeedbackApi
import com.eventbox.app.android.feedback.FeedbackService
import com.eventbox.app.android.feedback.FeedbackViewModel
import com.eventbox.app.android.notification.Notification
import com.eventbox.app.android.notification.NotificationApi
import com.eventbox.app.android.notification.NotificationService
import com.eventbox.app.android.notification.NotificationViewModel
import com.eventbox.app.android.order.Charge
import com.eventbox.app.android.order.ConfirmOrder
import com.eventbox.app.android.order.Order
import com.eventbox.app.android.order.OrderApi
import com.eventbox.app.android.order.OrderCompletedViewModel
import com.eventbox.app.android.order.OrderDetailsViewModel
import com.eventbox.app.android.order.OrderService
import com.eventbox.app.android.order.OrdersUnderUserViewModel
import com.eventbox.app.android.paypal.Paypal
import com.eventbox.app.android.paypal.PaypalApi
import com.eventbox.app.android.search.SearchResultsViewModel
import com.eventbox.app.android.search.SearchViewModel
import com.eventbox.app.android.location.GeoLocationViewModel
import com.eventbox.app.android.search.location.LocationService
import com.eventbox.app.android.location.LocationServiceImpl
import com.eventbox.app.android.search.location.SearchLocationViewModel
import com.eventbox.app.android.search.time.SearchTimeViewModel
import com.eventbox.app.android.search.type.SearchTypeViewModel
import com.eventbox.app.android.sessions.Session
import com.eventbox.app.android.sessions.SessionApi
import com.eventbox.app.android.sessions.SessionService
import com.eventbox.app.android.sessions.SessionViewModel
import com.eventbox.app.android.sessions.microlocation.MicroLocation
import com.eventbox.app.android.sessions.sessiontype.SessionType
import com.eventbox.app.android.sessions.track.Track
import com.eventbox.app.android.settings.Settings
import com.eventbox.app.android.settings.SettingsApi
import com.eventbox.app.android.settings.SettingsService
import com.eventbox.app.android.settings.SettingsViewModel
import com.eventbox.app.android.social.SocialLink
import com.eventbox.app.android.social.SocialLinkApi
import com.eventbox.app.android.social.SocialLinksService
import com.eventbox.app.android.speakercall.EditSpeakerViewModel
import com.eventbox.app.android.speakercall.Proposal
import com.eventbox.app.android.speakercall.SpeakersCall
import com.eventbox.app.android.speakercall.SpeakersCallProposalViewModel
import com.eventbox.app.android.speakercall.SpeakersCallViewModel
import com.eventbox.app.android.speakers.Speaker
import com.eventbox.app.android.speakers.SpeakerApi
import com.eventbox.app.android.speakers.SpeakerService
import com.eventbox.app.android.speakers.SpeakerViewModel
import com.eventbox.app.android.sponsor.Sponsor
import com.eventbox.app.android.sponsor.SponsorApi
import com.eventbox.app.android.sponsor.SponsorService
import com.eventbox.app.android.sponsor.SponsorsViewModel
import com.eventbox.app.android.ticket.Ticket
import com.eventbox.app.android.ticket.TicketApi
import com.eventbox.app.android.ticket.TicketId
import com.eventbox.app.android.ticket.TicketService
import com.eventbox.app.android.ticket.TicketsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

val commonModule = module {
    single { Preference() }
    single { Network() }
    single { Resource() }
    factory { MutableConnectionLiveData() }
    factory<LocationService> { LocationServiceImpl(androidContext(), get()) }
}

val apiModule = module {
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(AuthApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(TicketApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(FavoriteEventApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SocialLinkApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventTopicApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(AttendeeApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(OrderApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(PaypalApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventTypesApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventLocationApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(FeedbackApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SpeakerApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(EventFAQApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SessionApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SponsorApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(NotificationApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(DiscountApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SettingsApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(TaxApi::class.java)
    }

    factory { AuthHolder(get()) }
    factory { AuthService(get(), get(), get(), get(), get(), get(), get()) }

    factory { EventService(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { SpeakerService(get(), get(), get()) }
    factory { SponsorService(get(), get(), get()) }
    factory { TicketService(get(), get(), get()) }
    factory { SocialLinksService(get(), get()) }
    factory { AttendeeService(get(), get(), get()) }
    factory { OrderService(get(), get(), get(), get(), get()) }
    factory { SessionService(get(), get()) }
    factory { NotificationService(get(), get()) }
    factory { FeedbackService(get(), get()) }
    factory { SettingsService(get(), get()) }
    factory { TaxService(get(), get()) }
}

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { EventsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { StartupViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get(), get()) }
    viewModel {
        EventDetailsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SessionViewModel(get(), get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SearchResultsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AttendeeViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SearchLocationViewModel(get(), get(), get()) }
    viewModel { SearchTimeViewModel(get()) }
    viewModel { SearchTypeViewModel(get(), get(), get()) }
    viewModel { TicketsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AboutEventViewModel(get(), get()) }
    viewModel { EventFAQViewModel(get(), get()) }
    viewModel { FavoriteEventsViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { OrderCompletedViewModel(get(), get(), get(), get()) }
    viewModel { OrdersUnderUserViewModel(get(), get(), get(), get(), get()) }
    viewModel { OrderDetailsViewModel(get(), get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get(), get()) }
    viewModel { GeoLocationViewModel(get()) }
    viewModel { SmartAuthViewModel() }
    viewModel { SpeakerViewModel(get(), get()) }
    viewModel { SponsorsViewModel(get(), get()) }
    viewModel { NotificationViewModel(get(), get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { SpeakersCallViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SpeakersCallProposalViewModel(get(), get(), get(), get(), get()) }
    viewModel { EditSpeakerViewModel(get(), get(), get(), get()) }
    viewModel { FeedbackViewModel(get(), get()) }
}

val networkModule = module {

    single {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper
    }

    single {
        PagedList
            .Config
            .Builder()
            .setPageSize(5)
            .setInitialLoadSizeHint(5)
            .setEnablePlaceholders(false)
            .build()
    }

    single {
        val connectTimeout = 15 // 15s
        val readTimeout = 15 // 15s

        val builder = OkHttpClient().newBuilder()
            .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
            .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
            .addInterceptor(HostSelectionInterceptor(get()))
            .addInterceptor(RequestAuthenticator(get()))
            .addNetworkInterceptor(StethoInterceptor())

        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            builder.addInterceptor(httpLoggingInterceptor)
        }
        builder.build()
    }

    single {
        val baseUrl = BuildConfig.DEFAULT_BASE_URL
        val objectMapper: ObjectMapper = get()
        val onlineApiResourceConverter = ResourceConverter(
            objectMapper, Event::class.java, User::class.java,
            SignUp::class.java, Ticket::class.java, SocialLink::class.java, EventId::class.java,
            EventTopic::class.java, Attendee::class.java, TicketId::class.java, Order::class.java,
            Charge::class.java, Paypal::class.java, ConfirmOrder::class.java,
            CustomForm::class.java, EventLocation::class.java, EventType::class.java,
            EventSubTopic::class.java, Feedback::class.java, Speaker::class.java, FavoriteEvent::class.java,
            Session::class.java, SessionType::class.java, MicroLocation::class.java, SpeakersCall::class.java,
            Sponsor::class.java, EventFAQ::class.java, Notification::class.java, Track::class.java,
            DiscountCode::class.java, Settings::class.java, Proposal::class.java, Tax::class.java)

        Retrofit.Builder()
            .client(get())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(JSONAPIConverterFactory(onlineApiResourceConverter))
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .baseUrl(baseUrl)
            .build()
    }
}

val databaseModule = module {

    single {
        Room.databaseBuilder(androidApplication(),
            EventboxDatabase::class.java, "eventbox_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    factory {
        val database: EventboxDatabase = get()
        database.eventDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.sessionDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.userDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.ticketDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.socialLinksDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.attendeeDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.eventTopicsDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.orderDao()
    }
    factory {
        val database: EventboxDatabase = get()
        database.speakerWithEventDao()
    }
    factory {
        val database: EventboxDatabase = get()
        database.speakerDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.sponsorWithEventDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.sponsorDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.feedbackDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.speakersCallDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.notificationDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.settingsDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.taxDao()
    }
}
