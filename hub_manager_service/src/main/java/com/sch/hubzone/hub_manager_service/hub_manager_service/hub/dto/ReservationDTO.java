package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto;

import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.validator.ValidReservationTimeRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@Getter
@Setter
@Schema(
        name = "ReservationDTO",
        description = "DTO che rappresenta una prenotazione di una colonnina di ricarica"
)
@ValidReservationTimeRange // Annotazione custom che richiama il validator custom che verifica startTime < endTime
public class ReservationDTO {

    @NotBlank(message = "L'id del veicolo che ha prenotato non può essere nullo o vuoto")
    @Schema(
            description = "Identificativo del veicolo che effettua la prenotazione",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String vehicleId;

    @NotNull(message = "La data di prenotazione non può essere nulla")
    //@FutureOrPresent(message = "La data di prenotazione non può essere passata")
    @Schema(
            description = "Data della prenotazione all'interno della simulazione",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate reservationDate;

    @NotNull(message = "L'orario di inizio prenotazione non può essere nullo")
    @Schema(
            description = "Orario di inizio della prenotazione nella simulazione",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalTime startTime;

    @NotNull(message = "L'orario di fine prenotazione non può essere nullo")
    @Schema(
            description = "Orario di fine della prenotazione nella simulazione",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalTime endTime;

    @NotNull(message = "Il tipo di connettore prenotato non può essere nullo")
    @Schema(
            description = "Tipo di connettore prenotato nell'hub",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String chargerType;
}
