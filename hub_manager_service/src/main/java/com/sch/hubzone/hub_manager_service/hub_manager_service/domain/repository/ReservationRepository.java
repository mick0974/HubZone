package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.repository;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
}
