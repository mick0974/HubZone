package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.data;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ChargerMetricsChange {
    private String chargerId;
    private double currentPower;
    private boolean occupied;
    private ChargerOperationalState operationalState;
}
