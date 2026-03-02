package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.HubMetrics;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.data.ChargerMetricsChange;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@JsonSerialize
@Schema(description = "Evento di variazione delle metriche delle colonnine")
public class ChargerMetricsChangedEvent {

    @Schema(description = "Metriche aggregate dell'hub")
    private HubMetrics aggregatedHubMetrics;

    @Schema(description = "Lista di connettori con relativo nuovo stato soggetti al cambiamento")
    private List<ChargerMetricsChange> newChargerStates;

    @Schema(description = "Timestamp della simulazione come numero di secondi passato dall'inizio della simulazione")
    private Double simulationTimestamp;

    @Schema(description = "Timestamp formattato ricevuto dal simulatore")
    private String formattedTimestamp;
}
