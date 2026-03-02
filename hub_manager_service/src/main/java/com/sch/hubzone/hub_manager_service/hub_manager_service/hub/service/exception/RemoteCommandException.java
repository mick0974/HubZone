package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;

public class RemoteCommandException extends ApplicationBaseException {
    public RemoteCommandException(String message, ApplicationErrorCode errorCode) {
        super(message, errorCode);
    }
}
