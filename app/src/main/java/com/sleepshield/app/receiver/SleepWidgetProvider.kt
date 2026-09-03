package com.sleepshield.app.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.sleepshield.app.MainActivity
import com.sleepshield.app.R
import com.sleepshield.app.data.SleepRepository
import com.sleepshield.app.data.SleepState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Base class for shared logic
open class BaseSleepWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        SleepWidgetProvider.refreshAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == SleepWidgetProvider.ACTION_TOGGLE_SHIELD) {
            val repository = SleepRepository(context.applicationContext)
            CoroutineScope(Dispatchers.Main).launch {
                repository.toggleShield()
                SleepWidgetProvider.refreshAll(context)
            }
        }
    }
}

class SleepWidgetProvider : BaseSleepWidgetProvider() {
    companion object {
        const val ACTION_TOGGLE_SHIELD = "com.sleepshield.app.ACTION_TOGGLE_SHIELD"

        fun refreshAll(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            
            // Update all types of widgets
            updateWidgets(context, appWidgetManager, SleepWidgetProvider::class.java, R.layout.widget_4x2)
            updateWidgets(context, appWidgetManager, SleepWidgetProviderSmall::class.java, R.layout.widget_1x1)
            updateWidgets(context, appWidgetManager, SleepWidgetProviderMedium::class.java, R.layout.widget_2x2)
        }

        private fun updateWidgets(context: Context, manager: AppWidgetManager, clazz: Class<*>, layoutId: Int) {
            val componentName = ComponentName(context, clazz)
            val ids = manager.getAppWidgetIds(componentName)
            if (ids.isEmpty()) return

            val repository = SleepRepository(context.applicationContext)
            CoroutineScope(Dispatchers.Main).launch {
                val state = repository.sleepStateFlow.first()
                for (id in ids) {
                    val views = RemoteViews(context.packageName, layoutId)
                    populateViews(context, views, state)
                    manager.updateAppWidget(id, views)
                }
            }
        }

        private fun populateViews(context: Context, views: RemoteViews, state: SleepState) {
            val statusColor = if (state.shieldEnabled) 0xFFB7A9FF.toInt() else 0xFF9FAAC3.toInt()
            val statusText = if (state.shieldEnabled) "Active" else "Resting"

            // Common elements
            try { views.setTextViewText(R.id.widget_status, statusText) } catch(_: Exception) {}
            try { views.setTextColor(R.id.widget_status, statusColor) } catch(_: Exception) {}

            // Times
            val bedtime = formatMinutes(state.bedtime)
            val fajrAlarm = state.alarms.find { it.kind == state.wakeTarget }
            val wake = if (fajrAlarm != null) formatMinutes(fajrAlarm.time) else "--:--"

            try { views.setTextViewText(R.id.widget_bedtime, bedtime) } catch(_: Exception) {}
            try { views.setTextViewText(R.id.widget_wake, wake) } catch(_: Exception) {}

            // Icon tint
            try { views.setInt(R.id.widget_icon, "setColorFilter", statusColor) } catch(_: Exception) {}

            // Background tap (Universal)
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_container, appPendingIntent)

            // Toggle button tap
            val toggleIntent = Intent(context, SleepWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_SHIELD
            }
            val togglePendingIntent = PendingIntent.getBroadcast(context, 1, toggleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_toggle, togglePendingIntent)
            
            // Toggle icon color
            try { views.setInt(R.id.widget_toggle, "setColorFilter", statusColor) } catch(_: Exception) {}
        }

        private fun formatMinutes(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            val amPm = if (h < 12) "AM" else "PM"
            val h12 = if (h % 12 == 0) 12 else h % 12
            return String.format(java.util.Locale.US, "%d:%02d %s", h12, m, amPm)
        }
    }
}

class SleepWidgetProviderSmall : BaseSleepWidgetProvider()
class SleepWidgetProviderMedium : BaseSleepWidgetProvider()
