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
import com.eventbox.app.android.ui.auth.AuthHolder
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
import com.eventbox.app.android.models.event.*
import com.eventbox.app.android.ui.event.faq.EventFAQViewModel
import com.eventbox.app.android.models.feedback.Feedback
import com.eventbox.app.android.models.news.News
import com.eventbox.app.android.ui.feedback.FeedbackViewModel
import com.eventbox.app.android.models.notification.Notification
import com.eventbox.app.android.ui.notification.NotificationViewModel
import com.eventbox.app.android.models.settings.Settings
import com.eventbox.app.android.ui.settings.SettingsViewModel
import com.eventbox.app.android.networks.api.*
import com.eventbox.app.android.search.location.GeoLocationViewModel
import com.eventbox.app.android.search.location.LocationServiceImpl
import com.eventbox.app.android.service.*
import com.eventbox.app.android.ui.event.*
import com.eventbox.app.android.ui.event.search.*
import com.eventbox.app.android.ui.news.NewsDetailViewModel
import com.eventbox.app.android.ui.news.NewsViewModel
import com.fasterxml.jackson.core.JsonParser
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
    factory<SearchLocationService> { LocationServiceImpl(androidContext(), get()) }
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
        retrofit.create(FavoriteEventApi::class.java)
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
        retrofit.create(EventFAQApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(NotificationApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(SettingsApi::class.java)
    }
    single {
        val retrofit: Retrofit = get()
        retrofit.create(NewsApi::class.java)
    }

    factory { AuthHolder(get()) }
    factory { AuthService(get(), get(), get(), get()) }
    factory { EventService(get(), get(), get(), get(), get(), get()) }
    factory { NotificationService(get(), get()) }
    factory { FeedbackService(get(), get()) }
    factory { SettingsService(get(), get()) }
    factory { NewsService(get()) }
}

val viewModelModule = module {

    viewModel { LoginViewModel(get(), get(), get(), get()) }
    viewModel { EventsViewModel(get(), get(), get(), get(), get()) }
    viewModel { StartupViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { SignUpViewModel(get(), get(), get()) }
    viewModel { EventDetailsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { EventAddViewModel(get(), get(), get(), get()) }
    viewModel { SearchResultsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SearchLocationViewModel(get(), get(), get()) }
    viewModel { SearchTimeViewModel(get()) }
    viewModel { SearchTypeViewModel(get(), get(), get()) }
    viewModel { AboutEventViewModel(get(), get()) }
    viewModel { EventFAQViewModel(get(), get()) }
    viewModel { FavoriteEventsViewModel(get(), get(), get()) }
    viewModel { NewsViewModel(get(), get(), get(), get()) }
    viewModel { NewsDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { EditProfileViewModel(get(), get(), get()) }
    viewModel { GeoLocationViewModel(get()) }
    viewModel { SmartAuthViewModel() }
    viewModel { NotificationViewModel(get(), get(), get(), get()) }
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { FeedbackViewModel(get(), get()) }
}

val networkModule = module {

    single {
        val objectMapper = jacksonObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
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
            objectMapper, Event::class.java, User::class.java, News::class.java,
            SignUp::class.java, EventId::class.java, EventType::class.java,
            EventLocation::class.java, Feedback::class.java, Settings::class.java,
            EventFAQ::class.java, Notification::class.java, FavoriteEvent::class.java)

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
        database.userDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.feedbackDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.notificationDao()
    }

    factory {
        val database: EventboxDatabase = get()
        database.settingsDao()
    }
}
