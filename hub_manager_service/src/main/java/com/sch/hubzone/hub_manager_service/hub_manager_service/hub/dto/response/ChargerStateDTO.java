package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ChargerStateDTO {
    private String chargerId;
    private ChargerOperationalState chargerOperationalState;
    private double currentPower;
    private boolean occupied;
}
