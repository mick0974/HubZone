package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.validator;

import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.ReservationDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.validator.ReservationTimeRangeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;


@Disabled
class ReservationTimeRangeValidatorTest {

    private ReservationTimeRangeValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new ReservationTimeRangeValidator();

        when(context.buildConstraintViolationWithTemplate(anyString()))
                .thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString()))
                .thenReturn(nodeBuilder);
        when(nodeBuilder.addConstraintViolation())
                .thenReturn(context);
    }

    @Test
    void isValid_reservationDTONull_shouldReturnTrue() {
        Assertions.assertTrue(validator.isValid(null, null));
    }

    @Test
    void isValid_bothTimeNull_shouldReturnFalse() {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setStartTime(null);
        reservationDTO.setEndTime(null);

        Assertions.assertFalse(validator.isValid(reservationDTO, null));
    }

    @Test
    void isValid_startTimeNull_shouldReturnFalse() {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setStartTime(null);
        reservationDTO.setEndTime(LocalTime.now());

        Assertions.assertFalse(validator.isValid(reservationDTO, null));
    }

    @Test
    void isValid_endTimeNull_shouldReturnFalse() {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setStartTime(LocalTime.now());
        reservationDTO.setEndTime(null);

        Assertions.assertFalse(validator.isValid(reservationDTO, null));
    }

    @Test
    void isValid_timeRangeValid_shouldReturnTrue() {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setStartTime(LocalTime.now());
        reservationDTO.setEndTime(LocalTime.now().plusHours(1));

        Assertions.assertTrue(validator.isValid(reservationDTO, null));
    }

    @Test
    void isValid_timeRangeInvalid_shouldReturnFalse() {
        ReservationDTO reservationDTO = new ReservationDTO();
        reservationDTO.setStartTime(LocalTime.now().plusHours(1));
        reservationDTO.setEndTime(LocalTime.now());

        Assertions.assertFalse(validator.isValid(reservationDTO, null));
    }
}
