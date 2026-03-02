package com.sch.hubzone.charger_manager_service.service.exception;

import lombok.Getter;

public class CommandFailedException extends RuntimeException {
    @Getter
    private final String errorCode;

    public CommandFailedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
