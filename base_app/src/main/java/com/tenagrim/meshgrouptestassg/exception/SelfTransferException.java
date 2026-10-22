package com.tenagrim.meshgrouptestassg.exception;

public class SelfTransferException extends RuntimeException {
    public SelfTransferException() {
        super("Cannot transfer money to yourself");
    }
}
