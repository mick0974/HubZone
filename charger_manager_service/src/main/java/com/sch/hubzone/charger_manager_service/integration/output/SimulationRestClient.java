package com.sch.hubzone.charger_manager_service.integration.output;

import com.sch.hubzone.charger_manager_service.integration.output.dto.SimulationChargerStateDTO;
import com.sch.hubzone.charger_manager_service.service.exception.CommandFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class SimulationRestClient {

    @Value("${simulation.api.baseUrl}")
    private String baseUrl;
    @Value("${init.charger}")
    private String targetChargerId;

    private final RestClient restClient;

    public SimulationRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void activateCharger() {
        SimulationChargerStateDTO requestBody = new SimulationChargerStateDTO(
                targetChargerId,
                true
        );

        executeCommand(requestBody);
    }

    public void deactivateCharger() {
        SimulationChargerStateDTO requestBody = new SimulationChargerStateDTO(
                targetChargerId,
                false
        );

        executeCommand(requestBody);
    }

    private void executeCommand(SimulationChargerStateDTO requestBody) {
        String url = baseUrl + "/ChargerState";

        restClient
                .method(HttpMethod.POST)
                .uri(url)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (
                        (request, response) -> {
                            log.warn("Error {} while executing command on simulation: {}", response.getStatusCode(), response.getStatusText());
                            throw new CommandFailedException("BAD_REQUEST", response.getStatusText());
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, (
                        (request, response) -> {
                            log.warn("Error {} while executing command on simulation: {}", response.getStatusCode(), response.getStatusText());
                            throw new CommandFailedException("SIMULATION_ERROR", response.getStatusText());
                        }))
                .body(Void.class);
    }

}
