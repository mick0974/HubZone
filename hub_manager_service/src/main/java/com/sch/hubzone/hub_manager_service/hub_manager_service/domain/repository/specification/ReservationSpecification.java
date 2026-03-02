package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.specification;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;


public class ReservationSpecification {

    public static Specification<Reservation> hasDate(LocalDate reservationDate) {
        return (root, query, criteriaBuilder) -> {
            if (reservationDate == null)
                return criteriaBuilder.disjunction();

            return criteriaBuilder.equal(root.get("reservationDate"), reservationDate);
        };
    }

    public static Specification<Reservation> hasDateAfter(LocalDate reservationDate) {
        return (root, query, criteriaBuilder) -> {
            if (reservationDate == null)
                return criteriaBuilder.disjunction();

            return criteriaBuilder.greaterThan(root.get("reservationDate"), reservationDate);
        };
    }

    public static Specification<Reservation> isBetweenTimeInterval(LocalTime startTime, LocalTime endTime) {
        return (root, query, criteriaBuilder) -> {
            if (startTime == null || endTime == null)
                return criteriaBuilder.disjunction();

            if (startTime.isAfter(endTime))
                return criteriaBuilder.disjunction();

            return criteriaBuilder.and(
                    criteriaBuilder.between(root.get("startTime"), startTime, endTime),
                    criteriaBuilder.between(root.get("endTime"), startTime, endTime));
        };
    }
}
