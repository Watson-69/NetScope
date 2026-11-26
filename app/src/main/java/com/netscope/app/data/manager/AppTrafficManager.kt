package com.netscope.app.data.manager

import android.Manifest
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Build
import com.netscope.app.data.model.AppTrafficInfo
import com.netscope.app.util.NetworkStateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AppTrafficManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getAppTrafficList(): List<AppTrafficInfo> = withContext(Dispatchers.IO) {
        val hasUsageStats = NetworkStateHelper.hasUsageStatsPermission(context)

        val list = if (hasUsageStats && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getAppTrafficFromNetworkStatsManager()
        } else {
            getAppTrafficFromTrafficStats()
        }

        // If list has zero-traffic apps only or is empty, ensure all installed network apps are listed
        if (list.isEmpty()) {
            getAllInstalledNetworkApps()
        } else {
            list
        }
    }

    private fun getAppTrafficFromTrafficStats(): List<AppTrafficInfo> {
        val installedApps = getInstalledAppsSafely()
        val list = mutableListOf<AppTrafficInfo>()
        var totalAllBytes = 0L

        for (app in installedApps) {
            val uid = app.uid
            val rx = TrafficStats.getUidRxBytes(uid)
            val tx = TrafficStats.getUidTxBytes(uid)

            val validRx = if (rx != TrafficStats.UNSUPPORTED.toLong() && rx > 0) rx else 0L
            val validTx = if (tx != TrafficStats.UNSUPPORTED.toLong() && tx > 0) tx else 0L

            val appName = getAppLabel(app)
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val total = validRx + validTx
            totalAllBytes += total

            // Include apps with data or non-system user apps
            if (total > 0 || !isSystem) {
                list.add(
                    AppTrafficInfo(
                        uid = uid,
                        packageName = app.packageName,
                        appName = appName,
                        rxBytes = validRx,
                        txBytes = validTx,
                        isSystemApp = isSystem
                    )
                )
            }
        }

        return list.map {
            val pct = if (totalAllBytes > 0) (it.totalBytes.toFloat() / totalAllBytes.toFloat()) * 100f else 0f
            it.copy(percentageOfTotal = pct)
        }.sortedWith(
            compareByDescending<AppTrafficInfo> { it.totalBytes }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun getAppTrafficFromNetworkStatsManager(): List<AppTrafficInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return getAppTrafficFromTrafficStats()
        }

        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
                ?: return getAppTrafficFromTrafficStats()

        val installedApps = getInstalledAppsSafely()
        val list = mutableListOf<AppTrafficInfo>()
        var totalAllBytes = 0L

        val endTime = System.currentTimeMillis()
        // Query past 7 days to capture full recent network usage
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000L)

        for (app in installedApps) {
            val uid = app.uid
            var rx = 0L
            var tx = 0L

            try {
                // Query Wi-Fi bucket
                val wifiBucket = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_WIFI,
                    null,
                    startTime,
                    endTime,
                    uid
                )
                val bucket = NetworkStats.Bucket()
                while (wifiBucket != null && wifiBucket.hasNextBucket()) {
                    wifiBucket.getNextBucket(bucket)
                    rx += bucket.rxBytes
                    tx += bucket.txBytes
                }
                wifiBucket?.close()

                // Query Mobile bucket
                val mobileBucket = networkStatsManager.queryDetailsForUid(
                    ConnectivityManager.TYPE_MOBILE,
                    null,
                    startTime,
                    endTime,
                    uid
                )
                while (mobileBucket != null && mobileBucket.hasNextBucket()) {
                    mobileBucket.getNextBucket(bucket)
                    rx += bucket.rxBytes
                    tx += bucket.txBytes
                }
                mobileBucket?.close()
            } catch (e: Exception) {
                // Fallback to TrafficStats
                val fallbackRx = TrafficStats.getUidRxBytes(uid)
                val fallbackTx = TrafficStats.getUidTxBytes(uid)
                if (fallbackRx != TrafficStats.UNSUPPORTED.toLong() && fallbackRx > 0) rx = fallbackRx
                if (fallbackTx != TrafficStats.UNSUPPORTED.toLong() && fallbackTx > 0) tx = fallbackTx
            }

            // Also check live TrafficStats since boot in case NetworkStatsManager hasn't flushed yet
            val liveRx = TrafficStats.getUidRxBytes(uid)
            val liveTx = TrafficStats.getUidTxBytes(uid)
            if (liveRx > rx && liveRx != TrafficStats.UNSUPPORTED.toLong()) rx = liveRx
            if (liveTx > tx && liveTx != TrafficStats.UNSUPPORTED.toLong()) tx = liveTx

            val appName = getAppLabel(app)
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val total = rx + tx
            totalAllBytes += total

            if (total > 0 || !isSystem) {
                list.add(
                    AppTrafficInfo(
                        uid = uid,
                        packageName = app.packageName,
                        appName = appName,
                        rxBytes = rx,
                        txBytes = tx,
                        isSystemApp = isSystem
                    )
                )
            }
        }

        return list.map {
            val pct = if (totalAllBytes > 0) (it.totalBytes.toFloat() / totalAllBytes.toFloat()) * 100f else 0f
            it.copy(percentageOfTotal = pct)
        }.sortedWith(
            compareByDescending<AppTrafficInfo> { it.totalBytes }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun getAllInstalledNetworkApps(): List<AppTrafficInfo> {
        val installedApps = getInstalledAppsSafely()
        return installedApps.map { app ->
            val rx = TrafficStats.getUidRxBytes(app.uid).coerceAtLeast(0)
            val tx = TrafficStats.getUidTxBytes(app.uid).coerceAtLeast(0)
            AppTrafficInfo(
                uid = app.uid,
                packageName = app.packageName,
                appName = getAppLabel(app),
                rxBytes = rx,
                txBytes = tx,
                percentageOfTotal = 0f,
                isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }.sortedWith(
            compareByDescending<AppTrafficInfo> { it.totalBytes }
                .thenBy { it.appName.lowercase() }
        )
    }

    private fun getInstalledAppsSafely(): List<ApplicationInfo> {
        return try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun getAppLabel(app: ApplicationInfo): String {
        return try {
            packageManager.getApplicationLabel(app).toString()
        } catch (e: Exception) {
            app.packageName
        }
    }
}
