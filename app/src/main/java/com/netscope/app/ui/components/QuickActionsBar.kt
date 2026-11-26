package com.netscope.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.RoseAccent

/**
 * Quick Actions Bar for Overview.
 * Provides Start/Pause controls, Clear history (with confirmation), and Live Feed navigation.
 */
@Composable
fun QuickActionsBar(
    isMonitoring: Boolean,
    onToggleMonitoring: () -> Unit,
    onClearHistory: () -> Unit,
    onNavigateConnections: () -> Unit,
    modifier: Modifier = Modifier
) {
    EfferdCard(
        modifier = modifier,
        cornerRadius = 12.dp,
        contentPadding = 12.dp
    ) {
        Column {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionItem(
                    label = if (isMonitoring) "Pause" else "Start",
                    icon = if (isMonitoring) "⏸" else "▶",
                    highlight = true,
                    isPositive = !isMonitoring,
                    onClick = onToggleMonitoring,
                    modifier = Modifier.weight(1f)
                )

                QuickActionItem(
                    label = "Clear history",
                    icon = "🗑",
                    highlight = false,
                    isPositive = false,
                    onClick = onClearHistory,
                    modifier = Modifier.weight(1.1f)
                )

                QuickActionItem(
                    label = "Live Feed",
                    icon = "⚡",
                    highlight = false,
                    isPositive = true,
                    onClick = onNavigateConnections,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun QuickActionItem(
    label: String,
    icon: String,
    highlight: Boolean,
    isPositive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (highlight) {
        if (isPositive) EmeraldAccent.copy(alpha = 0.16f) else RoseAccent.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val textCol = if (highlight) {
        if (isPositive) EmeraldAccent else RoseAccent
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .defaultMinSize(minHeight = 42.dp)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textCol,
                maxLines = 1
            )
        }
    }
}
