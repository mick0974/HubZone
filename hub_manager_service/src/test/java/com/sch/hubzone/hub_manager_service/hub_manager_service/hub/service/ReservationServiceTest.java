package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.ReservationRepository;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.ReservationDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.mapper.ReservationMapper;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.ReservationService;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.ReservationNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public ReservationServiceTest() {
        this.reservationRepository = mock(ReservationRepository.class);
        this.reservationService = new ReservationService(reservationRepository);
    }

    private List<Reservation> initReservations() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Reservation expired = new Reservation();
        expired.setId(1L);
        expired.setVehicleId("EV-EXPIRED");
        expired.setReservationDate(today.minusDays(1));
        expired.setStartTime(LocalTime.of(10, 0));
        expired.setEndTime(LocalTime.of(11, 0));
        expired.setChargerType("CA");

        Reservation current = new Reservation();
        current.setId(2L);
        current.setVehicleId("EV-CURRENT");
        current.setReservationDate(today);
        current.setStartTime(now.minusMinutes(30));
        current.setEndTime(now.plusMinutes(30));
        current.setChargerType("CA");

        Reservation future = new Reservation();
        future.setId(3L);
        future.setVehicleId("EV-FUTURE");
        future.setReservationDate(today.plusDays(1));
        future.setStartTime(LocalTime.of(10, 0));
        future.setEndTime(LocalTime.of(11, 0));
        future.setChargerType("CA");

        return List.of(expired, current, future);
    }

    @Test
    void getAllReservations_shouldReturnAllReservations() {
        List<Reservation> reservations = initReservations();
        when(reservationRepository.findAll()).thenReturn(reservations);

        List<ReservationDTO> result = reservationService.getAllReservations();

        assertThat(result)
                .hasSize(3)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsExactlyInAnyOrderElementsOf(
                        reservations.stream()
                                .map(ReservationMapper::toReservationDTO)
                                .toList()
                );
    }

    @Test
    void getReservationsBy_shouldReturnFilteredReservations() {
        // Verifico solo che i filtri vengano accettati, la verifica delle specification va fatta con test di integrazione

        LocalDate date = LocalDate.now();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(18, 0);

        List<Reservation> reservations = initReservations();
        when(reservationRepository.findAll(any(Specification.class)))
                .thenReturn(reservations);

        List<ReservationDTO> result =
                reservationService.getReservationsBy(date, start, end);

        assertThat(result)
                .hasSize(3)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id", "createdAt", "updatedAt")
                .containsExactlyInAnyOrderElementsOf(
                        reservations.stream()
                                .map(ReservationMapper::toReservationDTO)
                                .toList()
                );

        verify(reservationRepository).findAll(any(Specification.class));
    }

    @Test
    void getReservationsBy_onlyStartTimeProvided_shouldThrowException() {
        assertThatThrownBy(() ->
                reservationService.getReservationsBy(
                        LocalDate.now(),
                        LocalTime.now(),
                        null
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startTime ed endTime devono essere entrambi presenti o entrambi assenti");
    }

    @Test
    void getReservationsBy_onlyEndTimeProvided_shouldThrowException() {
        assertThatThrownBy(() ->
                reservationService.getReservationsBy(
                        LocalDate.now(),
                        null,
                        LocalTime.now()
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startTime ed endTime devono essere entrambi presenti o entrambi assenti");
    }

    @Test
    void getReservationsBy_startTimeAfterEndTime_shouldThrowException() {
        assertThatThrownBy(() ->
                reservationService.getReservationsBy(
                        LocalDate.now(),
                        LocalTime.of(9, 0),
                        LocalTime.of(5, 0)
                )
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startTime non può essere successivo a endTime");
    }

    @Test
    void addNewReservation_shouldSaveAndReturnReservation() {
        Reservation reservation = initReservations().getFirst();
        ReservationDTO dto = ReservationMapper.toReservationDTO(reservation);
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(reservation);

        ReservationDTO result = reservationService.addNewReservation(dto);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(ReservationMapper.toReservationDTO(reservation));

        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void updateReservation_shouldUpdateAndReturnReservation() {
        Reservation reservation = initReservations().get(1);
        Long reservationId = reservation.getId();
        ReservationDTO dto = ReservationMapper.toReservationDTO(reservation);
        when(reservationRepository.existsById(reservationId)).thenReturn(true);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        ReservationDTO result = reservationService.updateReservation(reservationId, dto);

        assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt", "updatedAt")
                .isEqualTo(dto);

        verify(reservationRepository).existsById(reservationId);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void updateReservation_shouldThrowException_whenReservationNotFound() {
        when(reservationRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() ->
                reservationService.updateReservation(1L, mock(ReservationDTO.class))
        ).isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("non trovata");

        verify(reservationRepository, never()).save(any());
    }
}
