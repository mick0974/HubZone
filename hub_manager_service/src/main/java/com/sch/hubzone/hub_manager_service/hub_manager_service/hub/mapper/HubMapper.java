package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.mapper;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Hub;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.ChargerStateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.HubStateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.HubStructureDTO;

import java.util.ArrayList;
import java.util.List;

public class HubMapper {

    public static HubStateDTO toHubStateDTO(Hub hub) {
        HubStateDTO dto = new HubStateDTO();
        dto.setActiveChargers(hub.getHubMetrics().getActiveChargers());
        dto.setCurrentMaxPower(hub.getHubMetrics().getCurrentMaxPower());
        dto.setOccupiedChargers(hub.getHubMetrics().getOccupiedChargers());
        dto.setCurrentPowerInUse(hub.getHubMetrics().getCurrentPowerInUse());
        dto.setCurrentPowerRemaining(hub.getHubMetrics().getCurrentPowerRemaining());
        dto.setCurrentPowerInUsePercentage(hub.getHubMetrics().getCurrentPowerInUsePercentage());

        List<ChargerStateDTO> chargerStateDTOs = new ArrayList<>();
        hub.getAllChargers().forEach(charger ->
                chargerStateDTOs.add(toChargerStateDTO(charger)));

        dto.setChargerStates(chargerStateDTOs);
        return dto;
    }

    public static HubStructureDTO toHubStructureDTO(Hub hub) {
        List<HubStructureDTO.ChargerStructureDTO> structureDTOs = new ArrayList<>();
        hub.getAllChargers().forEach(charger ->
                structureDTOs.add(new HubStructureDTO.ChargerStructureDTO(charger.getChargerId(), charger.getChargerType(), charger.getPlugPowerKw())));

        return new HubStructureDTO(structureDTOs, List.of(hub.getLatitude(), hub.getLongitude()));
    }

    public static ChargerStateDTO toChargerStateDTO(Charger charger) {
        ChargerStateDTO dto = new ChargerStateDTO();

        dto.setChargerId(charger.getChargerId());
        dto.setChargerOperationalState(charger.getOperationalState());

        dto.setCurrentPower(charger.getCurrentPowerInUse());
        dto.setOccupied(charger.isOccupied());

        return dto;
    }
}
