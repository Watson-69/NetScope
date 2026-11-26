package com.netscope.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.model.TimeWindow
import com.netscope.app.data.model.TrafficSnapshot
import com.netscope.app.ui.theme.AmberOrange
import com.netscope.app.ui.theme.EmeraldGreen
import com.netscope.app.util.FormatUtils
import kotlin.math.max

@Composable
fun TrafficChart(
    snapshots: List<TrafficSnapshot>,
    selectedTimeWindow: TimeWindow,
    onTimeWindowSelected: (TimeWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter snapshots based on selected time window
    val now = System.currentTimeMillis()
    val filtered = if (selectedTimeWindow.durationSeconds > 0) {
        val windowCutoff = now - (selectedTimeWindow.durationSeconds * 1000L)
        snapshots.filter { it.timestamp >= windowCutoff }
    } else {
        snapshots
    }

    val maxRxSpeed = filtered.maxOfOrNull { it.rxSpeedBytesPerSec } ?: 0L
    val maxTxSpeed = filtered.maxOfOrNull { it.txSpeedBytesPerSec } ?: 0L
    val peakSpeed = max(max(maxRxSpeed, maxTxSpeed), 1024L) // At least 1 KB scale

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Header & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE TRAFFIC ACTIVITY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Peak: ${FormatUtils.formatRate(peakSpeed)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldGreen))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Down", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AmberOrange))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Up", fontSize = 11.sp, color = AmberOrange, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height

                // Draw horizontal grid lines
                for (i in 0..3) {
                    val y = height * (i / 3f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (filtered.size >= 2) {
                    val stepX = width / (filtered.size - 1).toFloat()

                    val rxPath = Path()
                    val txPath = Path()

                    filtered.forEachIndexed { index, snap ->
                        val x = index * stepX
                        val rxRatio = snap.rxSpeedBytesPerSec.toFloat() / peakSpeed.toFloat()
                        val txRatio = snap.txSpeedBytesPerSec.toFloat() / peakSpeed.toFloat()

                        val yRx = height - (rxRatio.coerceIn(0f, 1f) * height)
                        val yTx = height - (txRatio.coerceIn(0f, 1f) * height)

                        if (index == 0) {
                            rxPath.moveTo(x, yRx)
                            txPath.moveTo(x, yTx)
                        } else {
                            rxPath.lineTo(x, yRx)
                            txPath.lineTo(x, yTx)
                        }
                    }

                    // Draw curves
                    drawPath(
                        path = rxPath,
                        color = EmeraldGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = txPath,
                        color = AmberOrange,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    // Empty or waiting state
                    drawLine(
                        color = EmeraldGreen.copy(alpha = 0.5f),
                        start = Offset(0f, height - 2f),
                        end = Offset(width, height - 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Time Window Selector Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Range:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TimeWindow.values().forEach { window ->
                        val isSelected = window == selectedTimeWindow
                        val btnBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        val btnTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(btnBg)
                                .clickable { onTimeWindowSelected(window) }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = window.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = btnTextColor
                            )
                        }
                    }
                }
            }
        }
    }
}
