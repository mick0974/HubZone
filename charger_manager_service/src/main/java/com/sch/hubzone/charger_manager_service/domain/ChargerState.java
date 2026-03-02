package com.sch.hubzone.charger_manager_service.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class ChargerState {
    private boolean active;
    private boolean occupied;
    private double currentPower;
    private HealthStatus healthStatus;
    private double lastUpdateTime;
    private String lastUpdateFormattedTime;

    public ChargerState() {
        this.active = false;
        this.occupied = false;
        this.currentPower = 0.0;
        this.lastUpdateTime = 0.0;
        this.lastUpdateFormattedTime = "";
        this.healthStatus = HealthStatus.NOT_INITIALIZED;
    }

    public void updateHealthyState(boolean active, boolean occupied, double currentPower, double lastUpdateTime, String lastUpdateFormattedTime) {
        this.active = active;
        this.occupied = occupied;
        this.currentPower = currentPower;
        this.lastUpdateTime = lastUpdateTime;
        this.lastUpdateFormattedTime = lastUpdateFormattedTime;

        this.healthStatus = HealthStatus.HEALTY;

    }

    public void setToFaultState(double lastUpdateTime, String lastUpdateFormattedTime) {
        this.lastUpdateTime = lastUpdateTime;
        this.lastUpdateFormattedTime = lastUpdateFormattedTime;

        this.healthStatus = HealthStatus.FAULTED;
    }

    public ChargerState deepCopy() {
        return new ChargerState(
                this.active,
                this.occupied,
                this.currentPower,
                this.healthStatus,
                this.lastUpdateTime,
                this.lastUpdateFormattedTime
        );
    }
}
