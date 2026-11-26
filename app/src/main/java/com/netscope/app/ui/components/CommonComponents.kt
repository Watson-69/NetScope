package com.netscope.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.netscope.app.data.model.Direction
import com.netscope.app.data.model.NetworkType
import com.netscope.app.data.model.Protocol
import com.netscope.app.ui.theme.AmberOrange
import com.netscope.app.ui.theme.BlueAccent
import com.netscope.app.ui.theme.CyanAccent
import com.netscope.app.ui.theme.EmeraldGreen
import com.netscope.app.ui.theme.RoseRed
import com.netscope.app.ui.theme.VioletPurple

@Composable
fun StatusBadge(
    isMonitoring: Boolean,
    modifier: Modifier = Modifier
) {
    // Status badge for monitoring state
    val bgColor = if (isMonitoring) EmeraldGreen.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f)
    val dotColor = if (isMonitoring) EmeraldGreen else Color.Gray
    val text = if (isMonitoring) "MONITORING ACTIVE" else "IDLE"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, dotColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = dotColor,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun NetworkTypeBadge(
    networkType: NetworkType,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (networkType) {
        NetworkType.WIFI -> CyanAccent to "Wi-Fi"
        NetworkType.CELLULAR -> AmberOrange to "Cellular"
        NetworkType.ETHERNET -> BlueAccent to "Ethernet"
        NetworkType.UNKNOWN -> Color.Gray to "Unknown"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun ProtocolBadge(
    protocol: Protocol,
    modifier: Modifier = Modifier
) {
    val color = when (protocol) {
        Protocol.TCP -> BlueAccent
        Protocol.UDP -> CyanAccent
        Protocol.DNS -> VioletPurple
        Protocol.HTTPS -> EmeraldGreen
        Protocol.HTTP -> AmberOrange
        Protocol.ICMP -> RoseRed
        Protocol.OTHER -> Color.Gray
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = protocol.displayName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun DirectionBadge(
    direction: Direction,
    modifier: Modifier = Modifier
) {
    val (color, symbol, label) = when (direction) {
        Direction.INBOUND -> Triple(EmeraldGreen, "↓", "IN")
        Direction.OUTBOUND -> Triple(AmberOrange, "↑", "OUT")
        Direction.INTERNAL -> Triple(BlueAccent, "↔", "INT")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
