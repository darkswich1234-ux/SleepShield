package com.sleepshield.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepshield.app.ui.SleepViewModel
import com.sleepshield.app.ui.components.Pill
import com.sleepshield.app.ui.components.SectionLabel
import com.sleepshield.app.ui.components.Toggle

@Composable
fun DashboardScreen(
    viewModel: SleepViewModel,
    onNavigateToShield: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val view = LocalView.current
    val context = LocalContext.current

    // Check for blocked intent
    var showBlockedOverlay by remember { mutableStateOf(false) }
    var showAlarmOverlay by remember { mutableStateOf(false) }
    var triggeredAlarmId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val activity = context as? android.app.Activity
        if (activity?.intent?.getBooleanExtra("BLOCKED", false) == true) {
            showBlockedOverlay = true
            activity.intent.removeExtra("BLOCKED")
        }
        if (activity?.intent?.getBooleanExtra("ALARM_TRIGGERED", false) == true) {
            triggeredAlarmId = activity.intent.getStringExtra("ALARM_ID")
            showAlarmOverlay = true
            activity.intent.removeExtra("ALARM_TRIGGERED")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // ... (rest of the dashboard)
            Text(
                text = "Wednesday, September 2",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Good evening.",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 28.sp
                )
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hero Card (Night Backdrop)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        val bedtime = uiState.bedtime
                        val hour = bedtime / 60
                        val minute = bedtime % 60
                        android.app.TimePickerDialog(
                            view.context,
                            { _, h, m -> viewModel.updateBedtime(h * 60 + m) },
                            hour,
                            minute,
                            false
                        ).show()
                    }
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (uiState.shieldEnabled) "Sleep mode is on" else "Ready when you are",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Make tonight count.",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(49.dp)
                                .clip(RoundedCornerShape(19.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NightlightRound, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = "Bedtime", style = MaterialTheme.typography.labelSmall)
                            Text(text = viewModel.formatTime(uiState.bedtime), style = MaterialTheme.typography.titleLarge, fontSize = 21.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Box(modifier = Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.primary))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Wake at", style = MaterialTheme.typography.labelSmall)
                            val recommendedWake = viewModel.getRecommendedWake(uiState.wakeTarget)
                            Text(text = viewModel.formatTime(recommendedWake), style = MaterialTheme.typography.titleLarge, fontSize = 21.sp)
                        }
                    }
                }
            }

            SectionLabel(text = "Smart wake", trailing = {
                Text(text = "${uiState.wakeTarget.capitalize()} plan", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            })

            // Wake Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(17.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(43.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (uiState.wakeTarget == "fajr") Icons.Default.AutoAwesome else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val recommendedWake = viewModel.getRecommendedWake(uiState.wakeTarget)
                            Text(text = "Wake gently at ${viewModel.formatTime(recommendedWake)}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = if (uiState.wakeTarget == "fajr") {
                                    if (uiState.prayerShieldConnected) "Fajr time synced from Prayer Shield"
                                    else "For more accurate Fajr times link Sleep Shield to Prayer Shield."
                                } else "Your ${uiState.wakeTarget} start time",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (uiState.wakeTarget == "fajr" && !uiState.prayerShieldConnected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(17.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pill(label = "Fajr", active = uiState.wakeTarget == "fajr") { viewModel.setWakeTarget("fajr") }
                        Pill(label = "School", active = uiState.wakeTarget == "school") { viewModel.setWakeTarget("school") }
                        Pill(label = "Work", active = uiState.wakeTarget == "work") { viewModel.setWakeTarget("work") }
                    }
                }
            }

            SectionLabel(text = "Tonight's shield")

            // Shield Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondary,
                onClick = onNavigateToShield
            ) {
                Row(modifier = Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(7.dp))
                            Text(text = if (uiState.shieldEnabled) "Your night is protected" else "Protect your wind-down", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(
                            text = if (uiState.shieldEnabled) "${uiState.blockedPackages.size} app groups are blocked until morning." else "Block the apps that keep you scrolling past bedtime.",
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Toggle(checked = uiState.shieldEnabled, onCheckedChange = { viewModel.toggleShield() })
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            Text(
                text = "Sleep Shield works alongside Prayer Shield so your nights support your mornings.",
                style = MaterialTheme.typography.labelSmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 22.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
        }

        // Blocked Overlay
        if (showBlockedOverlay) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Sleep Mode is Active",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This app is currently blocked to help you rest. You can unblock apps in the Sleep Shield settings if needed.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    Button(
                        onClick = { showBlockedOverlay = false },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("I understand", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Alarm Ringing Overlay
        if (showAlarmOverlay) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    val alarm = uiState.alarms.find { it.id == triggeredAlarmId }
                    Text(
                        text = alarm?.title ?: "Time to wake up",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = viewModel.formatTime(uiState.bedtime), // Using formatTime for current triggered time if needed
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                    Button(
                        onClick = { 
                            showAlarmOverlay = false 
                            val ns = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            ns.cancel(triggeredAlarmId?.hashCode() ?: 0)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth().height(64.dp)
                    ) {
                        Text("Dismiss Alarm", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
