package com.sch.hubzone.hub_manager_service.hub_manager_service.startup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/hub/startup")
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

    @Operation(summary = "Inizializza l'hub con i dati del simulatore",
            description = """
                    Richiede al simulatore la lista degli hub disponibili,
                    seleziona l'hub configurato, recupera i connettori e li inizializza localmente.
                    L'operazione è consentita solo se l'hub non è già stato inizializzato.
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Hub inizializzato correttamente",
                    content = @Content(mediaType = "text/plain", schema = @Schema(example = "Dati hub ricevuti e salvati con successo"))),
            @ApiResponse(responseCode = "409", description = "Hub già inizializzato",
                    content = @Content(mediaType = "text/plain", schema = @Schema(example = "Hub già inizializzato"))),
            @ApiResponse(responseCode = "404", description = "Hub configurato non trovato nei dati del simulatore",
                    content = @Content(mediaType = "text/plain", schema = @Schema(example = "Hub con id hub_1 non trovato"))),
            @ApiResponse(responseCode = "503", description = "Simulatore non pronto o dati non disponibili/ricevuti",
                    content = @Content(mediaType = "text/plain", schema = @Schema(example = "Simulatore non disponibile"))),
            @ApiResponse(responseCode = "500", description = "Hub da gestire non indicato",
                    content = @Content(mediaType = "text/plain", schema = @Schema(example = "Hub da gestire non indicato")))})
    @PostMapping("/populate")
    public ResponseEntity<String> populateHub() {
        try {
            startupService.initHub();
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Dati hub ricevuti e salvati con successo");
        } catch (StartupService.StartupException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(e.getMessage());
        }
    }

}

