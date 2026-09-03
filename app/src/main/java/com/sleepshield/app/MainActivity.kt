package com.sleepshield.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sleepshield.app.receiver.SleepWidgetProvider
import com.sleepshield.app.ui.SleepApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (intent.getBooleanExtra("ALARM_TRIGGERED", false)) {
            turnOnScreen()
        }

        enableEdgeToEdge()
        setContent {
            SleepApp()
        }
        
        // Refresh widget when opening app
        SleepWidgetProvider.refreshAll(this)
    }

    override fun onResume() {
        super.onResume()
        SleepWidgetProvider.refreshAll(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("ALARM_TRIGGERED", false)) {
            turnOnScreen()
        }
    }

    private fun turnOnScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}
