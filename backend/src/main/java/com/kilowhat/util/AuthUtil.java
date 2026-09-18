package com.kilowhat.util;

import io.javalin.http.Context;
import io.javalin.http.BadRequestResponse;

// No JWT/session system - after login the frontend stores the userId
// it got back and sends it as a header on every request after that.
// Simple on purpose: this is a hackathon MVP, not a public product.
public class AuthUtil {

    public static int requireUserId(Context ctx) {
        String header = ctx.header("X-User-Id");
        if (header == null) {
            throw new BadRequestResponse("Missing X-User-Id header - log in first");
        }
        try {
            return Integer.parseInt(header);
        } catch (NumberFormatException e) {
            throw new BadRequestResponse("X-User-Id must be a number");
        }
    }
}
