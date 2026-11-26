package com.netscope.app.ui.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.model.ConnectionRecord
import com.netscope.app.data.model.Direction
import com.netscope.app.data.model.Protocol
import com.netscope.app.data.repository.TrafficRepository
import com.netscope.app.ui.components.DirectionBadge
import com.netscope.app.ui.components.EfferdCard
import com.netscope.app.ui.components.ProtocolBadge
import com.netscope.app.util.FormatUtils

@Composable
fun ConnectionsScreen(
    modifier: Modifier = Modifier
) {
    val repository = TrafficRepository.getInstance()
    val connections by repository.connections.collectAsState()
    val monitoringState by repository.monitoringState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedProtocol by remember { mutableStateOf<Protocol?>(null) }
    var selectedDirection by remember { mutableStateOf<Direction?>(null) }

    val filtered = connections.filter { record ->
        val matchesSearch = searchQuery.isBlank() ||
                record.sourceIp.contains(searchQuery, ignoreCase = true) ||
                record.destinationIp.contains(searchQuery, ignoreCase = true) ||
                record.sourcePort.toString().contains(searchQuery) ||
                record.destinationPort.toString().contains(searchQuery) ||
                (record.appName?.contains(searchQuery, ignoreCase = true) == true)

        val matchesProtocol = selectedProtocol == null || record.protocol == selectedProtocol
        val matchesDirection = selectedDirection == null || record.direction == selectedDirection

        matchesSearch && matchesProtocol && matchesDirection
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Search Input (Efferd style)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by IP, port, or protocol...", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedIndicatorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Protocol Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterPillItem(
                    label = "All Protocols",
                    isSelected = selectedProtocol == null,
                    onClick = { selectedProtocol = null }
                )
            }
            items(Protocol.values()) { proto ->
                FilterPillItem(
                    label = proto.displayName,
                    isSelected = selectedProtocol == proto,
                    onClick = {
                        selectedProtocol = if (selectedProtocol == proto) null else proto
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Direction Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterPillItem(
                    label = "All Directions",
                    isSelected = selectedDirection == null,
                    onClick = { selectedDirection = null }
                )
            }
            items(Direction.values()) { dir ->
                FilterPillItem(
                    label = dir.displayName,
                    isSelected = selectedDirection == dir,
                    onClick = {
                        selectedDirection = if (selectedDirection == dir) null else dir
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Table Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "OBSERVED METADATA FLOWS (${filtered.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )

            Text(
                text = "Zero Payloads Captured",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            EmptyConnectionsState(
                isMonitoring = monitoringState.isMonitoring,
                hasFilter = searchQuery.isNotBlank() || selectedProtocol != null || selectedDirection != null
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { record ->
                    ConnectionEfferdRow(record = record)
                }
            }
        }
    }
}

@Composable
private fun FilterPillItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val text = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = text
        )
    }
}

@Composable
private fun ConnectionEfferdRow(record: ConnectionRecord) {
    EfferdCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp,
        contentPadding = 12.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DirectionBadge(direction = record.direction)
                    Spacer(modifier = Modifier.width(6.dp))
                    ProtocolBadge(protocol = record.protocol)
                }

                Text(
                    text = FormatUtils.formatTimeWithMillis(record.timestamp),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Endpoints: Source -> Destination with monospace styling
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.sourceIp}:${record.sourcePort}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = " → ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${record.destinationIp}:${record.destinationPort}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.appName ?: "Socket Metadata",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Size: ${FormatUtils.formatBytes(record.packetSize)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyConnectionsState(
    isMonitoring: Boolean,
    hasFilter: Boolean
) {
    EfferdCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        cornerRadius = 12.dp,
        contentPadding = 24.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📡", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = when {
                    hasFilter -> "No telemetry flows match active filter"
                    !isMonitoring -> "Monitoring is currently paused"
                    else -> "Listening for active connection metadata..."
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    hasFilter -> "Try clearing IP or protocol filters."
                    !isMonitoring -> "Start monitoring on the Overview screen to inspect packet headers."
                    else -> "Socket connection flows will appear here in real time."
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
