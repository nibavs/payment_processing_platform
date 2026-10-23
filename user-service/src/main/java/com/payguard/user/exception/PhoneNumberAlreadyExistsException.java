package com.payguard.user.exception;

public class PhoneNumberAlreadyExistsException extends RuntimeException {
    public PhoneNumberAlreadyExistsException(String phone) {
        super("An account with phone '" + phone + "' already exists");
    }
}