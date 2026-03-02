package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.controller.advice;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.ApiErrorDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.*;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class HubExceptionHandler {

    @ExceptionHandler(HubNotInitializedException.class)
    public ResponseEntity<ApiErrorDTO> handleHubNotInitializedException(HubNotInitializedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiErrorDTO(
                        e.getErrorCode(),
                        e.getMessage(),
                        Timestamp.from(Instant.now())
                )
        );
    }

    @ExceptionHandler(ChargerNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleChargerStateNotFoundException(ChargerNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiErrorDTO(
                        e.getErrorCode(),
                        e.getMessage(),
                        Timestamp.from(Instant.now())
                )
        );
    }

    @ExceptionHandler(InvalidChargerStateTransitionException.class)
    public ResponseEntity<ApiErrorDTO> handleInvalidChargerStateTransitionException(InvalidChargerStateTransitionException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ApiErrorDTO(
                        e.getErrorCode(),
                        e.getMessage(),
                        Timestamp.from(Instant.now())
                )
        );
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> handleReservationNotFoundException(ReservationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiErrorDTO(
                        e.getErrorCode(),
                        e.getMessage(),
                        Timestamp.from(Instant.now())
                )
        );
    }

    @ExceptionHandler(RemoteCommandException.class)
    public ResponseEntity<ApiErrorDTO> handleSimulatorCommunicationException(RemoteCommandException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new ApiErrorDTO(
                        e.getErrorCode(),
                        e.getMessage(),
                        Timestamp.from(Instant.now())
                )
        );
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
    public ResponseEntity<ApiErrorDTO> handleValidationExceptions(Exception ex) {
        List<ApiErrorDTO.ValidationError> errors = new ArrayList<>();

        if (ex instanceof MethodArgumentNotValidException manv) {
            manv.getBindingResult().getAllErrors().forEach(error -> {
                String field = "";
                String rejectedValue = null;
                if (error instanceof FieldError fieldError) {
                    field = fieldError.getField();
                    rejectedValue = fieldError.getRejectedValue().toString();
                } else {
                    ObjectError objectError = error;
                    field = objectError.getObjectName();
                }

                errors.add(ApiErrorDTO.ValidationError.builder()
                        .field(field)
                        .message(error.getDefaultMessage())
                        .rejectedValue(rejectedValue)
                        .build());
            });
        } else if (ex instanceof ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach(violation ->
                    errors.add(ApiErrorDTO.ValidationError.builder()
                    .field(violation.getPropertyPath().toString())
                    .message(violation.getMessage())
                    .rejectedValue(String.valueOf(violation.getInvalidValue()))
                    .build()));
        }

        ApiErrorDTO body = new ApiErrorDTO(
                ApplicationErrorCode.VALIDATION_ERROR,
                "Richiesta non valida",
                Timestamp.from(Instant.now()),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
