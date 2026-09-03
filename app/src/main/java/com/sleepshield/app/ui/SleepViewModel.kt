package com.sleepshield.app.ui

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sleepshield.app.data.SleepRepository
import com.sleepshield.app.data.SleepState
import com.sleepshield.app.data.WakeTarget
import com.sleepshield.app.util.SleepAlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppInfo(
    val packageName: String,
    val label: String,
    val category: String,
    val isSystem: Boolean
)

class SleepViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SleepRepository(application)
    private val packageManager = application.packageManager
    private val scheduler = SleepAlarmScheduler(application)

    val uiState: StateFlow<SleepState> = repository.sleepStateFlow
        .onEach { state ->
            // Re-schedule all enabled alarms whenever the state changes to ensure they are set
            // In a production app, you might want to be more surgical here.
            state.alarms.forEach { alarm ->
                if (alarm.enabled) {
                    scheduler.schedule(alarm)
                } else {
                    scheduler.cancel(alarm)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SleepState()
        )

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    private fun loadInstalledApps() {
        viewModelScope.launch {
            val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { 
                    // Only show apps that can be launched (user-facing)
                    packageManager.getLaunchIntentForPackage(it.packageName) != null 
                    // Keep our apps out of the list to prevent self-blocking accidents
                    && it.packageName != "com.sleepshield.app" 
                    && it.packageName != "com.prayershield.app"
                }
                .map { app ->
                    AppInfo(
                        packageName = app.packageName,
                        label = app.loadLabel(packageManager).toString(),
                        category = getSmarterCategory(app),
                        isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                .sortedBy { it.label }
            _installedApps.value = apps
        }
    }

    private fun getSmarterCategory(app: ApplicationInfo): String {
        val pkg = app.packageName.lowercase()
        val label = app.loadLabel(packageManager).toString().lowercase()

        // 1. Explicit Overrides
        when {
            pkg.contains("com.openai.chatgpt") || pkg.contains("ai.chat") || pkg.contains("claude") || pkg.contains("gemini") -> return "Productivity"
            pkg.contains("com.google.android.apps.dynamite") || pkg.contains("google.chat") || pkg.contains("slack") || pkg.contains("discord") || pkg.contains("teams") -> return "Social Media"
            pkg.contains("samsung.android.mdx") || pkg.contains("samsungflow") || pkg.contains("sideSync") -> return "Tools & Utilities"
            pkg.contains("com.whatsapp") || pkg.contains("com.facebook.orca") || pkg.contains("telegram") || pkg.contains("signal") -> return "Social Media"
            pkg.contains("amazon") || pkg.contains("ebay") || pkg.contains("shopify") || pkg.contains("aliexpress") || pkg.contains("temu") || pkg.contains("walmart") || pkg.contains("target") -> return "Shopping"
            pkg.contains("netflix") || pkg.contains("disney") || pkg.contains("hulu") || pkg.contains("youtube") || pkg.contains("twitch") || pkg.contains("spotify") || pkg.contains("music") -> return "Streaming"
        }

        // 2. System Category Check (Android O+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            when (app.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return "Social Media"
                ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_AUDIO -> return "Streaming"
                ApplicationInfo.CATEGORY_GAME -> return "Games"
                ApplicationInfo.CATEGORY_MAPS -> return "Tools & Utilities"
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return "Productivity"
                ApplicationInfo.CATEGORY_NEWS -> return "News"
            }
        }

        // 3. Keyword Heuristics
        return when {
            pkg.contains("game") || pkg.contains("play") && !pkg.contains("store") -> "Games"
            pkg.contains("shop") || pkg.contains("store") || pkg.contains("mall") -> "Shopping"
            pkg.contains("tool") || pkg.contains("util") || pkg.contains("service") || (app.flags and ApplicationInfo.FLAG_SYSTEM != 0) -> "Tools & Utilities"
            else -> "Other"
        }
    }

    fun setOnboardingComplete(complete: Boolean) = viewModelScope.launch {
        repository.setOnboardingComplete(complete)
    }

    fun updateBedtime(minutes: Int) = viewModelScope.launch {
        repository.updateBedtime(minutes)
    }

    fun setWakeTarget(target: String) = viewModelScope.launch {
        repository.setWakeTarget(target)
    }

    fun toggleShield() = viewModelScope.launch {
        repository.toggleShield()
    }

    fun togglePrayerShield() = viewModelScope.launch {
        repository.togglePrayerShield()
    }

    fun togglePackageBlocked(packageName: String) = viewModelScope.launch {
        repository.togglePackageBlocked(packageName)
    }

    fun setBlockedPackages(packages: Set<String>) = viewModelScope.launch {
        repository.setBlockedPackages(packages)
    }

    fun setUninstallProtection(enabled: Boolean) = viewModelScope.launch {
        repository.setUninstallProtection(enabled)
    }

    fun setBlockSettings(enabled: Boolean) = viewModelScope.launch {
        repository.setBlockSettings(enabled)
    }

    fun blockAllExceptEssential() = viewModelScope.launch {
        val essential = listOf(
            "com.android.settings", "com.android.phone", "com.android.server.telecom",
            "com.google.android.apps.messaging", "com.android.contacts",
            "com.android.dialer", "com.android.deskclock", "com.google.android.deskclock",
            "com.sleepshield.app", "com.prayershield.app"
        )
        val allPackages = _installedApps.value.map { it.packageName }
        val toBlock = allPackages.filter { it !in essential }.toSet()
        repository.setBlockedPackages(toBlock)
        repository.setShieldEnabled(true)
    }

    fun setAmoled(enabled: Boolean) = viewModelScope.launch {
        repository.setAmoled(enabled)
    }

    fun setSyncToSystemClock(enabled: Boolean) = viewModelScope.launch {
        repository.setSyncToSystemClock(enabled)
    }

    fun toggleAlarm(id: String) = viewModelScope.launch {
        repository.toggleAlarm(id)
    }

    fun addAlarm(kind: String, time: Int) = viewModelScope.launch {
        repository.addAlarm(kind, time)
        val state = uiState.value
        if (state.syncToSystemClock) {
            // Find the newly added alarm (by time/kind since ID is generated)
            // Or just rely on the onEach block for local scheduling
            // For system clock, we trigger it explicitly
            val tempAlarm = com.sleepshield.app.data.Alarm("", kind, kind, time, true, "")
            scheduler.createSystemAlarm(tempAlarm)
        }
    }

    fun updateAlarm(id: String, time: Int) = viewModelScope.launch {
        repository.updateAlarm(id, time)
    }

    fun deleteAlarm(id: String) = viewModelScope.launch {
        repository.deleteAlarm(id)
    }

    fun resetPlan() = viewModelScope.launch {
        repository.resetPlan()
    }

    fun getRecommendedWake(targetKey: String): Int {
        return WakeTarget.values().find { it.key == targetKey }?.defaultTime ?: 0
    }
    
    fun formatTime(minutes: Int): String {
        val normalized = ((minutes % 1440) + 1440) % 1440
        val hour = normalized / 60
        val minute = normalized % 60
        val suffix = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return String.format("%d:%02d %s", hour12, minute, suffix)
    }

    fun getAppVersion(): String {
        return try {
            val pInfo = packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
}
