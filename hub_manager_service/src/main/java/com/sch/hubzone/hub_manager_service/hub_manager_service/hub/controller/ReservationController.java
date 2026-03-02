package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.controller;

import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.ReservationDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.ApiErrorDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/hub/reservation")
@Tag(name = "Reservation API", description = "API esposte per gestire prenotazioni di ricarica dell'hub")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Cerca prenotazioni filtrando per data e/o intervalli orari",
            description = """
                    Restituisce le prenotazioni dell'hub secondo dei filtri di ricerca. Attualmente è possibile ricercare:
                     - Tutte le prenotazioni per un giorno scelto;
                     - Tutte le prenotazioni che rientrano in un orario scelto;
                     - Tutte le prenotazioni per un giorno scelto che rientrano negli orari scelti.
                    Tutti i parametri sono opzionali. Non impostare alcun parametro equivale ad eseguire l'operazione GET /api/hub/reservation.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista delle prenotazioni filtrate",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservationDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Parametri non validi",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<List<ReservationDTO>> getReservationsBy(
            @Parameter(description = "Data della prenotazione (yyyy-MM-dd)", required = false)
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,

            @Parameter(description = "Orario di inizio prenotazione (HH:mm)", required = false)
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,

            @Parameter(description = "Orario di fine prenotazione (HH:mm)", required = false)
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "HH:mm") LocalTime endTime
    ) {
        List<ReservationDTO> dto = reservationService.getReservationsBy(date, startTime, endTime);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Recupera tutte le prenotazioni",
            description = "Restituisce la lista completa di tutte le prenotazioni presenti nell'hub."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista completa delle prenotazioni",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReservationDTO.class))))
    })
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        List<ReservationDTO> dto = reservationService.getAllReservations();
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "Crea una nuova prenotazione",
            description = "Permette di creare una nuova prenotazione nell'hub. Il body deve contenere un ReservationDTO valido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Prenotazione creata con successo",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dati del DTO non validi",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<ReservationDTO> createReservation(
            @Parameter(description = "Prenotazione da salvare", required = true)
            @Valid @RequestBody ReservationDTO reservationDTO
    ) {
        ReservationDTO dto = reservationService.addNewReservation(reservationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PutMapping("/{reservationId}")
    @Operation(
            summary = "Aggiorna una prenotazione esistente",
            description = "Permette di aggiornare una prenotazione esistente. Il body deve contenere un ReservationDTO valido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Prenotazione aggiornata con successo",
                    content = @Content(schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dati del DTO non validi",
                    content = @Content(schema = @Schema(implementation = ApiErrorDTO.class)))
    })
    public ResponseEntity<ReservationDTO> updateReservation(
            @PathVariable Long reservationId,
            @Parameter(description = "Prenotazione aggiornata", required = true)
            @Valid @RequestBody ReservationDTO reservationDTO
    ) {
        ReservationDTO dto = reservationService.updateReservation(reservationId, reservationDTO);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }
}