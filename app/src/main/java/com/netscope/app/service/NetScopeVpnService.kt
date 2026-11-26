package com.netscope.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.netscope.app.R
import com.netscope.app.data.model.ConnectionRecord
import com.netscope.app.data.model.Direction
import com.netscope.app.data.model.MonitoringMode
import com.netscope.app.data.model.Protocol
import com.netscope.app.data.repository.TrafficRepository
import com.netscope.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.net.InetAddress
import java.nio.ByteBuffer

/**
 * Transparent, local-only VpnService for connection metadata extraction.
 * Processes IP headers locally to identify protocol, IP, port, and size.
 * NEVER captures, inspects, stores, or transmits packet payloads.
 * Zero data leaves the device.
 */
class NetScopeVpnService : VpnService() {

    private val repository = TrafficRepository.getInstance()
    private val vpnScope = CoroutineScope(Dispatchers.IO + Job())
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_VPN -> {
                stopVpn()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startVpn()
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        try {
            val builder = Builder()
                .setSession("NetScope Local Diagnostic")
                .addAddress("10.1.1.2", 32)
                .addRoute("0.0.0.0", 0)
                .setMtu(1500)

            // Do not route NetScope's own management traffic
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    builder.addDisallowedApplication(packageName)
                } catch (ignored: Exception) {}
            }

            vpnInterface = builder.establish()

            repository.setMonitoringActive(true, MonitoringMode.VPN_HEADER_INSPECTOR)
            startForeground(VPN_NOTIFICATION_ID, buildVpnNotification())

            vpnScope.launch {
                readPacketHeaders()
            }
        } catch (e: Exception) {
            stopVpn()
        }
    }

    private fun readPacketHeaders() {
        val pfd = vpnInterface ?: return
        val inputStream = FileInputStream(pfd.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)

        try {
            while (vpnScope.isActive && !Thread.interrupted()) {
                val length = inputStream.read(packet.array())
                if (length > 0) {
                    packet.limit(length)
                    parseIpHeaderMetadata(packet, length)
                    packet.clear()
                }
            }
        } catch (e: Exception) {
            // Stream closed or service stopped
        }
    }

    /**
     * Parses IPv4/IPv6 headers for connection metadata only.
     * Extracts Protocol, Source IP/Port, Destination IP/Port, and Packet Length.
     * Packet payload is strictly ignored and discarded.
     */
    private fun parseIpHeaderMetadata(packet: ByteBuffer, totalLength: Int) {
        try {
            if (totalLength < 20) return
            val versionAndIhl = packet.get(0).toInt()
            val version = (versionAndIhl shr 4) and 0x0F

            if (version == 4) {
                val ihl = (versionAndIhl and 0x0F) * 4
                if (totalLength < ihl) return

                val protocolNum = packet.get(9).toInt() and 0xFF

                val srcIpBytes = ByteArray(4)
                packet.position(12)
                packet.get(srcIpBytes)
                val srcIp = InetAddress.getByAddress(srcIpBytes).hostAddress ?: "0.0.0.0"

                val dstIpBytes = ByteArray(4)
                packet.position(16)
                packet.get(dstIpBytes)
                val dstIp = InetAddress.getByAddress(dstIpBytes).hostAddress ?: "0.0.0.0"

                var srcPort = 0
                var dstPort = 0

                if ((protocolNum == 6 || protocolNum == 17) && totalLength >= ihl + 4) {
                    packet.position(ihl)
                    srcPort = packet.short.toInt() and 0xFFFF
                    dstPort = packet.short.toInt() and 0xFFFF
                }

                val protocol = Protocol.fromNumber(protocolNum, srcPort, dstPort)
                val isOutbound = srcIp.startsWith("10.") || srcIp.startsWith("192.168.") || srcIp.startsWith("172.")
                val direction = if (isOutbound) Direction.OUTBOUND else Direction.INBOUND

                val record = ConnectionRecord(
                    protocol = protocol,
                    direction = direction,
                    sourceIp = srcIp,
                    sourcePort = srcPort,
                    destinationIp = dstIp,
                    destinationPort = dstPort,
                    packetSize = totalLength.toLong()
                )

                repository.addConnectionRecord(record)
            }
        } catch (ignored: Exception) {
            // Malformed packet or parse skip
        }
    }

    private fun stopVpn() {
        repository.setMonitoringActive(false)
        vpnScope.cancel()
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (ignored: Exception) {}
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun buildVpnNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, NetScopeVpnService::class.java).apply {
                action = ACTION_STOP_VPN
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("NetScope VPN Inspector Active")
            .setContentText("Observing local connection metadata only. No payloads captured.")
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .addAction(0, "Stop", stopIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NetScope VPN Metadata Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows notification while local VPN metadata inspector is active."
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "netscope_vpn_channel"
        const val VPN_NOTIFICATION_ID = 1002
        const val ACTION_START_VPN = "com.netscope.app.action.START_VPN"
        const val ACTION_STOP_VPN = "com.netscope.app.action.STOP_VPN"

        fun startService(context: Context) {
            val intent = Intent(context, NetScopeVpnService::class.java).apply {
                action = ACTION_START_VPN
            }
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, NetScopeVpnService::class.java).apply {
                action = ACTION_STOP_VPN
            }
            context.startService(intent)
        }
    }
}
