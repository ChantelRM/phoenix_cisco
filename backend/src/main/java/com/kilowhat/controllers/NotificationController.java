package com.kilowhat.controllers;

import com.kilowhat.services.NotificationService;
import com.kilowhat.util.AuthUtil;
import io.javalin.Javalin;

public class NotificationController {

    private static final NotificationService notificationService = new NotificationService();

    public static void register(Javalin app) {
        // the frontend polls this to decide what pop-up to show
        app.get("/api/notifications", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.json(notificationService.getUnread(userId));
        });

        app.put("/api/notifications/{id}/read", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            int notificationId = Integer.parseInt(ctx.pathParam("id"));
            notificationService.markRead(userId, notificationId);
            ctx.status(204);
        });
    }
}
