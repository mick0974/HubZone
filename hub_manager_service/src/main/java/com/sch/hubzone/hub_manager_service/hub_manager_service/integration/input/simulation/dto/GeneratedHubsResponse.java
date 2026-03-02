package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto;

import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload.HubListDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class GeneratedHubsResponse {
    private String state;    // IDLE, RUNNING, COMPLETED, ERROR, STOPPED
    private String message;  // Messaggio descrittivo
    private String error;    // Errore se presente (null se tutto ok)
    private Long timestamp;  // Timestamp della risposta
    private HubListDTO data;
}
