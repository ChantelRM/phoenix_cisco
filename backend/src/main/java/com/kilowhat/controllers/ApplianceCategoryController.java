package com.kilowhat.controllers;

import com.kilowhat.services.UserService;
import com.kilowhat.util.AuthUtil;
import io.javalin.Javalin;

import java.util.Map;

public class ApplianceCategoryController {

    private static final UserService userService = new UserService();

    public static void register(Javalin app) {
        app.get("/api/appliance-categories", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            ctx.json(userService.getCategories(userId));
        });

        // editing an existing category's count (e.g. "we bought another heater")
        app.put("/api/appliance-categories/{id}", ctx -> {
            int userId = AuthUtil.requireUserId(ctx);
            int categoryId = Integer.parseInt(ctx.pathParam("id"));
            int newCount = ((Number) ctx.bodyAsClass(Map.class).get("count")).intValue();
            userService.updateCategory(userId, categoryId, newCount);
            ctx.status(204);
        });
    }
}
