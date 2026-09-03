package com.sleepshield.app.data

data class Alarm(
    val id: String,
    val kind: String,
    val title: String,
    val time: Int,
    val enabled: Boolean,
    val days: String
)

data class SleepState(
    val onboardingComplete: Boolean = false,
    val bedtime: Int = 22 * 60 + 30, // 10:30 PM
    val sleepGoalMinutes: Int = 7 * 60 + 30,
    val wakeTarget: String = "fajr",
    val shieldEnabled: Boolean = false,
    val prayerShieldConnected: Boolean = true,
    val isAmoled: Boolean = false,
    val syncToSystemClock: Boolean = false,
    val uninstallProtectionEnabled: Boolean = false,
    val blockSettingsEnabled: Boolean = false,
    val blockedPackages: Set<String> = emptySet(),
    val alarms: List<Alarm> = listOf(
        Alarm("fajr", "fajr", "Fajr", 5 * 60 + 18, true, "Every day"),
        Alarm("school", "school", "School morning", 7 * 60 + 15, true, "Mon – Fri"),
        Alarm("work", "work", "Workday", 8 * 60, false, "Mon – Fri")
    )
)

enum class WakeTarget(val key: String, val label: String, val defaultTime: Int) {
    FAJR("fajr", "Fajr", 5 * 60 + 18),
    SCHOOL("school", "School", 7 * 60 + 15),
    WORK("work", "Work", 8 * 60)
}
