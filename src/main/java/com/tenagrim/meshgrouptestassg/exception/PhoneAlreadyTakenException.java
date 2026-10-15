package com.tenagrim.meshgrouptestassg.exception;

public class PhoneAlreadyTakenException extends RuntimeException {
    public PhoneAlreadyTakenException(String phone) {
        super("Phone already taken: " + phone);
    }
}
