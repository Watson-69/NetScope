package com.netscope.app.data.model

enum class NetworkType(val displayName: String) {
    WIFI("Wi-Fi"),
    CELLULAR("Cellular"),
    ETHERNET("Ethernet"),
    UNKNOWN("Unknown");

    companion object {
        fun fromTransport(isWifi: Boolean, isCellular: Boolean, isEthernet: Boolean): NetworkType {
            return when {
                isWifi -> WIFI
                isCellular -> CELLULAR
                isEthernet -> ETHERNET
                else -> UNKNOWN
            }
        }
    }
}
