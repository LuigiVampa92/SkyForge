package com.luigivampa92.remoteandroidbuilds.ideplugin;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public final class NotificationManager {

    private static final String NOTIFICATION_ID = "Remote Android Builds";
    private static final String NOTIFICATION_TITLE = "Remote Builds";

    public void testNotification(@NotNull Project project, String message) {
        if (message != null && !message.isEmpty()) {
            NotificationGroup notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_ID);
            Notification notification = notificationGroup.createNotification(
                    NOTIFICATION_TITLE,
                    message,
                    NotificationType.INFORMATION
            );
            notification.notify(project);
        }
    }
}
