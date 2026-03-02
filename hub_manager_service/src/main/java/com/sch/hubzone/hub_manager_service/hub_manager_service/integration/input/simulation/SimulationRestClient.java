package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation;

import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.helper.ApiTemplateHelper;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.helper.ErrorStatusHandlerRegistry;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.GeneratedHubsResponse;
import com.sch.hubzone.hub_manager_service.hub_manager_service.startup.StartupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimulationRestClient {

    @Value("${simulation.api.baseUrl}")
    private String simulationApiUrl;

    private final ApiTemplateHelper apiTemplateHelper;

    public SimulationRestClient(ApiTemplateHelper apiTemplateHelper) {
        this.apiTemplateHelper = apiTemplateHelper;
    }

    public GeneratedHubsResponse fetchHubs() {
        ErrorStatusHandlerRegistry errorHandlers = ErrorStatusHandlerRegistry
                .builder()
                .register(HttpStatus.NO_CONTENT, (response -> {
                    log.error("Nessun hub generato dal simulatore");
                    throw new StartupService.StartupException(HttpStatus.SERVICE_UNAVAILABLE, "Nessun hub generato dal simulatore");
                }))
                .register(HttpStatus.INTERNAL_SERVER_ERROR, (response -> {
                    log.error("Errore interno al simulatore");
                    throw new StartupService.StartupException(HttpStatus.SERVICE_UNAVAILABLE, "Errore interno al simulatore");
                }))
                .build();

        return apiTemplateHelper.execute(
                simulationApiUrl + "/hub",
                null,
                HttpMethod.GET,
                null,
                null,
                GeneratedHubsResponse.class,
                errorHandlers
        );
    }
}
