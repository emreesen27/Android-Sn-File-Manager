package com.sn.snfilemanager.core.util

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.app.Service

class FrNotificationManager(
    private val service: Service
) {
    private val notifications = mutableMapOf<Int, Notification>()
    private val notificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var foregroundId = 0

    fun notify(id: Int, notification: Notification) {
        synchronized(notifications) {
            if (notifications.isEmpty()) {
                service.startForeground(id, notification)
                notifications[id] = notification
                foregroundId = id
            } else {
                if (id == foregroundId) {
                    service.startForeground(id, notification)
                } else {
                    notificationManager.notify(id, notification)
                }
                notifications[id] = notification
            }
        }
    }

    fun cancel(id: Int) {
        synchronized(notifications) {
            if (id !in notifications) {
                return
            }
            if (id == foregroundId) {
                if (notifications.size == 1) {
                    service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
                    notifications -= id
                    foregroundId = 0
                } else {
                    notifications.entries.find { it.key != id }!!.let {
                        service.startForeground(it.key, it.value)
                        foregroundId = it.key
                    }
                    notificationManager.cancel(id)
                    notifications -= id
                }
            } else {
                notificationManager.cancel(id)
                notifications -= id
            }
        }
    }
}
