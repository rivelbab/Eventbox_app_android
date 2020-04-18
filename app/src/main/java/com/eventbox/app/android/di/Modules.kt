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
import com.eventbox.app.android.data.db.EventboxDatabase
import com.eventbox.app.android.ui.StartupViewModel
import com.eventbox.app.android.ui.event.AboutEventViewModel
import com.eventbox.app.android.models.attendees.Attendee
import com.eventbox.app.android.networks.api.AttendeeApi
import com.eventbox.app.android.service.AttendeeService
import com.eventbox.app.android.ui.attendees.AttendeeViewModel
import com.eventbox.app.android.models.attendees.CustomForm
import com.eventbox.app.android.networks.api.AuthApi
import com.eventbox.app.android.ui.auth.AuthHolder
import com.eventbox.app.android.service.AuthService
import com.eventbox.app.android.ui.auth.AuthViewModel
import com.eventbox.app.android.ui.user.EditProfileViewModel
import com.eventbox.app.android.ui.auth.LoginViewModel
import com.eventbox.app.android.ui.user.ProfileViewModel
import com.eventbox.app.android.networks.payloads.auth.RequestAuthenticator
import com.eventbox.app.android.models.auth.SignUp
import com.eventbox.app.android.ui.auth.SignUpViewModel
import com.eventbox.app.android.auth.SmartAuthViewModel
import com.eventbox.app.android.models.user.User
import com.eventbox.app.android.networks.connectivity.MutableConnectionLiveData
import com.eventbox.app.android.config.Network
import com.eventbox.app.android.config.Preference
import com.eventbox.app.android.config.Resource
import com.eventbox.app.android.networks.api.DiscountApi
import com.eventbox.app.android.models.payment.DiscountCode
import com.eventbox.app.android.models.event.Event
import com.eventbox.app.android.networks.api.EventApi
import com.eventbox.app.android.ui.event.EventDetailsViewModel
import com.eventbox.app.android.models.event.EventId
import com.eventbox.app.android.service.EventService
import com.eventbox.app.android.ui.event.EventsViewModel
import com.eventbox.app.android.models.event.EventFAQ
import com.eventbox.app.android.networks.api.EventFAQApi
import com.eventbox.app.android.ui.event.faq.EventFAQViewModel
import com.eventbox.app.android.models.event.EventLocation
import com.eventbox.app.android.networks.api.EventLocationApi
import com.eventbox.app.android.models.event.EventSubTopic
import com.eventbox.app.android.models.payment.Tax
import com.eventbox.app.android.networks.api.TaxApi
import com.eventbox.app.android.service.TaxService
import com.eventbox.app.android.models.event.EventTopic
import com.eventbox.app.android.networks.api.EventTopicApi
import com.eventbox.app.android.models.event.EventType
import com.eventbox.app.android.networks.api.EventTypesApi
import com.eventbox.app.android.models.event.FavoriteEvent
import com.eventbox.app.android.networks.api.FavoriteEventApi
import com.eventbox.app.android.ui.event.FavoriteEventsViewModel
import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.networks.api.FeedbackApi
import com.eventbox.app.android.service.FeedbackService
import com.eventbox.app.android.ui.feedback.FeedbackViewModel
import com.eventbox.app.android.models.notification.Notification
import com.eventbox.app.android.networks.api.NotificationApi
import com.eventbox.app.android.service.NotificationService
import com.eventbox.app.android.ui.notification.NotificationViewModel
import com.eventbox.app.android.models.payment.Charge
import com.eventbox.app.android.models.payment.ConfirmOrder
import com.eventbox.app.android.models.payment.Order
import com.eventbox.app.android.networks.api.OrderApi
import com.eventbox.app.android.ui.payment.OrderCompletedViewModel
import com.eventbox.app.android.ui.payment.OrderDetailsViewModel
import com.eventbox.app.android.service.OrderService
import com.eventbox.app.android.ui.payment.OrdersUnderUserViewModel
import com.eventbox.app.android.models.payment.Paypal
import com.eventbox.app.android.networks.api.PaypalApi
import com.eventbox.app.android.ui.event.search.SearchResultsViewModel
import com.eventbox.app.android.ui.event.search.SearchViewModel
import com.eventbox.app.android.location.GeoLocationViewModel
import com.eventbox.app.android.service.SearchLocationService
import com.eventbox.app.android.location.SearchLocationServiceImpl
import com.eventbox.app.android.ui.event.search.SearchLocationViewModel
import com.eventbox.app.android.ui.event.search.SearchTimeViewModel
import com.eventbox.app.android.ui.event.search.SearchTypeViewModel
import com.eventbox.app.android.models.session.Session
import com.eventbox.app.android.networks.api.SessionApi
import com.eventbox.app.android.service.SessionService
import com.eventbox.app.android.ui.session.SessionViewModel
import com.eventbox.app.android.models.session.MicroLocation
import com.eventbox.app.android.models.session.SessionType
import com.eventbox.app.android.models.session.Track
import com.eventbox.app.android.models.settings.Settings
import com.eventbox.app.android.networks.api.SettingsApi
import com.eventbox.app.android.service.SettingsService
import com.eventbox.app.android.ui.settings.SettingsViewModel
import com.eventbox.app.android.models.event.SocialLink
import com.eventbox.app.android.networks.api.SocialLinkApi
import com.eventbox.app.android.service.SocialLinksService
import com.eventbox.app.android.ui.speakers.EditSpeakerViewModel
import com.eventbox.app.android.models.speakers.Proposal
import com.eventbox.app.android.models.speakers.SpeakersCall
import com.eventbox.app.android.ui.speakers.SpeakersCallProposalViewModel
import com.eventbox.app.android.ui.speakers.SpeakersCallViewModel
import com.eventbox.app.android.models.speakers.Speaker
import com.eventbox.app.android.networks.api.SpeakerApi
import com.eventbox.app.android.service.SpeakerService
import com.eventbox.app.android.ui.speakers.SpeakerViewModel
import com.eventbox.app.android.models.sponsor.Sponsor
import com.eventbox.app.android.networks.api.SponsorApi
import com.eventbox.app.android.service.SponsorService
import com.eventbox.app.android.ui.sponsor.SponsorsViewModel
import com.eventbox.app.android.models.payment.Ticket
import com.eventbox.app.android.networks.api.TicketApi
import com.eventbox.app.android.models.payment.TicketId
import com.eventbox.app.android.service.TicketService
import com.eventbox.app.android.ui.payment.TicketsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

