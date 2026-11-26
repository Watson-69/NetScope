package com.netscope.app.data.model

enum class Protocol(val displayName: String) {
    TCP("TCP"),
    UDP("UDP"),
    ICMP("ICMP"),
    DNS("DNS"),
    HTTPS("HTTPS / TLS"),
    HTTP("HTTP"),
    OTHER("OTHER");

    companion object {
        fun fromNumber(protocolNumber: Int, srcPort: Int = 0, dstPort: Int = 0): Protocol {
            if (srcPort == 53 || dstPort == 53) return DNS
            if (srcPort == 443 || dstPort == 443) return HTTPS
            if (srcPort == 80 || dstPort == 80) return HTTP

            return when (protocolNumber) {
                6 -> TCP
                17 -> UDP
                1, 58 -> ICMP
                else -> OTHER
            }
        }
    }
}

enum class Direction(val displayName: String) {
    INBOUND("Inbound"),
    OUTBOUND("Outbound"),
    INTERNAL("Internal")
}

data class ConnectionRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val protocol: Protocol,
    val direction: Direction,
    val sourceIp: String,
    val sourcePort: Int,
    val destinationIp: String,
    val destinationPort: Int,
    val packetSize: Long,
    val appName: String? = null,
    val uid: Int? = null
)
