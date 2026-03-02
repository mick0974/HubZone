package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class HubStateUpdateDTO {
    List<ChargerStateUpdateDTO> states;
    private Double lastUpdateTime;
    private String lastUpdateFormattedTime;
}