val commonModule = module {
    single { Preference() }
    single { Network() }
    single { Resource() }
    factory { MutableConnectionLiveData() }
    factory<SearchLocationService> { SearchLocationServiceImpl(androidContext(), get()) }
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
    factory {
        AuthService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    factory {
        EventService(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory {
        SpeakerService(
            get(),
            get(),
            get()
        )
    }
    factory {
        SponsorService(
            get(),
            get(),
            get()
        )
    }
    factory {
        TicketService(
            get(),
            get(),
            get()
        )
    }
    factory { SocialLinksService(get(), get()) }
    factory {
        AttendeeService(
            get(),
            get(),
            get()
        )
    }
    factory {
        OrderService(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory { SessionService(get(), get()) }
    factory {
        NotificationService(
            get(),
            get()
        )
    }
    factory { FeedbackService(get(), get()) }
    factory { SettingsService(get(), get()) }
    factory { TaxService(get(), get()) }
}

val viewModelModule = module {
    viewModel {
        LoginViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        EventsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        StartupViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel {
        SignUpViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        EventDetailsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SessionViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SearchViewModel(
            get(),
            get()
        )
    }
    viewModel {
        SearchResultsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        AttendeeViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SearchLocationViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel { SearchTimeViewModel(get()) }
    viewModel {
        SearchTypeViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        TicketsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        AboutEventViewModel(
            get(),
            get()
        )
    }
    viewModel {
        EventFAQViewModel(
            get(),
            get()
        )
    }
    viewModel {
        FavoriteEventsViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SettingsViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        OrderCompletedViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        OrdersUnderUserViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        OrderDetailsViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        EditProfileViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel { GeoLocationViewModel(get()) }
    viewModel { SmartAuthViewModel() }
    viewModel {
        SpeakerViewModel(
            get(),
            get()
        )
    }
    viewModel {
        SponsorsViewModel(
            get(),
            get()
        )
    }
    viewModel {
        NotificationViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        AuthViewModel(
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SpeakersCallViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        SpeakersCallProposalViewModel(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        EditSpeakerViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel {
        FeedbackViewModel(
            get(),
            get()
        )
    }
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
            .addInterceptor(
                RequestAuthenticator(
                    get()
                )
            )
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
