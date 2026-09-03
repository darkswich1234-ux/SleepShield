package com.sleepshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepshield.app.ui.SleepViewModel
import com.sleepshield.app.ui.components.SectionLabel
import com.sleepshield.app.ui.components.Toggle

@Composable
fun SettingsScreen(viewModel: SleepViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Header
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
            text = "Make it yours",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Settings",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 28.sp
        )

        SectionLabel(text = "Connected ecosystem")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Prayer Shield", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Share Fajr times and keep your morning in sync.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Toggle(checked = uiState.prayerShieldConnected, onCheckedChange = { viewModel.togglePrayerShield() })
            }
        }

        SectionLabel(text = "Sleep plan")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                InfoRow(icon = Icons.Outlined.Nightlight, title = "Bedtime", value = viewModel.formatTime(uiState.bedtime))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.17f))
                InfoRow(icon = Icons.Outlined.HourglassEmpty, title = "Sleep goal", value = "${uiState.sleepGoalMinutes / 60}h ${uiState.sleepGoalMinutes % 60}m")
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.17f))
                InfoRow(icon = Icons.Outlined.Notifications, title = "Morning sound", value = "Soft chime")
            }
        }

        SectionLabel(text = "About Sleep Shield")
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(text = "A calmer night, a clearer morning.", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "Sleep Shield is the sleep companion to Prayer Shield — helping you protect the quiet hours and wake up for what matters.",
                    fontSize = 12.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Version ${viewModel.getAppVersion()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        SectionLabel(text = "Protection")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Uninstall Protection", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Make Sleep Shield harder to remove.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Toggle(checked = uiState.uninstallProtectionEnabled, onCheckedChange = { viewModel.setUninstallProtection(it) })
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AppSettingsAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(11.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Block Settings App", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Prevent access to system settings while active.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Toggle(checked = uiState.blockSettingsEnabled, onCheckedChange = { viewModel.setBlockSettings(it) })
                }
            }
        }

        SectionLabel(text = "Appearance")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "AMOLED Black", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Pure black background for OLED screens.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Toggle(checked = uiState.isAmoled, onCheckedChange = { viewModel.setAmoled(it) })
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Sync to System Clock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "Automatically create alarms in your device clock app.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Toggle(checked = uiState.syncToSystemClock, onCheckedChange = { viewModel.setSyncToSystemClock(it) })
            }
        }

        SectionLabel(text = "Support the Developer")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "If you find this app helpful, consider supporting its development with a small tip! It helps keep the project alive and free for everyone.", fontSize = 12.sp, lineHeight = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { /* Open Ko-fi */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tip on Ko-fi")
                }
            }
        }

        Spacer(modifier = Modifier.height(27.dp))
        TextButton(
            onClick = { viewModel.resetPlan() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Reset sleep plan", color = Color(0xFFC6536B), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 55.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
