package com.tenagrim.meshgrouptestassg.config;

public class Constants {
    public static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**"
    };
    public static final String PHONE_REGEX = "^\\d{11}$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
}
