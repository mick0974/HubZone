package com.sch.hubzone.charger_manager_service.startup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/charger/startup")
@Tag(
        name = "Startup API",
        description = "API esposte per gestire l'inizializzazione del microservizio, quali la destione della connessione " +
                "alla simulazione e all'inizializzazione dell'hub e della sua struttura."
)
public class StartupController {

    private final StartupService startupService;

    public StartupController(StartupService startupService) {
        this.startupService = startupService;
    }

    @Operation(
            summary = "Avvia la connessione WebSocket con il simulatore.",
            description = """
                    Avvia in modo asincrono la connessione WebSocket verso il simulatore.
                    La risposta indica solo l'accettazione della richiesta,
                    la connessione potrebbe non essere ancora stabilita al momento della risposta.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Richiesta accettata, connessione WebSocket in fase di instaurazione",
                    content = @Content(mediaType = "text/plain", schema = @Schema(example = "Richiesta accettata, connessione in corso..."))),
    })
    @PostMapping("/ws/connect")
    public ResponseEntity<String> connectToSimulation() {
        startupService.connectWS();

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Richiesta accettata, connessione in corso...");
    }

    @Operation(
            summary = "Restituisce lo stato corrente della connessione alla simulazione.",
            description = """
                    Restituisce lo stato della connessione alla simulazione. Gli stati previsti sono:
                     - Non connesso: nessuna connessione stabilita o si è verificato un errore;
                     - Connesso: connessione alla simulazione stabilita;
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Stato della connessione alla simulazione",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(
                            example = "Connesso"
                    )
            )
    )
    @GetMapping("/ws/status")
    public ResponseEntity<String> verifyConnectionToSimulation() {
        String state = startupService.isWsConnected();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(state);
    }

    @Operation(
            summary = "Termina la connessione alla simulazione.",
            description = """
                    Richiede la chiusura della connessione instaurata con la simulazione
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Connessione chiusa o già chiusa al momento della richiesta",
            content = @Content(
                    mediaType = "text/plain",
                    schema = @Schema(
                            example = "Connessione terminata"
                    )
            )
    )
    @PostMapping("/ws/disconnect")
    public ResponseEntity<String> disconnectFromSimulation() {
        startupService.disconnectWS();

        return ResponseEntity.status(HttpStatus.OK).body("Connessione chiusa con successo");
    }
}

