package pl.edu.pw.meil.knr.classes

import android.app.Application
import timber.log.Timber

class HalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}