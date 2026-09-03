package com.sleepshield.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleepshield.app.ui.SleepViewModel
import com.sleepshield.app.ui.components.SectionLabel
import com.sleepshield.app.ui.components.Toggle

@Composable
fun ScheduleScreen(viewModel: SleepViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val view = LocalView.current

    val showTimePicker = { kind: String ->
        val bedtime = uiState.bedtime
        val hour = bedtime / 60
        val minute = bedtime % 60
        android.app.TimePickerDialog(
            view.context,
            { _, h, m -> viewModel.addAlarm(kind, h * 60 + m) },
            hour,
            minute,
            false
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Header
        Text(
            text = "Your mornings",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Wake plans",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp
            )
            TextButton(onClick = { showTimePicker("custom") }) {
                Text(text = "Add alarm", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Intro Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.secondary
        ) {
            Row(modifier = Modifier.padding(17.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "One night, many mornings.", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Sleep Shield keeps every wake-up in one quiet place.", fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
        }

        SectionLabel(text = "Saved alarms", trailing = {
            Text(text = "${uiState.alarms.size} plans", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        })

        // Alarm List
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            uiState.alarms.forEach { alarm ->
                AlarmCard(alarm, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(17.dp))
        
        // Add Button
        OutlinedButton(
            onClick = { showTimePicker("custom") },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add another wake plan", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = "Fajr updates are shared through your Prayer Shield connection.",
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AlarmCard(alarm: com.sleepshield.app.data.Alarm, viewModel: SleepViewModel) {
    val view = LocalView.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(modifier = Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (alarm.kind) {
                    "fajr" -> Icons.Default.NightlightRound
                    "school" -> Icons.Default.School
                    "work" -> Icons.Default.Work
                    else -> Icons.Default.Alarm
                }
                val iconColor = when (alarm.kind) {
                    "school" -> Color(0xFF438A78)
                    "work" -> Color(0xFFB47A45)
                    else -> MaterialTheme.colorScheme.primary
                }
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = alarm.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = alarm.days, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Row {
                    TextButton(onClick = {
                        val hour = alarm.time / 60
                        val minute = alarm.time % 60
                        android.app.TimePickerDialog(
                            view.context,
                            { _, h, m -> viewModel.updateAlarm(alarm.id, h * 60 + m) },
                            hour,
                            minute,
                            false
                        ).show()
                    }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(30.dp)) {
                        Text("Edit", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { viewModel.deleteAlarm(alarm.id) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.height(30.dp)) {
                        Text("Delete", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(text = viewModel.formatTime(alarm.time), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Toggle(checked = alarm.enabled, onCheckedChange = { viewModel.toggleAlarm(alarm.id) })
            }
        }
    }
}
