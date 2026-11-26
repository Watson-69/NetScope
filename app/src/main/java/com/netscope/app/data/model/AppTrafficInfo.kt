package com.netscope.app.data.model

data class AppTrafficInfo(
    val uid: Int,
    val packageName: String,
    val appName: String,
    val rxBytes: Long,
    val txBytes: Long,
    val percentageOfTotal: Float = 0f,
    val isSystemApp: Boolean = false
) {
    val totalBytes: Long get() = rxBytes + txBytes
}
