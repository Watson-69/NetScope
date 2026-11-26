# Architecture

NetScope is a single-module Android application (`:app`) written in Kotlin with Jetpack Compose. It runs monitoring work in Android services and exposes state to the UI through singleton repositories backed by `StateFlow`.

<!-- Architecture overview -->

## Modules and packages

- `com.netscope.app`: `NetScopeApp` (Application entry point) initializes repositories and syncs initial settings.
- `com.netscope.app.data.model`: data classes and enums for traffic snapshots, connections, apps, monitoring state, network type, protocol, direction, and time windows.
- `com.netscope.app.data.repository`: `SettingsRepository` (SharedPreferences-backed settings) and `TrafficRepository` (in-memory traffic/connection state exposed via `StateFlow`).
- `com.netscope.app.data.manager`: `AppTrafficManager` collects per-app network usage via `TrafficStats` or `NetworkStatsManager`.
- `com.netscope.app.service`: `TrafficMonitoringService` (foreground service, standard metrics polling) and `NetScopeVpnService` (local VPN header metadata inspection).
- `com.netscope.app.ui`: `MainActivity`, navigation, and Compose screens for overview, connections, apps, and settings. Compose animations are used for the preloader and screen transitions.
- `com.netscope.app.ui.components`: reusable Compose components.
- `com.netscope.app.ui.theme`: theme definitions and color tokens.
- `com.netscope.app.util`: `FormatUtils` and `NetworkStateHelper`.

## Data flow

1. The UI starts or stops monitoring through `TrafficRepository` and the monitoring services.
2. Services poll Android traffic APIs and publish snapshots, connection records, and app traffic into `TrafficRepository`.
3. Compose screens collect `StateFlow` values from `TrafficRepository` and `SettingsRepository` and render them.
4. Settings changes persist through `SettingsRepository` and are mirrored into `TrafficRepository` where needed.

## Key responsibilities

- `TrafficRepository`: central in-memory state for monitoring status, snapshots, history, connections, app traffic, and selected time window.
- `SettingsRepository`: persistent user preferences such as theme, retention limit, monitoring mode, and disclosure acknowledgement.
- `TrafficMonitoringService`: collects global traffic rates, active sockets, and periodic per-app usage.
- `NetScopeVpnService`: establishes a local VPN and parses IP header metadata without capturing payloads.
- `AppTrafficManager`: resolves per-app traffic using usage stats when available, otherwise `TrafficStats`.
EOF