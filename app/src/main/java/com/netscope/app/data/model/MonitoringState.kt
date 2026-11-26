package com.netscope.app.data.model

enum class TimeWindow(val label: String, val durationSeconds: Int) {
    LAST_30_SECONDS("30s", 30),
    LAST_1_MINUTE("1m", 60),
    LAST_5_MINUTES("5m", 300),
    LAST_15_MINUTES("15m", 900),
    ALL_SESSION("All", -1)
}

enum class MonitoringMode(val title: String, val description: String) {
    STANDARD_METRICS(
        title = "Device Traffic Mode (Standard)",
        description = "Uses Android TrafficStats & NetworkCapabilities. Observes global network rates, session totals, and per-app usage without routing or packet interception."
    ),
    VPN_HEADER_INSPECTOR(
        title = "Connection Metadata Mode (VpnService)",
        description = "Uses Android VpnService to observe connection metadata (protocol, IP, port, size). 100% processed locally on-device. No payloads or data content are ever captured, inspected, or transmitted."
    )
}

data class MonitoringState(
    val isMonitoring: Boolean = false,
    val mode: MonitoringMode = MonitoringMode.STANDARD_METRICS,
    val sessionStartTime: Long = 0L,
    val sessionDurationMs: Long = 0L,
    val disclosureAcknowledged: Boolean = false,
    val vpnPermissionGranted: Boolean = false,
    val usageAccessGranted: Boolean = false
)
