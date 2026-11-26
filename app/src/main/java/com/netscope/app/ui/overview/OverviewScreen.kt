package com.netscope.app.ui.overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.repository.TrafficRepository
import com.netscope.app.ui.components.EfferdCard
import com.netscope.app.ui.components.EfferdKpiCard
import com.netscope.app.ui.components.EfferdTrafficChart
import com.netscope.app.ui.components.NetworkActivityHeatmap
import com.netscope.app.ui.components.NetworkTypeBadge
import com.netscope.app.ui.components.ProtocolChannelsCard
import com.netscope.app.ui.components.QuickActionsBar
import com.netscope.app.ui.components.StatusBadge
import com.netscope.app.ui.theme.CyanAccent
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.MilemarkOrange
import com.netscope.app.ui.theme.RoseAccent
import com.netscope.app.util.FormatUtils
import com.netscope.app.util.RateUnit

@Composable
fun OverviewScreen(
    onToggleMonitoring: () -> Unit,
    onNavigateConnections: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val repository = TrafficRepository.getInstance()
    val monitoringState by repository.monitoringState.collectAsState()
    val snapshot by repository.currentSnapshot.collectAsState()
    val history by repository.snapshotHistory.collectAsState()
    val connections by repository.connections.collectAsState()
    val timeWindow by repository.selectedTimeWindow.collectAsState()

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Interactive Rate Unit States: user can tap to cycle KB/s -> MB/s -> GB/s -> Auto
    var downloadUnit by remember { mutableStateOf(RateUnit.KB_S) }
    var uploadUnit by remember { mutableStateOf(RateUnit.KB_S) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 1. Simplified Status & Diagnostics Header
        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 14.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Diagnostics Overview 👋",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (monitoringState.isMonitoring) {
                            "Session duration: ${FormatUtils.formatDuration(monitoringState.sessionDurationMs)}"
                        } else {
                            "System idle • Tap Start to monitor telemetry"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(isMonitoring = monitoringState.isMonitoring)
                    Spacer(modifier = Modifier.width(6.dp))
                    NetworkTypeBadge(networkType = snapshot.networkType)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Quick Actions Strip with Clear history
        QuickActionsBar(
            isMonitoring = monitoringState.isMonitoring,
            onToggleMonitoring = onToggleMonitoring,
            onClearHistory = { showClearConfirmDialog = true },
            onNavigateConnections = onNavigateConnections,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Hero Transfer Rates Card: Interactive Unit Toggle (KB/s ➔ MB/s ➔ GB/s) with slide animations
        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            contentPadding = 16.dp
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LIVE TRANSFER RATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap rates to cycle KB/s ➔ MB/s ➔ GB/s",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (monitoringState.isMonitoring) EmeraldAccent else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (monitoringState.isMonitoring) "Live Stream" else "Paused",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (monitoringState.isMonitoring) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download Column (Clickable to switch units with flip animation)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { downloadUnit = downloadUnit.next() }
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldAccent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↓",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DOWNLOAD",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldAccent
                                )
                            }

                            // Interactive Unit Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldAccent.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${downloadUnit.label} ⟳",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Animated Rate Figure
                        AnimatedContent(
                            targetState = downloadUnit,
                            transitionSpec = {
                                (slideInVertically(animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f)) { height -> height } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(slideOutVertically(animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f)) { height -> -height } + fadeOut(animationSpec = tween(220)))
                            },
                            label = "DownloadRateAnimation"
                        ) { targetUnit ->
                            Text(
                                text = FormatUtils.formatRateWithUnit(snapshot.rxSpeedBytesPerSec, targetUnit),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Session: ${FormatUtils.formatBytes(snapshot.sessionRxTotalBytes)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(68.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Upload Column (Clickable to switch units with flip animation)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { uploadUnit = uploadUnit.next() }
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(MilemarkOrange.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "↑",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MilemarkOrange
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "UPLOAD",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MilemarkOrange
                                )
                            }

                            // Interactive Unit Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MilemarkOrange.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${uploadUnit.label} ⟳",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MilemarkOrange
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Animated Rate Figure
                        AnimatedContent(
                            targetState = uploadUnit,
                            transitionSpec = {
                                (slideInVertically(animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f)) { height -> height } + fadeIn(animationSpec = tween(220)))
                                    .togetherWith(slideOutVertically(animationSpec = spring(dampingRatio = 0.7f, stiffness = 450f)) { height -> -height } + fadeOut(animationSpec = tween(220)))
                            },
                            label = "UploadRateAnimation"
                        ) { targetUnit ->
                            Text(
                                text = FormatUtils.formatRateWithUnit(snapshot.txSpeedBytesPerSec, targetUnit),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Session: ${FormatUtils.formatBytes(snapshot.sessionTxTotalBytes)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Secondary Plain-Language KPI Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EfferdKpiCard(
                title = "Active Connections",
                primaryValue = snapshot.activeConnectionsCount.toString(),
                subtitle = "Live TCP/UDP network flows",
                secondaryInfo = "${snapshot.totalPacketsRecorded} packets",
                accentColor = CyanAccent,
                iconSymbol = "⚡",
                modifier = Modifier.weight(1f)
            )

            EfferdKpiCard(
                title = "Device Total Rx",
                primaryValue = FormatUtils.formatBytes(snapshot.deviceRxTotalBytes),
                subtitle = "Total received since reboot",
                secondaryInfo = snapshot.networkType.displayName,
                accentColor = EmeraldAccent,
                iconSymbol = "📶",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Throughput Dynamics Trend Area Chart (180dp height, clear legend, plain labels)
        EfferdTrafficChart(
            snapshots = history,
            selectedTimeWindow = timeWindow,
            onTimeWindowSelected = { repository.setSelectedTimeWindow(it) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Traffic Sources by Channel (TCP vs UDP)
        ProtocolChannelsCard(
            connections = connections,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Network Activity Matrix
        NetworkActivityHeatmap(
            activeConnectionsCount = snapshot.activeConnectionsCount,
            isMonitoring = monitoringState.isMonitoring,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 8. Privacy Guarantee Card
        EfferdCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 12.dp,
            contentPadding = 14.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛡️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "100% Local On-Device Telemetry",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Zero payloads or private messages are captured, stored, or transmitted. All throughput rates and connection metadata are computed strictly on-device.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Confirmation Dialog before Clearing Stored History
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Traffic History?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear the recorded session history and connection logs from this device? Real-time network telemetry will continue uninterrupted.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.clearHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoseAccent,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear History", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
