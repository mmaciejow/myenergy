package net.myenv.myenergy

import android.app.Application
import net.myenv.myenergy.di.injectFeature
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.logger.Level

class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        configureDi()
    }

    private fun configureDi() =
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApp)
            injectFeature()
        }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }

}