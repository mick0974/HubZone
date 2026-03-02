package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class HubMetrics {
    private int activeChargers = 0;
    private int occupiedChargers = 0;
    private double currentMaxPower = 0.0;
    private double currentPowerInUse = 0.0;
    private double currentPowerRemaining = 0.0;
    private double currentPowerInUsePercentage = 0.0;

    protected void applyStateChangeDelta(StateChangeDelta delta) {
        this.activeChargers += delta.activeChargersDelta();
        this.occupiedChargers += delta.occupiedChargersDelta();
        this.currentMaxPower += delta.currentMaxPowerDelta();
        this.currentPowerInUse += delta.currentPowerInUseDelta();
        this.currentPowerRemaining += delta.currentPowerRemainingDelta();
        this.currentPowerInUsePercentage = currentMaxPower > 0
                ? Math.round(((currentPowerInUse / currentMaxPower) * 100.0) * 100.0) / 100.0
                : 0.0;
    }

    protected HubMetrics deepCopy() {
        return new HubMetrics(activeChargers, occupiedChargers, currentMaxPower,
                currentPowerInUse, currentPowerRemaining, currentPowerInUsePercentage);
    }
}
