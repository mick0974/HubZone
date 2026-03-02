package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommandResponseDTO(boolean success, String errorCode, String errorMessage) {
    public CommandResponseDTO(
            @JsonProperty(value = "success", required = true) boolean success,
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("errorMessage") String errorMessage) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
