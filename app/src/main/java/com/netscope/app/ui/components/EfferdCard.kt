package com.netscope.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Signature Efferd Dashboard Card with subtle precision '+' crosshairs
 * at each corner, matching the developer-dashboard design of dashboard-9.
 * 
 * Used across Overview and Settings screens.
@Composable
fun EfferdCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    showCrosshairs: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
    ) {
        if (showCrosshairs) {
            val crossColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val crossSize = 7.dp.toPx()
                val offset = 7.dp.toPx()
                val w = size.width
                val h = size.height

                // Top-Left Crosshair (+)
                drawLine(crossColor, Offset(offset - crossSize / 2, offset), Offset(offset + crossSize / 2, offset), strokeWidth = 1.2f)
                drawLine(crossColor, Offset(offset, offset - crossSize / 2), Offset(offset, offset + crossSize / 2), strokeWidth = 1.2f)

                // Top-Right Crosshair (+)
                drawLine(crossColor, Offset(w - offset - crossSize / 2, offset), Offset(w - offset + crossSize / 2, offset), strokeWidth = 1.2f)
                drawLine(crossColor, Offset(w - offset, offset - crossSize / 2), Offset(w - offset, offset + crossSize / 2), strokeWidth = 1.2f)

                // Bottom-Left Crosshair (+)
                drawLine(crossColor, Offset(offset - crossSize / 2, h - offset), Offset(offset + crossSize / 2, h - offset), strokeWidth = 1.2f)
                drawLine(crossColor, Offset(offset, h - offset - crossSize / 2), Offset(offset, h - offset + crossSize / 2), strokeWidth = 1.2f)

                // Bottom-Right Crosshair (+)
                drawLine(crossColor, Offset(w - offset - crossSize / 2, h - offset), Offset(w - offset + crossSize / 2, h - offset), strokeWidth = 1.2f)
                drawLine(crossColor, Offset(w - offset, h - offset - crossSize / 2), Offset(w - offset, h - offset + crossSize / 2), strokeWidth = 1.2f)
            }
        }

        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
