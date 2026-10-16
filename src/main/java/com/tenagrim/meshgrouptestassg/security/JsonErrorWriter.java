package com.tenagrim.meshgrouptestassg.security;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JsonErrorWriter {

    private JsonErrorWriter() {}

    public static void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
