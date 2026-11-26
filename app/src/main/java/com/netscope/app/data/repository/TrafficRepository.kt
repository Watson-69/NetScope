package com.netscope.app.data.repository

import com.netscope.app.data.model.AppTrafficInfo
import com.netscope.app.data.model.ConnectionRecord
import com.netscope.app.data.model.Direction
import com.netscope.app.data.model.MonitoringMode
import com.netscope.app.data.model.MonitoringState
import com.netscope.app.data.model.NetworkType
import com.netscope.app.data.model.Protocol
import com.netscope.app.data.model.TimeWindow
import com.netscope.app.data.model.TrafficSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TrafficRepository private constructor() {

    private val _monitoringState = MutableStateFlow(MonitoringState())
    val monitoringState: StateFlow<MonitoringState> = _monitoringState.asStateFlow()

    private val _currentSnapshot = MutableStateFlow(TrafficSnapshot())
    val currentSnapshot: StateFlow<TrafficSnapshot> = _currentSnapshot.asStateFlow()

    private val _snapshotHistory = MutableStateFlow<List<TrafficSnapshot>>(emptyList())
    val snapshotHistory: StateFlow<List<TrafficSnapshot>> = _snapshotHistory.asStateFlow()

    private val _connections = MutableStateFlow<List<ConnectionRecord>>(emptyList())
    val connections: StateFlow<List<ConnectionRecord>> = _connections.asStateFlow()

    private val _appTraffic = MutableStateFlow<List<AppTrafficInfo>>(emptyList())
    val appTraffic: StateFlow<List<AppTrafficInfo>> = _appTraffic.asStateFlow()

    private val _selectedTimeWindow = MutableStateFlow(TimeWindow.LAST_1_MINUTE)
    val selectedTimeWindow: StateFlow<TimeWindow> = _selectedTimeWindow.asStateFlow()

    private val _retentionLimit = MutableStateFlow(500)
    val retentionLimit: StateFlow<Int> = _retentionLimit.asStateFlow()

    // Session baselines for relative delta calculation (rx/tx tracking)
    internal var baselineRxBytes: Long = 0L
    internal var baselineTxBytes: Long = 0L
    internal var lastSampleRxBytes: Long = 0L
    internal var lastSampleTxBytes: Long = 0L
    internal var lastSampleTimestamp: Long = 0L

    fun setMonitoringActive(
        active: Boolean,
        mode: MonitoringMode = _monitoringState.value.mode
    ) {
        val now = System.currentTimeMillis()
        _monitoringState.update { current ->
            current.copy(
                isMonitoring = active,
                mode = mode,
                sessionStartTime = if (active) now else current.sessionStartTime,
                sessionDurationMs = if (active) 0L else current.sessionDurationMs
            )
        }
    }

    fun updateSessionDuration(durationMs: Long) {
        _monitoringState.update { it.copy(sessionDurationMs = durationMs) }
    }

    fun setDisclosureAcknowledged(acknowledged: Boolean) {
        _monitoringState.update { it.copy(disclosureAcknowledged = acknowledged) }
    }

    fun setVpnPermissionGranted(granted: Boolean) {
        _monitoringState.update { it.copy(vpnPermissionGranted = granted) }
    }

    fun setUsageAccessGranted(granted: Boolean) {
        _monitoringState.update { it.copy(usageAccessGranted = granted) }
    }

    fun setMonitoringMode(mode: MonitoringMode) {
        _monitoringState.update { it.copy(mode = mode) }
    }

    fun setSelectedTimeWindow(window: TimeWindow) {
        _selectedTimeWindow.value = window
    }

    fun setRetentionLimit(limit: Int) {
        _retentionLimit.value = limit
        // Enforce new limit on connection history
        _connections.update { list ->
            if (list.size > limit) list.take(limit) else list
        }
    }

    fun recordSnapshot(snapshot: TrafficSnapshot) {
        _currentSnapshot.value = snapshot

        // Maintain time series for chart (keep max 180 points, i.e., 3 minutes of 1-second ticks, or scaled)
        _snapshotHistory.update { history ->
            val updated = history + snapshot
            val maxPoints = 180
            if (updated.size > maxPoints) {
                updated.drop(updated.size - maxPoints)
            } else {
                updated
            }
        }
    }

    fun addConnectionRecord(record: ConnectionRecord) {
        if (!_monitoringState.value.isMonitoring) return

        _connections.update { list ->
            val limit = _retentionLimit.value
            val updated = listOf(record) + list
            if (updated.size > limit) {
                updated.take(limit)
            } else {
                updated
            }
        }
    }

    fun updateAppTraffic(list: List<AppTrafficInfo>) {
        _appTraffic.value = list
    }

    fun clearHistory() {
        _connections.value = emptyList()
        _snapshotHistory.value = emptyList()
        
        // Reset session totals while preserving active state
        val now = System.currentTimeMillis()
        val currentSnap = _currentSnapshot.value
        baselineRxBytes = currentSnap.deviceRxTotalBytes
        baselineTxBytes = currentSnap.deviceTxTotalBytes
        lastSampleRxBytes = currentSnap.deviceRxTotalBytes
        lastSampleTxBytes = currentSnap.deviceTxTotalBytes
        lastSampleTimestamp = now

        _currentSnapshot.update {
            it.copy(
                sessionRxTotalBytes = 0L,
                sessionTxTotalBytes = 0L,
                rxSpeedBytesPerSec = 0L,
                txSpeedBytesPerSec = 0L,
                totalPacketsRecorded = 0L
            )
        }

        if (_monitoringState.value.isMonitoring) {
            _monitoringState.update {
                it.copy(sessionStartTime = now, sessionDurationMs = 0L)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: TrafficRepository? = null

        fun getInstance(): TrafficRepository {
            return instance ?: synchronized(this) {
                instance ?: TrafficRepository().also { instance = it }
            }
        }
    }
}
