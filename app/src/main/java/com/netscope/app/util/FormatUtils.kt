package com.netscope.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Supported units for live network transfer rates.
 * Cycles smoothly through KB/s -> MB/s -> GB/s -> Auto upon user interaction.
 */
enum class RateUnit(val label: String) {
    KB_S("KB/s"),
    MB_S("MB/s"),
    GB_S("GB/s"),
    AUTO("Auto");

    fun next(): RateUnit = when (this) {
        KB_S -> MB_S
        MB_S -> GB_S
        GB_S -> AUTO
        AUTO -> KB_S
    }
}

object FormatUtils {

    fun formatBytes(bytes: Long): String {
        if (bytes < 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.2f MB", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB", gb)
    }

    fun formatRate(bytesPerSec: Long): String {
        if (bytesPerSec <= 0) return "0 B/s"
        if (bytesPerSec < 1024) return "$bytesPerSec B/s"
        val kb = bytesPerSec / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB/s", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format(Locale.US, "%.2f MB/s", mb)
        val gb = mb / 1024.0
        return String.format(Locale.US, "%.2f GB/s", gb)
    }

    fun formatRateWithUnit(bytesPerSec: Long, unit: RateUnit): String {
        if (bytesPerSec <= 0) {
            return when (unit) {
                RateUnit.KB_S -> "0.0 KB/s"
                RateUnit.MB_S -> "0.00 MB/s"
                RateUnit.GB_S -> "0.000 GB/s"
                RateUnit.AUTO -> "0 B/s"
            }
        }
        return when (unit) {
            RateUnit.KB_S -> {
                val kb = bytesPerSec / 1024.0
                String.format(Locale.US, "%.1f KB/s", kb)
            }
            RateUnit.MB_S -> {
                val mb = bytesPerSec / (1024.0 * 1024.0)
                String.format(Locale.US, "%.2f MB/s", mb)
            }
            RateUnit.GB_S -> {
                val gb = bytesPerSec / (1024.0 * 1024.0 * 1024.0)
                String.format(Locale.US, "%.3f GB/s", gb)
            }
            RateUnit.AUTO -> formatRate(bytesPerSec)
        }
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"
        val totalSeconds = durationMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun formatTimeWithMillis(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }
}
