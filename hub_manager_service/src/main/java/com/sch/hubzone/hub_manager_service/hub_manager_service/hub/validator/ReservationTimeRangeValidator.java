package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.validator;

import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.ReservationDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReservationTimeRangeValidator implements ConstraintValidator<ValidReservationTimeRange, ReservationDTO> {

    @Override
    public boolean isValid(ReservationDTO reservationDTO, ConstraintValidatorContext constraintValidatorContext) {
        if (reservationDTO == null)
            return true;

        // disabilito il messaggio di default
        constraintValidatorContext.disableDefaultConstraintViolation();
        boolean valid = true;
        if (reservationDTO.getStartTime() == null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("L'orario di inizio non può essere nullo")
                    .addPropertyNode("startTime")
                    .addConstraintViolation();
            valid = false;
        }

        if (reservationDTO.getEndTime() == null) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("L'orario di fine non può essere nullo")
                    .addPropertyNode("endTime")
                    .addConstraintViolation();
            valid = false;
        }

        if (!valid)
            return false;

        if (reservationDTO.getStartTime().isAfter(reservationDTO.getEndTime())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("L'orario di inizio deve essere precedente a quello di fine")
                    .addPropertyNode("startTime")
                    .addConstraintViolation();

            constraintValidatorContext.buildConstraintViolationWithTemplate("L'orario di fine deve essere successivo a quello di inizio")
                    .addPropertyNode("endTime")
                    .addConstraintViolation();

            return false;
        } else {
            return true;
        }
    }
}
