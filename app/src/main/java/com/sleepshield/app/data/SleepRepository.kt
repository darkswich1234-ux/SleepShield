package com.sleepshield.app.data

import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sleepshield.app.receiver.SleepWidgetProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sleep_settings")

class SleepRepository(private val context: Context) {
    private val gson = Gson()

    private fun updateWidgets() {
        SleepWidgetProvider.refreshAll(context)
    }

    private fun broadcastStatus(enabled: Boolean) {
        val intent = Intent("com.sleepshield.app.SHIELD_STATUS_CHANGED").apply {
            putExtra("enabled", enabled)
            // Specific for Samsung Modes & Routines if it supports custom broadcasts
            // Also standard for Tasker, MacroDroid, etc.
            setPackage(context.packageName) // Limit to same app for now unless global is needed
        }
        context.sendBroadcast(intent)
        
        // Global broadcast for system-wide routines
        val globalIntent = Intent("com.sleepshield.app.ACTION_SLEEP_MODE").apply {
            putExtra("is_active", enabled)
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }
        context.sendBroadcast(globalIntent)
    }

    private object PreferencesKeys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val BEDTIME = intPreferencesKey("bedtime")
        val SLEEP_GOAL = intPreferencesKey("sleep_goal")
        val WAKE_TARGET = stringPreferencesKey("wake_target")
        val SHIELD_ENABLED = booleanPreferencesKey("shield_enabled")
        val PRAYER_SHIELD_CONNECTED = booleanPreferencesKey("prayer_shield_connected")
        val IS_AMOLED = booleanPreferencesKey("is_amoled")
        val SYNC_TO_SYSTEM_CLOCK = booleanPreferencesKey("sync_to_system_clock")
        val UNINSTALL_PROTECTION = booleanPreferencesKey("uninstall_protection")
        val BLOCK_SETTINGS = booleanPreferencesKey("block_settings")
        val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
        val ALARMS = stringPreferencesKey("alarms")
    }

    val sleepStateFlow: Flow<SleepState> = context.dataStore.data.map { preferences ->
        val alarmsJson = preferences[PreferencesKeys.ALARMS]
        val alarms: List<Alarm> = if (alarmsJson != null) {
            val type = object : TypeToken<List<Alarm>>() {}.type
            gson.fromJson(alarmsJson, type)
        } else {
            listOf(
                Alarm("fajr", "fajr", "Fajr", 5 * 60 + 18, true, "Every day"),
                Alarm("school", "school", "School morning", 7 * 60 + 15, true, "Mon – Fri"),
                Alarm("work", "work", "Workday", 8 * 60, false, "Mon – Fri")
            )
        }

        SleepState(
            onboardingComplete = preferences[PreferencesKeys.ONBOARDING_COMPLETE] ?: false,
            bedtime = preferences[PreferencesKeys.BEDTIME] ?: (22 * 60 + 30),
            sleepGoalMinutes = preferences[PreferencesKeys.SLEEP_GOAL] ?: (7 * 60 + 30),
            wakeTarget = preferences[PreferencesKeys.WAKE_TARGET] ?: "fajr",
            shieldEnabled = preferences[PreferencesKeys.SHIELD_ENABLED] ?: false,
            prayerShieldConnected = preferences[PreferencesKeys.PRAYER_SHIELD_CONNECTED] ?: true,
            isAmoled = preferences[PreferencesKeys.IS_AMOLED] ?: false,
            syncToSystemClock = preferences[PreferencesKeys.SYNC_TO_SYSTEM_CLOCK] ?: false,
            uninstallProtectionEnabled = preferences[PreferencesKeys.UNINSTALL_PROTECTION] ?: false,
            blockSettingsEnabled = preferences[PreferencesKeys.BLOCK_SETTINGS] ?: false,
            blockedPackages = preferences[PreferencesKeys.BLOCKED_PACKAGES] ?: emptySet(),
            alarms = alarms
        )
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = complete }
    }

    suspend fun updateBedtime(minutes: Int) {
        context.dataStore.edit { it[PreferencesKeys.BEDTIME] = minutes }
        updateWidgets()
    }

    suspend fun setWakeTarget(target: String) {
        context.dataStore.edit { it[PreferencesKeys.WAKE_TARGET] = target }
        updateWidgets()
    }

    suspend fun toggleShield() {
        context.dataStore.edit { 
            val current = it[PreferencesKeys.SHIELD_ENABLED] ?: false
            val newVal = !current
            it[PreferencesKeys.SHIELD_ENABLED] = newVal
            broadcastStatus(newVal)
        }
        updateWidgets()
    }

    suspend fun setShieldEnabled(enabled: Boolean) {
        context.dataStore.edit { 
            it[PreferencesKeys.SHIELD_ENABLED] = enabled 
            broadcastStatus(enabled)
        }
        updateWidgets()
    }

    suspend fun togglePrayerShield() {
        context.dataStore.edit {
            val current = it[PreferencesKeys.PRAYER_SHIELD_CONNECTED] ?: true
            it[PreferencesKeys.PRAYER_SHIELD_CONNECTED] = !current
        }
        updateWidgets()
    }

    suspend fun setAmoled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_AMOLED] = enabled }
    }

    suspend fun setSyncToSystemClock(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SYNC_TO_SYSTEM_CLOCK] = enabled }
    }

    suspend fun setUninstallProtection(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.UNINSTALL_PROTECTION] = enabled }
    }

    suspend fun setBlockSettings(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BLOCK_SETTINGS] = enabled }
    }

    suspend fun togglePackageBlocked(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.BLOCKED_PACKAGES] ?: emptySet()
            if (packageName in current) {
                preferences[PreferencesKeys.BLOCKED_PACKAGES] = current - packageName
            } else {
                preferences[PreferencesKeys.BLOCKED_PACKAGES] = current + packageName
            }
        }
    }

    suspend fun setBlockedPackages(packages: Set<String>) {
        context.dataStore.edit { it[PreferencesKeys.BLOCKED_PACKAGES] = packages }
    }

    suspend fun toggleAlarm(id: String) {
        context.dataStore.edit { preferences ->
            val currentAlarms = getAlarms(preferences)
            val newAlarms = currentAlarms.map { 
                if (it.id == id) it.copy(enabled = !it.enabled) else it
            }
            preferences[PreferencesKeys.ALARMS] = gson.toJson(newAlarms)
        }
    }

    suspend fun addAlarm(kind: String, time: Int) {
        context.dataStore.edit { preferences ->
            val currentAlarms = getAlarms(preferences).toMutableList()
            val newAlarm = Alarm(
                id = System.currentTimeMillis().toString(),
                kind = kind,
                title = when(kind) {
                    "fajr" -> "Fajr"
                    "school" -> "School morning"
                    "work" -> "Workday"
                    else -> "Morning alarm"
                },
                time = time,
                enabled = true,
                days = if (kind == "fajr") "Every day" else "Mon – Fri"
            )
            currentAlarms.add(newAlarm)
            preferences[PreferencesKeys.ALARMS] = gson.toJson(currentAlarms)
        }
    }

    suspend fun updateAlarm(id: String, time: Int) {
        context.dataStore.edit { preferences ->
            val currentAlarms = getAlarms(preferences)
            val newAlarms = currentAlarms.map { 
                if (it.id == id) it.copy(time = time) else it
            }
            preferences[PreferencesKeys.ALARMS] = gson.toJson(newAlarms)
        }
    }

    suspend fun deleteAlarm(id: String) {
        context.dataStore.edit { preferences ->
            val currentAlarms = getAlarms(preferences)
            val newAlarms = currentAlarms.filter { it.id != id }
            preferences[PreferencesKeys.ALARMS] = gson.toJson(newAlarms)
        }
    }

    private fun getAlarms(preferences: Preferences): List<Alarm> {
        val alarmsJson = preferences[PreferencesKeys.ALARMS]
        return if (alarmsJson != null) {
            val type = object : TypeToken<List<Alarm>>() {}.type
            gson.fromJson(alarmsJson, type)
        } else {
            listOf(
                Alarm("fajr", "fajr", "Fajr", 5 * 60 + 18, true, "Every day"),
                Alarm("school", "school", "School morning", 7 * 60 + 15, true, "Mon – Fri"),
                Alarm("work", "work", "Workday", 8 * 60, false, "Mon – Fri")
            )
        }
    }

    suspend fun resetPlan() {
        context.dataStore.edit { it.clear() }
    }
}
