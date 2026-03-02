package com.sch.hubzone.charger_manager_service.integration.output.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class SimulationChargerStateDTO {
    String chargerId;

    @JsonProperty("isActive")
    boolean isActive;
}
