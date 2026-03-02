package com.sch.hubzone.charger_manager_service.controller.advice;

import com.sch.hubzone.charger_manager_service.dto.CommandResult;
import com.sch.hubzone.charger_manager_service.service.exception.CommandFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommandFailedException.class)
    ResponseEntity<CommandResult> handleCommandFailedException(CommandFailedException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                CommandResult.builder()
                        .success(false)
                        .errorCode(e.getErrorCode())
                        .errorMessage(e.getMessage())
                        .build()
        );
    }
}
