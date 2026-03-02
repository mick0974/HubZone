package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.ReservationRepository;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository.specification.ReservationSpecification;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.ReservationDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.mapper.ReservationMapper;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.ReservationNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationDTO> getReservationsBy(LocalDate reservationDate, LocalTime startTime, LocalTime endTime) {
        if (endTime == null || startTime == null) {
            throw new IllegalArgumentException("startTime ed endTime devono essere entrambi presenti o entrambi assenti");
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("startTime non può essere successivo a endTime");
        }

        Specification<Reservation> spec =
                ReservationSpecification.hasDate(reservationDate)
                        .and(ReservationSpecification.isBetweenTimeInterval(startTime, endTime));

        List<Reservation> reservations = reservationRepository.findAll(spec);
        return ReservationMapper.toReservationDTOs(reservations);
    }

    public List<ReservationDTO> getAllReservations() {
        return ReservationMapper.toReservationDTOs(reservationRepository.findAll());
    }

    public ReservationDTO addNewReservation(ReservationDTO reservationDTO) {
        Reservation newReservation = ReservationMapper.toReservation(reservationDTO);
        newReservation = reservationRepository.save(newReservation);

        return ReservationMapper.toReservationDTO(newReservation);
    }

    public ReservationDTO updateReservation(Long id, ReservationDTO reservationDTO) {
        boolean exists = reservationRepository.existsById(id);
        if (!exists)
            throw new ReservationNotFoundException("Prenotazione con id " + id + " non trovata", ApplicationErrorCode.RESERVATION_NOT_FOUND);

        Reservation reservation = ReservationMapper.toReservation(reservationDTO);
        reservation.setId(id);

        reservation = reservationRepository.save(reservation);

        return ReservationMapper.toReservationDTO(reservation);
    }
}
