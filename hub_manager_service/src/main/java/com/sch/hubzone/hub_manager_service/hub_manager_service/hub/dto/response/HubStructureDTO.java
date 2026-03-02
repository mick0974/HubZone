package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class HubStructureDTO {
    private List<ChargerStructureDTO> chargerStructureDTOs;
    private List<Double> hubPosition;

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @ToString
    public static class ChargerStructureDTO {
        private String chargerId;
        private String chargerType;
        private double plugPowerKw;
    }
}
