package com.tenagrim.meshgrouptestassg.util;

public final class Masking {

    private static final String MASK = "***";

    private Masking() {}

    public static String email(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return prefix(email, 2);
        }
        return prefix(email.substring(0, at), 2) + email.substring(at);
    }

    public static String phone(String phone) {
        if (phone == null) {
            return null;
        }
        if (phone.length() <= 6) {
            return MASK;
        }
        return phone.substring(0, 4) + MASK + phone.substring(phone.length() - 2);
    }

    public static String login(String login) {
        if (login == null) {
            return null;
        }
        if (login.indexOf('@') >= 0) {
            return email(login);
        }
        if (login.chars().allMatch(Character::isDigit)) {
            return phone(login);
        }
        return prefix(login, 2);
    }

    private static String prefix(String value, int visible) {
        return value.length() <= visible ? MASK : value.substring(0, visible) + MASK;
    }
}
