package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public record ChargerStateUpdateDTO(String chargerId, StateDTO chargerState, String errorCode, String errorMessage) {
    public ChargerStateUpdateDTO(@JsonProperty(value = "chargerId", required = true) String chargerId,
                                 @JsonProperty("chargerState") StateDTO chargerState,
                                 @JsonProperty("errorCode") String errorCode,
                                 @JsonProperty("errorMessage") String errorMessage) {
        this.chargerId = chargerId;
        this.chargerState = chargerState;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isFetchFailed() {
        return errorCode != null;
    }
}
