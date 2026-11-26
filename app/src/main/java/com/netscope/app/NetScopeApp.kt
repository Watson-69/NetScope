package com.netscope.app

import android.app.Application
import com.netscope.app.data.repository.SettingsRepository
import com.netscope.app.data.repository.TrafficRepository

class NetScopeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize repositories
        val settings = SettingsRepository.getInstance(this)
        val traffic = TrafficRepository.getInstance()

        // Sync initial preferences
        traffic.setRetentionLimit(settings.retentionLimit.value)
        traffic.setDisclosureAcknowledged(settings.hasSeenDisclosure.value)
        traffic.setMonitoringMode(settings.preferredMode.value)
    }
}
