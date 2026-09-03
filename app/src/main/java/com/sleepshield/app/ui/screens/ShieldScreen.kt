package com.sleepshield.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.sleepshield.app.ui.AppInfo
import com.sleepshield.app.ui.SleepViewModel
import com.sleepshield.app.ui.components.SectionLabel
import com.sleepshield.app.ui.components.Toggle

@Composable
fun ShieldScreen(viewModel: SleepViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val scrollState = rememberScrollState()

    val groupedApps = remember(installedApps) {
        installedApps.groupBy { it.category }
    }

    val categories = listOf("Social Media", "Streaming", "Games", "Shopping", "Productivity", "News", "Tools & Utilities", "Other")

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
            text = "Protect your rest",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = "Sleep shield",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Actions
        Button(
            onClick = { viewModel.blockAllExceptEssential() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Block all except essential", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(if (uiState.shieldEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (uiState.shieldEnabled) Color.White.copy(alpha = 0.18f) else MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (uiState.shieldEnabled) Icons.Default.Shield else Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = if (uiState.shieldEnabled) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (uiState.shieldEnabled) "Sleep mode is active" else "Sleep mode is resting",
                    color = if (uiState.shieldEnabled) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp
                )
                Text(
                    text = if (uiState.shieldEnabled) "${uiState.blockedPackages.size} apps are protected until morning." else "Turn it on when you are ready to put your phone down.",
                    color = if (uiState.shieldEnabled) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.widthIn(max = 270.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = { viewModel.toggleShield() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.shieldEnabled) Color.White else MaterialTheme.colorScheme.primary,
                        contentColor = if (uiState.shieldEnabled) MaterialTheme.colorScheme.primary else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(text = if (uiState.shieldEnabled) "Pause sleep mode" else "Start sleep mode", fontWeight = FontWeight.Bold)
                }
            }
        }

        SectionLabel(text = "Apps to protect", trailing = {
            Text(text = "${uiState.blockedPackages.size} selected", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        })

        // App Groups
        categories.forEach { category ->
            val appsInCategory = groupedApps[category] ?: emptyList()
            if (appsInCategory.isNotEmpty()) {
                AppGroup(
                    title = category,
                    apps = appsInCategory,
                    blockedPackages = uiState.blockedPackages,
                    onTogglePackage = { viewModel.togglePackageBlocked(it) },
                    onToggleCategory = { isBlocked ->
                        val currentBlocked = uiState.blockedPackages.toMutableSet()
                        appsInCategory.forEach { app ->
                            if (isBlocked) {
                                currentBlocked.add(app.packageName)
                            } else {
                                currentBlocked.remove(app.packageName)
                            }
                        }
                        viewModel.setBlockedPackages(currentBlocked)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun AppGroup(
    title: String,
    apps: List<AppInfo>,
    blockedPackages: Set<String>,
    onTogglePackage: (String) -> Unit,
    onToggleCategory: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val allBlocked = apps.all { it.packageName in blockedPackages }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${apps.size} apps", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Checkbox(
                    checked = allBlocked,
                    onCheckedChange = { onToggleCategory(it) },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    apps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )
                            Toggle(
                                checked = app.packageName in blockedPackages,
                                onCheckedChange = { onTogglePackage(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}
