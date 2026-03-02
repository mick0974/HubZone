package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.specification;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.ReservationRepository;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.specification.ReservationSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class ReservationSpecificationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    private final LocalDate today = LocalDate.now();
    private final LocalTime now = LocalTime.now();

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();

        Reservation expired = new Reservation();
        expired.setVehicleId("EV-EXPIRED");
        expired.setReservationDate(today.minusDays(1));
        expired.setStartTime(now.minusHours(1));
        expired.setEndTime(now.plusHours(1));
        expired.setChargerType("CA");

        Reservation current = new Reservation();
        current.setVehicleId("EV-CURRENT");
        current.setReservationDate(today);
        current.setStartTime(now.minusMinutes(30));
        current.setEndTime(now.plusMinutes(30));
        current.setChargerType("CA");

        Reservation future = new Reservation();
        future.setVehicleId("EV-FUTURE");
        future.setReservationDate(today.plusDays(1));
        future.setStartTime(now.minusHours(2));
        future.setEndTime(now.plusHours(2));
        future.setChargerType("CA");

        reservationRepository.saveAll(List.of(expired, current, future));
    }

    @Test
    void hasDate_shouldReturnOnlyTodayReservations() {
        Specification<Reservation> spec = ReservationSpecification.hasDate(today);

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .extracting(Reservation::getReservationDate)
                .containsExactly(today);
    }

    @Test
    void isBetweenTimeInterval_shouldReturnReservationsInTimeRange() {
        Specification<Reservation> spec = ReservationSpecification.isBetweenTimeInterval(
                now.minusHours(1), now.plusHours(1));

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .allMatch(r ->
                        r.getStartTime().isAfter(now.minusHours(1))
                                && r.getEndTime().isBefore(now.plusHours(1))
                );
    }

    @Test
    void hasDateAndIsBetweenTimeInterval_shouldReturnReservationsInTimeRange() {
        Specification<Reservation> spec =
                ReservationSpecification.hasDate(today)
                        .and(ReservationSpecification.isBetweenTimeInterval(
                                now.minusHours(1),
                                now.plusHours(1)
                        ));

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .allMatch(r ->
                        r.getReservationDate().equals(today) &&
                        r.getStartTime().isAfter(now.minusHours(1)) &&
                                r.getEndTime().isBefore(now.plusHours(1))
                );
    }

    @Test
    void hasDateNull_shouldReturnNothing() {
        Specification<Reservation> spec = ReservationSpecification.hasDate(null);

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void isBetweenTimeInterval_startTimeNull_shouldReturnNothing() {
        Specification<Reservation> spec = ReservationSpecification.isBetweenTimeInterval(
                null, now.plusHours(1));

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void isBetweenTimeInterval_endTimeNull_shouldReturnNothing() {
        Specification<Reservation> spec = ReservationSpecification.isBetweenTimeInterval(
                now.minusHours(1), null);

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void isBetweenTimeInterval_startTimeAfterEndTime_shouldReturnNothing() {
        Specification<Reservation> spec = ReservationSpecification.isBetweenTimeInterval(
                now.plusHours(1), now.minusHours(1));

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result).isEmpty();
    }

    @Test
    void hasDateAfter_shouldReturnReservationsAfterDate() {
        Specification<Reservation> spec = ReservationSpecification.hasDateAfter(today);

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result)
                .hasSize(1)
                .allMatch(r ->
                        r.getReservationDate().isAfter(today)
                );
    }

    @Test
    void hasDateAfter_noDate_shouldReturnNothing() {
        Specification<Reservation> spec = ReservationSpecification.hasDateAfter(null);

        List<Reservation> result = reservationRepository.findAll(spec);

        assertThat(result).hasSize(0);
    }

}
