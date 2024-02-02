package com.sn.snfilemanager.core.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context

class FrNotificationManager(
    private val service: Service,
) {
    private val notifications = mutableMapOf<Int, Notification>()
    private val notificationManager =
        service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var foregroundId = 0

    init {
        val channel =
            NotificationChannel(
                "FileOperationChannel",
                "File Operation Channel",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notification channel for file operation service"
            }
        notificationManager.createNotificationChannel(channel)
    }

    fun notify(
        id: Int,
        notification: Notification,
    ) {
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
