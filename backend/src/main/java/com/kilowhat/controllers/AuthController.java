package com.kilowhat.controllers;

import com.kilowhat.models.ApplianceCategory;
import com.kilowhat.models.User;
import com.kilowhat.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.NotFoundResponse;

import java.util.List;
import java.util.Map;

public class AuthController {

    private static final UserService userService = new UserService();

    // matches the registration form shape: username, area, household size,
    // and a list of {category, count} instead of naming each appliance
    public static class RegisterRequest {
        public String username;
        public String areaId;
        public int householdSize;
        public List<ApplianceCategory> applianceCategories;
    }

    public static void register(Javalin app) {
        app.post("/api/auth/register", ctx -> {
            RegisterRequest req = ctx.bodyAsClass(RegisterRequest.class);
            User user = userService.register(req.username, req.areaId, req.householdSize, req.applianceCategories);
            ctx.status(201).json(user);
        });

        app.post("/api/auth/login", ctx -> {
            String username = ctx.bodyAsClass(Map.class).get("username").toString();
            User user = userService.login(username);
            if (user == null) throw new NotFoundResponse("No user with that username - register first");
            ctx.json(user);
        });

        app.get("/api/users/me", ctx -> {
            int userId = com.kilowhat.util.AuthUtil.requireUserId(ctx);
            User user = userService.getById(userId);
            if (user == null) throw new NotFoundResponse("User not found");
            ctx.json(user);
        });
    }
}
