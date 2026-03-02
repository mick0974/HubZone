package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import lombok.Getter;

public class ApplicationBaseException extends RuntimeException {
    @Getter
    private final ApplicationErrorCode errorCode;

    public ApplicationBaseException(String message, ApplicationErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
