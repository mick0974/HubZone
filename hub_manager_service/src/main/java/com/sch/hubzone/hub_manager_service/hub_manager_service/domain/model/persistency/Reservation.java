package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(name = "reservation_date_index", columnList = "reservation_date"),
                @Index(name = "reservation_time_index", columnList = "start_time, end_time"),
                @Index(name = "reservation_date_time_index", columnList = "reservation_date, start_time, end_time")
        })
@NoArgsConstructor
@Getter
@Setter
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "charger_type", nullable = false)
    private String chargerType;

    @JsonIgnore
    @CreationTimestamp
    @Column(name = "audit_created_at", nullable = false)
    private Timestamp createdAt;

    @JsonIgnore
    @UpdateTimestamp
    @Column(name = "audit_updated_at", nullable = false)
    private Timestamp updatedAt;
}
