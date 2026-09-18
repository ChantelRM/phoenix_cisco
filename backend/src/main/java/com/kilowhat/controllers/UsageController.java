package com.kilowhat.controllers;

import com.kilowhat.services.UsageService;
import com.kilowhat.util.AuthUtil;
import io.javalin.Javalin;

import java.util.Map;

public class UsageController {

    private static final UsageService usageService = new UsageService();

    public static class DailyUsageRequest {
        public String date;
        public double kwh;
    }

    public static class PurchaseRequest {
        public double amountRand;
        public double unitsKwh;
        public String date;
    }

    public static void register(Javalin app) {
        app.post("/api/usage/daily", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            DailyUsageRequest req = ctx.bodyAsClass(DailyUsageRequest.class);
            ctx.status(201).json(usageService.recordDailyUsage(userId, req.date, req.kwh));
        });

        app.get("/api/usage/daily", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.json(usageService.getHistory(userId));
        });

        app.get("/api/usage/summary", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.json(usageService.getSummary(userId));
        });

        app.post("/api/purchases", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            PurchaseRequest req = ctx.bodyAsClass(PurchaseRequest.class);
            ctx.status(201).json(usageService.recordPurchase(userId, req.amountRand, req.unitsKwh, req.date));
        });

        app.get("/api/purchases", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.json(usageService.getPurchases(userId));
        });
    }
}
