package com.tenagrim.meshgrouptestassg.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long userId, BigDecimal requested, BigDecimal available) {
        super("Insufficient funds for user " + userId + ": requested " + requested + ", available " + available);
    }
    public InsufficientFundsException(Long userId, BigDecimal requested) {
        super("Insufficient funds for user " + userId + ": requested " + requested);
    }
}
