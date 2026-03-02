package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public record StateDTO(boolean active, boolean occupied, double currentPower,
                       StateDTO.HealthStatus healthStatus,
                       double lastUpdateTime, String lastUpdateFormattedTime) {
    @JsonCreator
    public StateDTO(@JsonProperty("active") boolean active,
                    @JsonProperty("occupied") boolean occupied,
                    @JsonProperty("currentPower") double currentPower,
                    @JsonProperty("healthStatus") HealthStatus healthStatus,
                    @JsonProperty("lastUpdateTime") double lastUpdateTime,
                    @JsonProperty("lastUpdateFormattedTime") String lastUpdateFormattedTime) {
        this.active = active;
        this.occupied = occupied;
        this.currentPower = currentPower;
        this.healthStatus = healthStatus;
        this.lastUpdateTime = lastUpdateTime;
        this.lastUpdateFormattedTime = lastUpdateFormattedTime;
    }

    public enum HealthStatus {
        HEALTY,
        FAULTED,
        NOT_INITIALIZED;

        public boolean isInitialized() {
            return !this.equals(NOT_INITIALIZED);
        }

        public boolean isFaulted() {
            return this.equals(FAULTED);
        }

        public boolean isHealthy() {
            return this.equals(HEALTY);
        }
    }

}
