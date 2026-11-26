package com.netscope.app

import com.netscope.app.data.model.TrafficSnapshot
import com.netscope.app.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class TrafficAggregationTest {
    // Traffic aggregation tests

    @Test
    fun testTrafficSnapshotCalculations() {
        val snapshot = TrafficSnapshot(
            rxSpeedBytesPerSec = 1024 * 1024 * 2L, // 2 MB/s
            txSpeedBytesPerSec = 512 * 1024L,       // 512 KB/s
            sessionRxTotalBytes = 100 * 1024 * 1024L,
            sessionTxTotalBytes = 25 * 1024 * 1024L
        )

        assertEquals(125 * 1024 * 1024L, snapshot.totalSessionBytes)
        assertEquals(2621440L, snapshot.currentTotalSpeedBytesPerSec)
    }

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", FormatUtils.formatBytes(0))
        assertEquals("500 B", FormatUtils.formatBytes(500))
        assertEquals("1.0 KB", FormatUtils.formatBytes(1024))
        assertEquals("1.5 KB", FormatUtils.formatBytes(1536))
        assertEquals("10.00 MB", FormatUtils.formatBytes(10 * 1024 * 1024))
        assertEquals("1.50 GB", FormatUtils.formatBytes((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun testFormatRate() {
        assertEquals("0 B/s", FormatUtils.formatRate(0))
        assertEquals("800 B/s", FormatUtils.formatRate(800))
        assertEquals("2.0 KB/s", FormatUtils.formatRate(2048))
        assertEquals("5.00 MB/s", FormatUtils.formatRate(5 * 1024 * 1024))
    }

    @Test
    fun testFormatDuration() {
        assertEquals("00:00", FormatUtils.formatDuration(0))
        assertEquals("00:45", FormatUtils.formatDuration(45 * 1000L))
        assertEquals("02:15", FormatUtils.formatDuration((2 * 60 + 15) * 1000L))
        assertEquals("01:10:05", FormatUtils.formatDuration((3600 + 10 * 60 + 5) * 1000L))
    }
}
