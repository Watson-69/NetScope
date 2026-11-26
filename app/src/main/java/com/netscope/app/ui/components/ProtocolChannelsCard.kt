package com.netscope.app.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.model.ConnectionRecord
import com.netscope.app.data.model.Protocol
import com.netscope.app.ui.theme.AmberAccent
import com.netscope.app.ui.theme.CyanAccent
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.IndigoAccent

/**
 * Protocol Channels Card inspired by Efferd's OrdersByChannel / Traffic Sources widget.
 * Shows distribution of observed connection flows by protocol channel.
 */
@Composable
fun ProtocolChannelsCard(
    connections: List<ConnectionRecord>,
    modifier: Modifier = Modifier
) {
    val total = connections.size.coerceAtLeast(1)
    val httpsCount = connections.count { it.protocol == Protocol.HTTPS }
    val dnsCount = connections.count { it.protocol == Protocol.DNS }
    val tcpCount = connections.count { it.protocol == Protocol.TCP || it.protocol == Protocol.HTTP }
    val udpCount = connections.count { it.protocol == Protocol.UDP || it.protocol == Protocol.ICMP || it.protocol == Protocol.OTHER }

    val httpsPct = (httpsCount.toFloat() / total * 100f).coerceAtLeast(if (connections.isEmpty()) 65f else 0f)
    val dnsPct = (dnsCount.toFloat() / total * 100f).coerceAtLeast(if (connections.isEmpty()) 15f else 0f)
    val tcpPct = (tcpCount.toFloat() / total * 100f).coerceAtLeast(if (connections.isEmpty()) 12f else 0f)
    val udpPct = (udpCount.toFloat() / total * 100f).coerceAtLeast(if (connections.isEmpty()) 8f else 0f)

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
                    text = "TRAFFIC SOURCES BY CHANNEL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.6.sp
                )

                Text(
                    text = "${connections.size} Flows",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Multi-segment Horizontal Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                Box(modifier = Modifier.weight(httpsPct.coerceAtLeast(1f)).height(6.dp).background(EmeraldAccent))
                Box(modifier = Modifier.weight(dnsPct.coerceAtLeast(1f)).height(6.dp).background(CyanAccent))
                Box(modifier = Modifier.weight(tcpPct.coerceAtLeast(1f)).height(6.dp).background(IndigoAccent))
                Box(modifier = Modifier.weight(udpPct.coerceAtLeast(1f)).height(6.dp).background(AmberAccent))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Rows
            ChannelRowItem("Secure HTTPS / TLS", EmeraldAccent, "${httpsPct.toInt()}%", "$httpsCount flows")
            Spacer(modifier = Modifier.height(6.dp))
            ChannelRowItem("DNS Queries", CyanAccent, "${dnsPct.toInt()}%", "$dnsCount flows")
            Spacer(modifier = Modifier.height(6.dp))
            ChannelRowItem("TCP Transport", IndigoAccent, "${tcpPct.toInt()}%", "$tcpCount flows")
            Spacer(modifier = Modifier.height(6.dp))
            ChannelRowItem("UDP / ICMP / Other", AmberAccent, "${udpPct.toInt()}%", "$udpCount flows")
        }
    }
}

@Composable
private fun ChannelRowItem(name: String, color: Color, percentage: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = count, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = percentage, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
