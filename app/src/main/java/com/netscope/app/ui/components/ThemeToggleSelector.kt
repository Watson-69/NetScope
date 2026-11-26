package com.netscope.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.ui.theme.AppThemeMode

/**
 * 3-Way Theme Toggle Switcher for Black, White, and Red themes.
 * Supports a compact icon-only mode for top bars and a full labeled mode for settings.
 */
@Composable
fun ThemeToggleSelector(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (compact) {
        // Compact mode for TopAppBar: minimal footprint to prevent crushing titles
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppThemeMode.values().forEach { mode ->
                val isSelected = mode == currentTheme
                val swatchColor = when (mode) {
                    AppThemeMode.BLACK -> Color(0xFF18181B)
                    AppThemeMode.WHITE -> Color(0xFFFAFAFA)
                    AppThemeMode.RED -> Color(0xFFEF4444)
                }
                val borderSwatch = when (mode) {
                    AppThemeMode.BLACK -> Color(0xFF52525B)
                    AppThemeMode.WHITE -> Color(0xFF94A3B8)
                    AppThemeMode.RED -> Color(0xFFB91C1C)
                }

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onSelectTheme(mode) }
                        .then(
                            if (isSelected) {
                                Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(0.8.dp, borderSwatch, CircleShape)
                    )
                }
            }
        }
    } else {
        // Full labeled mode for SettingsScreen
        Row(
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AppThemeMode.values().forEach { mode ->
                val isSelected = mode == currentTheme

                val (swatchColor, borderSwatch) = when (mode) {
                    AppThemeMode.BLACK -> Color(0xFF0A0B09) to Color(0xFF3F3F46)
                    AppThemeMode.WHITE -> Color(0xFFF5F5F1) to Color(0xFFCBD5E1)
                    AppThemeMode.RED -> Color(0xFFEF4444) to Color(0xFFFDA4AF)
                }

                val itemBg = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                val itemBorder = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                val textColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(itemBg)
                        .border(if (isSelected) 1.dp else 0.dp, itemBorder, RoundedCornerShape(6.dp))
                        .clickable { onSelectTheme(mode) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(0.8.dp, borderSwatch, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.displayName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}
