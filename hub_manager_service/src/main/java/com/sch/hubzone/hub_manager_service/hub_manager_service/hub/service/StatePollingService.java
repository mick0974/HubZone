package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.HubNotInitializedException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.ChargerManagerRestClient;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.ChargerStateUpdateDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Service
@Slf4j
public class StatePollingService {

    private final ChargerManagerRestClient client;
    private final HubStateManager hubStateManager;

    public StatePollingService(ChargerManagerRestClient client, HubStateManager hubStateManager) {
        this.client = client;
        this.hubStateManager = hubStateManager;
    }

    @Scheduled(fixedDelayString = "${hub.pollState.fixedDelay}", initialDelayString = "${hub.pollState.initialDelay}")
    public void pollHubState() {
        try {
            hubStateManager.checkIfHubIsInitialized();
        } catch (HubNotInitializedException e) {
            log.warn("Hub not initialized, update stopped");
            return;
        }

        List<ChargerStateUpdateDTO> update;

        try {
             update = client.fetchHubState();
        } catch (RestClientResponseException e) {
            log.warn("Error fetching hub state, update stopped", e);
            return;
        }

        if (update == null || update.isEmpty()) {
            log.warn("[StatePollingService] Received empty or null hub updated state");
            return;
        }

        hubStateManager.updateHubState(update);
    }
}
