package com.sleepshield.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.sleepshield.app.MainActivity
import com.sleepshield.app.data.SleepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SleepAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var repository: SleepRepository

    override fun onCreate() {
        super.onCreate()
        repository = SleepRepository(this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // The config can be set in code as well, but we have it in XML.
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            scope.launch {
                val state = repository.sleepStateFlow.first()
                if (state.shieldEnabled) {
                    if (packageName in state.blockedPackages) {
                        blockApp()
                    } else if (state.blockSettingsEnabled && (packageName == "com.android.settings" || packageName == "com.samsung.android.settings")) {
                        blockApp()
                    }
                }
            }
        }
    }

    private fun blockApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("BLOCKED", true)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
