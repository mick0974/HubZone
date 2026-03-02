package com.sch.hubzone.charger_manager_service.service;

import com.sch.hubzone.charger_manager_service.domain.ChargerState;
import com.sch.hubzone.charger_manager_service.dto.CommandResult;
import com.sch.hubzone.charger_manager_service.integration.input.dto.payload.ChargerStatus;
import com.sch.hubzone.charger_manager_service.integration.output.SimulationRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChargerStateService {

    private final SimulationRestClient simulationRestClient;
    private final ChargerState chargerState = new ChargerState();

    public ChargerStateService(SimulationRestClient simulationRestClient) {
        this.simulationRestClient = simulationRestClient;
    }

    public synchronized void updateChargerFromSimulation(ChargerStatus updateState, double updateTime, String updateTimeFormatted) {
        if (updateState == null) {
            log.error("[ChargerStateService] updateState is null, setting to fault state");
            chargerState.setToFaultState(updateTime, updateTimeFormatted);
        } else {
            chargerState.updateHealthyState(updateState.isActive(), updateState.isOccupied(),
                    updateState.getCharging_energy(), updateTime, updateTimeFormatted);
        }

        log.info("[ChargerStateService] Updated state: {}", chargerState);
    }

    public synchronized CommandResult activateCharger() {
        simulationRestClient.activateCharger();

        return CommandResult.builder()
                .success(true)
                .build();
    }

    public synchronized CommandResult deactivateCharger() {
        simulationRestClient.deactivateCharger();

        return CommandResult.builder()
                .success(true)
                .build();
    }

    public synchronized ChargerState getCurrentState() {
        return chargerState.deepCopy();
    }
}
