package com.sch.hubzone.hub_manager_service.hub_manager_service.startup;

import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.HubStateManager;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.HubNotInitializedException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.SimulationRestClient;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.GeneratedHubsResponse;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload.HubDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload.HubListDTO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class StartupService {

    private final HubStateManager hubStateManager;
    private final SimulationRestClient client;

    @Value("${hub.init.hubTarget}")
    private String targetHubId;

    public StartupService(HubStateManager hubStateManager, SimulationRestClient client) {
        this.hubStateManager = hubStateManager;
        this.client = client;
    }

    public void initHub() {
        if (targetHubId == null) {
            log.error("Hub da gestire non indicato, configurazione non valida");
            throw new StartupException(HttpStatus.INTERNAL_SERVER_ERROR, "Hub da gestire non indicato, configurazione non valida");
        }

        try {
            if (hubStateManager.getHubState() != null) {
                log.error("Hub già inizializzato");
                throw new StartupException(HttpStatus.CONFLICT, "Hub già inizializzato");
            }
        } catch (HubNotInitializedException e) {
            log.info("Hub non ancora inizializzato");
        }

        HubDTO selectedHub = fetchHubDataFromSimulator();

        hubStateManager.initHub(selectedHub.getChargers(), selectedHub.getLatitude(), selectedHub.getLongitude());

        log.info("Hub inizializzato con successo");
    }

    private HubDTO fetchHubDataFromSimulator() {
        GeneratedHubsResponse response;

        try {
            response = client.fetchHubs();
        } catch (RestClientException e) {
            log.error("Errore nella ricezione degli hub generati dal simulatore", e);
            throw new StartupException(HttpStatus.SERVICE_UNAVAILABLE, "Simulatore non disponibile");
        }

        if (response.getState().equals("ERROR") || response.getError() != null) {
            log.error("Ricevuto messaggio di errore dal simulatore: {}", response.getError());
            throw new StartupException(HttpStatus.SERVICE_UNAVAILABLE, "Simulatore non disponibile");
        }

        HubListDTO payload = response.getData();
        if (payload == null || payload.getHubs() == null || payload.getHubs().isEmpty()) {
            log.error("Nessun hub ricevuto dal simulatore");
            throw new StartupException(HttpStatus.SERVICE_UNAVAILABLE, "Nessun hub ricevuto dal simulatore");
        }

        return payload.getHubs().stream()
                .filter(dto -> dto.getHubId() != null && dto.getHubId().equals(targetHubId))
                .findFirst()
                .orElseThrow(() ->
                        new StartupException(
                                HttpStatus.NOT_FOUND,
                                "Hub con id " + targetHubId + " non restituito dal simulatore"
                        )
                );
    }

    @Getter
    public static class StartupException extends RuntimeException {
        private final HttpStatus httpStatus;

        public StartupException(HttpStatus status, String message) {
            super(message);
            this.httpStatus = status;
        }

    }
}
