package com.netscope.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.netscope.app.R
import com.netscope.app.data.model.MonitoringMode
import com.netscope.app.data.repository.SettingsRepository
import com.netscope.app.data.repository.TrafficRepository
import com.netscope.app.service.NetScopeVpnService
import com.netscope.app.service.TrafficMonitoringService
import com.netscope.app.ui.apps.AppsScreen
import com.netscope.app.ui.components.DisclosureDialog
import com.netscope.app.ui.components.PreloaderScreen
import com.netscope.app.ui.components.ThemeToggleSelector
import com.netscope.app.ui.connections.ConnectionsScreen
import com.netscope.app.ui.overview.OverviewScreen
import com.netscope.app.ui.settings.SettingsScreen
import com.netscope.app.ui.theme.AppThemeMode
import com.netscope.app.ui.theme.EmeraldAccent
import com.netscope.app.ui.theme.MilemarkOrange
import com.netscope.app.ui.theme.NetScopeTheme

class MainActivity : ComponentActivity() {
    // Main activity entry point

    private lateinit var settingsRepository: SettingsRepository
    private val trafficRepository = TrafficRepository.getInstance()

    // VPN Consent result launcher
    private val vpnConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            trafficRepository.setVpnPermissionGranted(true)
            NetScopeVpnService.startService(this)
            Toast.makeText(this, "NetScope VPN Inspector started", Toast.LENGTH_SHORT).show()
        } else {
            trafficRepository.setVpnPermissionGranted(false)
            Toast.makeText(this, "VPN permission required for packet metadata mode", Toast.LENGTH_LONG).show()
        }
    }

    // Notification permission launcher for Android 13+
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository.getInstance(this)

        requestNotificationPermissionIfNeeded()

        setContent {
            val themeMode by settingsRepository.themeMode.collectAsState()
            var isPreloading by remember { mutableStateOf(true) }

            NetScopeTheme(themeMode = themeMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (isPreloading) {
                        PreloaderScreen(
                            onPreloadComplete = { isPreloading = false }
                        )
                    } else {
                        AnimatedVisibility(
                            visible = !isPreloading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            MainAppScaffold(
                                currentTheme = themeMode,
                                onSelectTheme = { settingsRepository.setThemeMode(it) },
                                onToggleMonitoring = { toggleMonitoring() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun toggleMonitoring() {
        val state = trafficRepository.monitoringState.value
        if (state.isMonitoring) {
            TrafficMonitoringService.stopService(this)
            NetScopeVpnService.stopService(this)
            trafficRepository.setMonitoringActive(false)
            Toast.makeText(this, "Monitoring Stopped", Toast.LENGTH_SHORT).show()
        } else {
            if (state.mode == MonitoringMode.VPN_HEADER_INSPECTOR) {
                prepareAndStartVpn()
            } else {
                TrafficMonitoringService.startService(this)
                Toast.makeText(this, "Monitoring Active", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareAndStartVpn() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            vpnConsentLauncher.launch(vpnIntent)
        } else {
            trafficRepository.setVpnPermissionGranted(true)
            NetScopeVpnService.startService(this)
            Toast.makeText(this, "NetScope VPN Inspector started", Toast.LENGTH_SHORT).show()
        }
    }
}

enum class NavigationTab(val label: String, val icon: ImageVector) {
    OVERVIEW("Overview", Icons.Default.Assessment),
    CONNECTIONS("Flows", Icons.Default.Cable),
    APPS("Cargo Apps", Icons.Default.Apps),
    SETTINGS("Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onToggleMonitoring: () -> Unit
) {
    val repository = TrafficRepository.getInstance()
    val settings = SettingsRepository.getInstance(androidx.compose.ui.platform.LocalContext.current)
    val monitoringState by repository.monitoringState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDisclosureDialog by remember { mutableStateOf(!monitoringState.disclosureAcknowledged) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_netscope_mark),
                            contentDescription = "NetScope Mark",
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "netscope",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                letterSpacing = (-0.6).sp,
                                maxLines = 1,
                                softWrap = false,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = ".",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = MilemarkOrange
                            )
                        }
                    }
                },
                actions = {
                    // Compact 3-Way Theme Toggle: Black / White / Red
                    ThemeToggleSelector(
                        currentTheme = currentTheme,
                        onSelectTheme = onSelectTheme,
                        compact = true,
                        modifier = Modifier.padding(end = 6.dp)
                    )

                    // Minimal compact status indicator pill
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (monitoringState.isMonitoring) EmeraldAccent.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (monitoringState.isMonitoring) EmeraldAccent else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (monitoringState.isMonitoring) "ACTIVE" else "IDLE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monitoringState.isMonitoring) EmeraldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> OverviewScreen(
                    onToggleMonitoring = {
                        if (!monitoringState.disclosureAcknowledged) {
                            showDisclosureDialog = true
                        } else {
                            onToggleMonitoring()
                        }
                    },
                    onNavigateConnections = { selectedTab = 1 }
                )
                1 -> ConnectionsScreen()
                2 -> AppsScreen()
                3 -> SettingsScreen(
                    currentTheme = currentTheme,
                    onSelectTheme = onSelectTheme,
                    onShowDisclosureAgain = { showDisclosureDialog = true }
                )
            }

            if (showDisclosureDialog) {
                DisclosureDialog(
                    onConfirmConsent = {
                        repository.setDisclosureAcknowledged(true)
                        settings.setHasSeenDisclosure(true)
                        showDisclosureDialog = false
                        onToggleMonitoring()
                    },
                    onDismiss = {
                        showDisclosureDialog = false
                    }
                )
            }
        }
    }
}
