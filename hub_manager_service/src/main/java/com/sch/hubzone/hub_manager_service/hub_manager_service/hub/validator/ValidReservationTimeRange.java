package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ReservationTimeRangeValidator.class)
public @interface ValidReservationTimeRange {

    String message() default "L'orario di inizio deve essere precedente a quello di fine";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

