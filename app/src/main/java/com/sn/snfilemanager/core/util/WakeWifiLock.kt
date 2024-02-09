package com.sn.snfilemanager.core.util

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager

class WakeWifiLock(context: Context, tag: String) {
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val wakeLock =
        powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag)
            .apply { setReferenceCounted(false) }

    private val wifiLock =
        wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, tag)
            .apply { setReferenceCounted(false) }

    var isAcquired: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                wakeLock.acquire()
                wifiLock.acquire()
            } else {
                wifiLock.release()
                wakeLock.release()
            }
            field = value
        }
}
