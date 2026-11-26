package com.netscope.app

import com.netscope.app.data.model.Direction
import com.netscope.app.data.model.NetworkType
import com.netscope.app.data.model.Protocol
import org.junit.Assert.assertEquals
import org.junit.Test

class DirectionAndProtocolTest {
    // Direction and protocol tests

    @Test
    fun testProtocolDetectionFromPortAndNumber() {
        // DNS on port 53 takes precedence
        assertEquals(Protocol.DNS, Protocol.fromNumber(17, srcPort = 53, dstPort = 49200))
        assertEquals(Protocol.DNS, Protocol.fromNumber(17, srcPort = 51200, dstPort = 53))

        // HTTPS on port 443
        assertEquals(Protocol.HTTPS, Protocol.fromNumber(6, srcPort = 54000, dstPort = 443))

        // HTTP on port 80
        assertEquals(Protocol.HTTP, Protocol.fromNumber(6, srcPort = 54000, dstPort = 80))

        // General TCP
        assertEquals(Protocol.TCP, Protocol.fromNumber(6, srcPort = 8080, dstPort = 9000))

        // General UDP
        assertEquals(Protocol.UDP, Protocol.fromNumber(17, srcPort = 9999, dstPort = 9998))

        // ICMP
        assertEquals(Protocol.ICMP, Protocol.fromNumber(1))
        assertEquals(Protocol.ICMP, Protocol.fromNumber(58))

        // Unknown / Other
        assertEquals(Protocol.OTHER, Protocol.fromNumber(99))
    }

    @Test
    fun testNetworkTypeClassification() {
        assertEquals(NetworkType.WIFI, NetworkType.fromTransport(isWifi = true, isCellular = false, isEthernet = false))
        assertEquals(NetworkType.CELLULAR, NetworkType.fromTransport(isWifi = false, isCellular = true, isEthernet = false))
        assertEquals(NetworkType.ETHERNET, NetworkType.fromTransport(isWifi = false, isCellular = false, isEthernet = true))
        
        // When unknown or no reliable transport, label unknown rather than guessing
        assertEquals(NetworkType.UNKNOWN, NetworkType.fromTransport(isWifi = false, isCellular = false, isEthernet = false))
    }

    @Test
    fun testDirectionDisplayNames() {
        assertEquals("Inbound", Direction.INBOUND.displayName)
        assertEquals("Outbound", Direction.OUTBOUND.displayName)
        assertEquals("Internal", Direction.INTERNAL.displayName)
    }
}
