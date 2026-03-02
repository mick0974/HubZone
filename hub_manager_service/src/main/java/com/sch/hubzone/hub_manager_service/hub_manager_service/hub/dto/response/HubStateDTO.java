package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class HubStateDTO {
    private int activeChargers;
    private int occupiedChargers;
    private double currentMaxPower;
    private double currentPowerInUse;
    private double currentPowerRemaining;
    private double currentPowerInUsePercentage;
    private List<ChargerStateDTO> chargerStates;
}
