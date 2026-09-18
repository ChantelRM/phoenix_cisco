package com.kilowhat.controllers;

import com.kilowhat.models.ApplianceCategory;
import com.kilowhat.services.BackupCalculatorService;
import com.kilowhat.services.UserService;
import com.kilowhat.util.AuthUtil;
import io.javalin.Javalin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupController {

    private static final BackupCalculatorService calculator = new BackupCalculatorService();
    private static final UserService userService = new UserService();

    public static void register(Javalin app) {
        app.get("/api/backup/estimate", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            var user = userService.getById(userId);
            List<ApplianceCategory> categories = userService.getCategories(userId);

            double loadWatts = calculator.estimateLoadWatts(categories, user.householdSize);
            int runtimeMinutes = calculator.estimateRuntimeMinutes(loadWatts);

            Map<String, Object> result = new HashMap<>();
            result.put("estimatedLoadWatts", Math.round(loadWatts));
            result.put("runtimeMinutes", runtimeMinutes);
            ctx.json(result);
        });
    }
}
