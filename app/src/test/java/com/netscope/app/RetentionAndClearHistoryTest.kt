package com.netscope.app

import com.netscope.app.data.model.AppTrafficInfo
import com.netscope.app.data.model.ConnectionRecord
import com.netscope.app.data.model.Direction
import com.netscope.app.data.model.Protocol
import com.netscope.app.data.model.TrafficSnapshot
import com.netscope.app.data.repository.TrafficRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RetentionAndClearHistoryTest {

    private lateinit var repository: TrafficRepository

    @Before
    fun setup() {
        repository = TrafficRepository.getInstance()
        repository.clearHistory()
        repository.setMonitoringActive(false)
    }

    @Test
    fun testRecordsIgnoredWhenMonitoringIsOff() {
        val record = ConnectionRecord(
            protocol = Protocol.TCP,
            direction = Direction.OUTBOUND,
            sourceIp = "192.168.1.100",
            sourcePort = 54321,
            destinationIp = "1.1.1.1",
            destinationPort = 443,
            packetSize = 120L
        )

        repository.addConnectionRecord(record)
        assertEquals(0, repository.connections.value.size)
    }

    @Test
    fun testRetentionLimitEnforcement() {
        repository.setMonitoringActive(true)
        repository.setRetentionLimit(5)

        for (i in 1..10) {
            repository.addConnectionRecord(
                ConnectionRecord(
                    protocol = Protocol.TCP,
                    direction = Direction.OUTBOUND,
                    sourceIp = "192.168.1.100",
                    sourcePort = 50000 + i,
                    destinationIp = "10.0.0.1",
                    destinationPort = 80,
                    packetSize = 100L * i
                )
            )
        }

        assertEquals(5, repository.connections.value.size)
    }

    @Test
    fun testClearHistoryResetsState() {
        repository.setMonitoringActive(true)
        repository.recordSnapshot(
            TrafficSnapshot(
                sessionRxTotalBytes = 5000L,
                sessionTxTotalBytes = 3000L,
                rxSpeedBytesPerSec = 500L,
                txSpeedBytesPerSec = 200L
            )
        )
        repository.addConnectionRecord(
            ConnectionRecord(
                protocol = Protocol.UDP,
                direction = Direction.INBOUND,
                sourceIp = "8.8.8.8",
                sourcePort = 53,
                destinationIp = "192.168.1.100",
                destinationPort = 55555,
                packetSize = 64L
            )
        )

        assertEquals(1, repository.connections.value.size)
        assertTrue(repository.currentSnapshot.value.sessionRxTotalBytes > 0)

        // Clear history
        repository.clearHistory()

        assertEquals(0, repository.connections.value.size)
        assertEquals(0, repository.snapshotHistory.value.size)
        assertEquals(0L, repository.currentSnapshot.value.sessionRxTotalBytes)
        assertEquals(0L, repository.currentSnapshot.value.sessionTxTotalBytes)
        assertEquals(0L, repository.currentSnapshot.value.rxSpeedBytesPerSec)
    }

    @Test
    fun testAppTrafficCalculations() {
        val app = AppTrafficInfo(
            uid = 10042,
            packageName = "com.example.browser",
            appName = "Browser",
            rxBytes = 7000L,
            txBytes = 3000L,
            percentageOfTotal = 25.5f
        )

        assertEquals(10000L, app.totalBytes)
        assertEquals(25.5f, app.percentageOfTotal)
    }
}
