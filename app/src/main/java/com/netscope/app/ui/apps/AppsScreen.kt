package com.netscope.app.ui.apps

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.netscope.app.data.manager.AppTrafficManager
import com.netscope.app.data.model.AppTrafficInfo
import com.netscope.app.data.repository.TrafficRepository
import com.netscope.app.ui.components.EfferdCard
import com.netscope.app.ui.theme.CrimsonRed
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.MilemarkOrange
import com.netscope.app.util.FormatUtils
import com.netscope.app.util.NetworkStateHelper
import kotlinx.coroutines.launch

enum class AppSortOption(val label: String) {
    TOTAL("Total Data"),
    DOWNLOAD("Inbound ↓"),
    UPLOAD("Outbound ↑"),
    NAME("App Name")
}

@Composable
fun AppsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = TrafficRepository.getInstance()
    val apps by repository.appTraffic.collectAsState()
    val hasUsageAccess = remember { NetworkStateHelper.hasUsageStatsPermission(context) }
    val appTrafficManager = remember { AppTrafficManager(context) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(apps.isEmpty()) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(AppSortOption.TOTAL) }

    // Actively load app traffic list immediately on entry
    LaunchedEffect(hasUsageAccess) {
        isLoading = true
        val list = appTrafficManager.getAppTrafficList()
        repository.updateAppTraffic(list)
        isLoading = false
    }

    val filtered = apps.filter {
        searchQuery.isBlank() ||
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
    }

    val sortedApps = when (sortOption) {
        AppSortOption.TOTAL -> filtered.sortedByDescending { it.totalBytes }
        AppSortOption.DOWNLOAD -> filtered.sortedByDescending { it.rxBytes }
        AppSortOption.UPLOAD -> filtered.sortedByDescending { it.txBytes }
        AppSortOption.NAME -> filtered.sortedBy { it.appName.lowercase() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Platform Permissions / Limitations Notice Card
        if (!hasUsageAccess) {
            EfferdCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                contentPadding = 14.dp
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Platform Usage Access Recommended",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MilemarkOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Android requires 'Usage Access' permission to attribute historical network data accurately per app. Without it, live TrafficStats since device boot are shown.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            } catch (ignored: Exception) {}
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MilemarkOrange,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Grant Usage Access in System Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search installed apps...", fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val list = appTrafficManager.getAppTrafficList()
                            repository.updateAppTraffic(list)
                            isLoading = false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
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

        // Sorting options strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CARGO APPS (${sortedApps.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AppSortOption.values()) { opt ->
                    val isSelected = opt == sortOption
                    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    val text = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable { sortOption = opt }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = opt.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = text
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Scanning application data pipelines...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (sortedApps.isEmpty()) {
            EmptyAppsState(searchQuery.isNotBlank())
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedApps, key = { "${it.uid}_${it.packageName}" }) { appInfo ->
                    AppTrafficEfferdRow(appInfo = appInfo)
                }
            }
        }
    }
}

@Composable
private fun AppTrafficEfferdRow(appInfo: AppTrafficInfo) {
    EfferdCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 10.dp,
        contentPadding = 12.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Monogram avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = appInfo.appName.take(1).uppercase(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appInfo.appName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${appInfo.packageName} • UID ${appInfo.uid}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FormatUtils.formatBytes(appInfo.totalBytes),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (appInfo.percentageOfTotal > 0) String.format("%.1f%%", appInfo.percentageOfTotal) else "Active",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Percentage Progress Bar
            LinearProgressIndicator(
                progress = (appInfo.percentageOfTotal / 100f).coerceIn(0.02f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "↓ Inbound: ", fontSize = 11.sp, color = EmeraldAccent, fontWeight = FontWeight.Bold)
                    Text(
                        text = FormatUtils.formatBytes(appInfo.rxBytes),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "↑ Outbound: ", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
                    Text(
                        text = FormatUtils.formatBytes(appInfo.txBytes),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAppsState(isFiltering: Boolean) {
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
            Text(text = "📦", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isFiltering) "No apps match your search" else "No active application traffic detected",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isFiltering) "Try clearing the search query." else "Apps that transmit or receive data will appear here.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
