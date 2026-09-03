package com.sleepshield.app

import android.app.Application
import com.sleepshield.app.data.SleepRepository
import com.sleepshield.app.sync.PrayerShieldSync

class MainApplication : Application() {
    lateinit var sleepRepository: SleepRepository
    lateinit var prayerShieldSync: PrayerShieldSync

    override fun onCreate() {
        super.onCreate()
        sleepRepository = SleepRepository(this)
        prayerShieldSync = PrayerShieldSync(this, sleepRepository)
        prayerShieldSync.startSync()
    }
}
