package com.eventbox.app.android

import android.content.Context
import androidx.multidex.MultiDexApplication
import com.facebook.stetho.Stetho
import com.jakewharton.threetenabp.AndroidThreeTen
import com.eventbox.app.android.di.apiModule
import com.eventbox.app.android.di.commonModule
import com.eventbox.app.android.di.databaseModule
import com.eventbox.app.android.di.networkModule
import com.eventbox.app.android.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class EventboxEntryPoint : MultiDexApplication() {

    companion object {
        @JvmStatic
        var appContext: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        startKoin {
            androidLogger()
            androidContext(this@EventboxEntryPoint)
            modules(listOf(
                commonModule,
                apiModule,
                viewModelModule,
                networkModule,
                databaseModule
            ))
        }
        Timber.plant(Timber.DebugTree())
        AndroidThreeTen.init(applicationContext)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }
}
