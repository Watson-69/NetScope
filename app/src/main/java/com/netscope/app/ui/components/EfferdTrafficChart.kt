package com.netscope.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.model.TimeWindow
import com.netscope.app.data.model.TrafficSnapshot
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.MilemarkOrange
import com.netscope.app.util.FormatUtils
import kotlin.math.max

/**
 * Real-time network throughput area chart.
 * Features distinguishable inbound and outbound series, a readable legend,
 * generous 180dp canvas height, and plain-language labels.
 */
@Composable
fun EfferdTrafficChart(
    snapshots: List<TrafficSnapshot>,
    selectedTimeWindow: TimeWindow,
    onTimeWindowSelected: (TimeWindow) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val filtered = if (selectedTimeWindow.durationSeconds > 0) {
        val cutoff = now - (selectedTimeWindow.durationSeconds * 1000L)
        snapshots.filter { it.timestamp >= cutoff }
    } else {
        snapshots
    }

    val maxRx = filtered.maxOfOrNull { it.rxSpeedBytesPerSec } ?: 0L
    val maxTx = filtered.maxOfOrNull { it.txSpeedBytesPerSec } ?: 0L
    val peak = max(max(maxRx, maxTx), 1024L)

    val inboundLineColor = EmeraldAccent
    val outboundLineColor = MilemarkOrange

    EfferdCard(
        modifier = modifier,
        cornerRadius = 12.dp,
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
                        text = "TRAFFIC THROUGHPUT (LIVE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Peak Speed: ${FormatUtils.formatRate(peak)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(inboundLineColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Inbound",
                        fontSize = 12.sp,
                        color = inboundLineColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(outboundLineColor))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Outbound",
                        fontSize = 12.sp,
                        color = outboundLineColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val width = size.width
                val height = size.height

                // Grid lines for reference
                for (i in 0..4) {
                    val y = height * (i / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                }

                if (filtered.size >= 2) {
                    val stepX = width / (filtered.size - 1).toFloat()

                    val rxLinePath = Path()
                    val rxAreaPath = Path()
                    val txLinePath = Path()
                    val txAreaPath = Path()

                    filtered.forEachIndexed { i, snap ->
                        val x = i * stepX
                        val rxRatio = snap.rxSpeedBytesPerSec.toFloat() / peak.toFloat()
                        val txRatio = snap.txSpeedBytesPerSec.toFloat() / peak.toFloat()

                        val yRx = height - (rxRatio.coerceIn(0f, 1f) * height)
                        val yTx = height - (txRatio.coerceIn(0f, 1f) * height)

                        if (i == 0) {
                            rxLinePath.moveTo(x, yRx)
                            rxAreaPath.moveTo(x, height)
                            rxAreaPath.lineTo(x, yRx)

                            txLinePath.moveTo(x, yTx)
                            txAreaPath.moveTo(x, height)
                            txAreaPath.lineTo(x, yTx)
                        } else {
                            rxLinePath.lineTo(x, yRx)
                            rxAreaPath.lineTo(x, yRx)

                            txLinePath.lineTo(x, yTx)
                            txAreaPath.lineTo(x, yTx)
                        }

                        if (i == filtered.size - 1) {
                            rxAreaPath.lineTo(x, height)
                            rxAreaPath.close()

                            txAreaPath.lineTo(x, height)
                            txAreaPath.close()
                        }
                    }

                    // Gradients
                    drawPath(
                        path = rxAreaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(inboundLineColor.copy(alpha = 0.32f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )
                    drawPath(
                        path = txAreaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(outboundLineColor.copy(alpha = 0.24f), Color.Transparent),
                            startY = 0f,
                            endY = height
                        )
                    )

                    // Lines
                    drawPath(
                        path = rxLinePath,
                        color = inboundLineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = txLinePath,
                        color = outboundLineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                } else {
                    drawLine(
                        color = inboundLineColor.copy(alpha = 0.5f),
                        start = Offset(0f, height - 2f),
                        end = Offset(width, height - 2f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Window:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TimeWindow.values().forEach { window ->
                        val isSelected = window == selectedTimeWindow
                        val btnBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        val btnTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(btnBg)
                                .clickable { onTimeWindowSelected(window) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
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
