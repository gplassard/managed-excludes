package com.github.gplassard.managedexcludes.helpers

import com.github.gplassard.managedexcludes.MyBundle
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

class PluginNotifications {
    companion object {

        fun info(title: String, message: String) {
            Notifications.Bus.notify(
                Notification(
                    MyBundle.message("notifications.group"),
                    title,
                    message,
                    NotificationType.INFORMATION,
                )
            )
        }
    }
}
