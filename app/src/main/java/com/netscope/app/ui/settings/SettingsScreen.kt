package com.netscope.app.ui.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.model.MonitoringMode
import com.netscope.app.data.repository.SettingsRepository
import com.netscope.app.data.repository.TrafficRepository
import com.netscope.app.ui.components.EfferdCard
import com.netscope.app.ui.components.ThemeToggleSelector
import com.netscope.app.ui.theme.AppThemeMode
import com.netscope.app.ui.theme.CrimsonRed
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.util.NetworkStateHelper

@Composable
fun SettingsScreen(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onShowDisclosureAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository.getInstance(context) }
    val trafficRepo = TrafficRepository.getInstance()

    val currentMode by trafficRepo.monitoringState.collectAsState()
    val retentionLimit by trafficRepo.retentionLimit.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var historyClearedMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
    // Section: Theme Toggle Switcher (Black / White / Red)
    // Theme configuration section
    SettingsSectionHeader(title = "PALETTE CONFIGURATION (BLACK / WHITE / RED)")

        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 14.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Milemark Freight Palette",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Active: ${currentTheme.displayName} Palette",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    ThemeToggleSelector(
                        currentTheme = currentTheme,
                        onSelectTheme = onSelectTheme
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = when (currentTheme) {
                        AppThemeMode.BLACK -> "• Black (Obsidian Carbon): Deep ink canvas (#0A0B09) with freight orange telemetry."
                        AppThemeMode.WHITE -> "• White (Paper Freight): Authentic Milemark logistics paper canvas (#F5F5F1) with crisp slate."
                        AppThemeMode.RED -> "• Red (Crimson Telemetry): High-contrast alert dispatch mode with crimson glow."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Privacy & Consent
        SettingsSectionHeader(title = "PRIVACY & ARCHITECTURE")

        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 14.dp
        ) {
            Column {
                Text(
                    text = "NetScope Privacy Architecture",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• 100% On-Device Processing: No traffic, metrics, or telemetry are ever sent to remote servers.\n• Connection Metadata Only: Packet payloads, message bodies, and passwords are never read or stored.\n• Transparent Consent: Operates strictly with explicit user knowledge.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onShowDisclosureAgain,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Review Disclosure & Consent Terms", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Monitoring Engine Selection
        SettingsSectionHeader(title = "MONITORING ENGINE")

        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 12.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MonitoringMode.values().forEach { mode ->
                    val isSelected = currentMode.mode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                trafficRepo.setMonitoringMode(mode)
                                settingsRepo.setPreferredMode(mode)
                            },
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                trafficRepo.setMonitoringMode(mode)
                                settingsRepo.setPreferredMode(mode)
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = mode.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mode.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Data Retention & History Purge
        SettingsSectionHeader(title = "DATA RETENTION & HISTORY")

        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 14.dp
        ) {
            Column {
                Text(
                    text = "Connection History Limit",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Maximum metadata rows kept in memory before rolling over.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(100, 500, 1000).forEach { limit ->
                        val isSelected = retentionLimit == limit
                        val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        val text = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bg)
                                .clickable {
                                    trafficRepo.setRetentionLimit(limit)
                                    settingsRepo.setRetentionLimit(limit)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "$limit records",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = text
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showClearConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonRed.copy(alpha = 0.15f),
                        contentColor = CrimsonRed
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "🗑️ Clear All Traffic History Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (historyClearedMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = historyClearedMessage!!,
                        fontSize = 11.sp,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Device & Platform Diagnostics
        SettingsSectionHeader(title = "PLATFORM DIAGNOSTICS")

        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 14.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DiagnosticItem("OS Version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                DiagnosticItem("Device", "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}")
                DiagnosticItem("Active Network", NetworkStateHelper.getCurrentNetworkType(context).displayName)
                DiagnosticItem("Usage Access", if (NetworkStateHelper.hasUsageStatsPermission(context)) "Granted" else "Not Granted")
                DiagnosticItem("App Edition", "NetScope Milemark Final (Multi-Theme)")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(text = "Clear All Traffic History?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "This will permanently purge all captured connection metadata rows, reset session totals, and clear chart history.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        trafficRepo.clearHistory()
                        showClearConfirmDialog = false
                        historyClearedMessage = "History successfully cleared at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text(text = "Yes, Clear Everything", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearConfirmDialog = false }) {
                    Text(text = "Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DiagnosticItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}
