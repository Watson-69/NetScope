package com.netscope.app.data.model

data class TrafficSnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val rxSpeedBytesPerSec: Long = 0L,
    val txSpeedBytesPerSec: Long = 0L,
    val sessionRxTotalBytes: Long = 0L,
    val sessionTxTotalBytes: Long = 0L,
    val deviceRxTotalBytes: Long = 0L,
    val deviceTxTotalBytes: Long = 0L,
    val networkType: NetworkType = NetworkType.UNKNOWN,
    val activeConnectionsCount: Int = 0,
    val totalPacketsRecorded: Long = 0L
) {
    val totalSessionBytes: Long get() = sessionRxTotalBytes + sessionTxTotalBytes
    val currentTotalSpeedBytesPerSec: Long get() = rxSpeedBytesPerSec + txSpeedBytesPerSec
}
