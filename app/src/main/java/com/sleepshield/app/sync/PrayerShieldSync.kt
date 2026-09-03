package com.sleepshield.app.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import com.sleepshield.app.data.SleepRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class PrayerShieldSync(private val context: Context, private val repository: SleepRepository) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    fun startSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                try {
                    val state = repository.sleepStateFlow.first()
                    if (state.prayerShieldConnected) {
                        queryPrayerShield()
                    }
                } catch (e: Exception) {
                    Log.e("PrayerShieldSync", "Sync error", e)
                }
                delay(3600000) // Poll every hour
            }
        }
    }

    private suspend fun queryPrayerShield() {
        val uri = Uri.parse("content://com.prayershield.app.prayerdata/today")
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val fajrMinutesIndex = it.getColumnIndex("fajr_minutes")
                val syncEnabledIndex = it.getColumnIndex("sync_enabled")
                
                if (fajrMinutesIndex != -1 && syncEnabledIndex != -1) {
                    val fajrMinutes = it.getInt(fajrMinutesIndex)
                    val remoteSyncEnabled = it.getInt(syncEnabledIndex) == 1
                    
                    if (remoteSyncEnabled && fajrMinutes > 0) {
                        onFajrTimeUpdated(fajrMinutes)
                    }
                }
            }
        }
    }

    private suspend fun onFajrTimeUpdated(newMinutes: Int) {
        val state = repository.sleepStateFlow.first()
        val fajrAlarm = state.alarms.find { it.kind == "fajr" }
        if (fajrAlarm != null && fajrAlarm.time != newMinutes) {
            Log.d("PrayerShieldSync", "Updating Fajr time to $newMinutes")
            repository.updateAlarm(fajrAlarm.id, newMinutes)
        }
    }
}
