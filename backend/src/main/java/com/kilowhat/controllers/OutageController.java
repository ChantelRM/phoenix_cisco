package com.kilowhat.controllers;

import com.kilowhat.services.OutageService;
import com.kilowhat.util.AuthUtil;
import io.javalin.Javalin;

public class OutageController {

    private static final OutageService outageService = new OutageService();

    public static void register(Javalin app) {
        app.get("/api/outages/scheduled", ctx -> {
            String areaId = ctx.queryParam("area");
            ctx.json(outageService.getScheduleForArea(areaId));
        });

        app.get("/api/outages/manual", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.json(outageService.getManualOutages(userId));
        });

        app.post("/api/outages/manual", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.status(201).json(outageService.report(userId));
        });

        app.put("/api/outages/manual/{id}/restore", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            int outageId = Integer.parseInt(ctx.pathParam("id"));
            outageService.markRestored(userId, outageId);
            ctx.status(204);
        });
    }
}
