package com.netscope.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.Zinc800

/**
 * Activity Heatmap widget inspired by Efferd's SalesHeatmap.
 * Renders a visual matrix grid representing network throughput activity
 * density across recent intervals.
 */
@Composable
fun NetworkActivityHeatmap(
    activeConnectionsCount: Int,
    isMonitoring: Boolean,
    modifier: Modifier = Modifier
) {
    EfferdCard(
        modifier = modifier,
        cornerRadius = 12.dp,
        contentPadding = 14.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRAFFIC DENSITY HEATMAP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.6.sp
                )

                Text(
                    text = if (isMonitoring) "Live Telemetry" else "Idle",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMonitoring) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7 Columns x 4 Rows Activity Matrix
            val columns = 7
            val rows = 4

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (c in 0 until columns) {
                            val seed = (r * columns + c)
                            val intensity = if (!isMonitoring) 0 else ((seed + activeConnectionsCount) % 5)
                            val blockColor = when (intensity) {
                                1 -> EmeraldAccent.copy(alpha = 0.20f)
                                2 -> EmeraldAccent.copy(alpha = 0.45f)
                                3 -> EmeraldAccent.copy(alpha = 0.70f)
                                4 -> EmeraldAccent
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(blockColor)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Intensity Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Less Active",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        EmeraldAccent.copy(alpha = 0.20f),
                        EmeraldAccent.copy(alpha = 0.45f),
                        EmeraldAccent.copy(alpha = 0.70f),
                        EmeraldAccent
                    ).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                }

                Text(
                    text = "Peak Traffic",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
