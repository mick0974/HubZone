package com.sch.hubzone.charger_gateway.service.exception;

public class ChargerNotFoundException extends RuntimeException {
    public ChargerNotFoundException(String message) {
        super(message);
    }
}
