package com.netscope.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.netscope.app.data.model.MonitoringMode
import com.netscope.app.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("netscope_settings", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(
                prefs.getString(KEY_THEME_MODE, AppThemeMode.BLACK.name) ?: AppThemeMode.BLACK.name
            )
        } catch (e: Exception) {
            AppThemeMode.BLACK
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _retentionLimit = MutableStateFlow(
        prefs.getInt(KEY_RETENTION, 500)
    )
    val retentionLimit: StateFlow<Int> = _retentionLimit.asStateFlow()

    private val _preferredMode = MutableStateFlow(
        MonitoringMode.valueOf(
            prefs.getString(KEY_MODE, MonitoringMode.STANDARD_METRICS.name)
                ?: MonitoringMode.STANDARD_METRICS.name
        )
    )
    val preferredMode: StateFlow<MonitoringMode> = _preferredMode.asStateFlow()

    private val _hasSeenDisclosure = MutableStateFlow(
        prefs.getBoolean(KEY_SEEN_DISCLOSURE, false)
    )
    val hasSeenDisclosure: StateFlow<Boolean> = _hasSeenDisclosure.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun setRetentionLimit(limit: Int) {
        _retentionLimit.value = limit
        prefs.edit().putInt(KEY_RETENTION, limit).apply()
    }

    fun setPreferredMode(mode: MonitoringMode) {
        _preferredMode.value = mode
        prefs.edit().putString(KEY_MODE, mode.name).apply()
    }

    fun setHasSeenDisclosure(seen: Boolean) {
        _hasSeenDisclosure.value = seen
        prefs.edit().putBoolean(KEY_SEEN_DISCLOSURE, seen).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode_v2"
        private const val KEY_RETENTION = "pref_retention"
        private const val KEY_MODE = "pref_mode"
        private const val KEY_SEEN_DISCLOSURE = "pref_seen_disclosure"

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
