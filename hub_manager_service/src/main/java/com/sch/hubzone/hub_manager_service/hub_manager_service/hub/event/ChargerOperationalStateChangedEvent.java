package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonSerialize;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonSerialize
@Schema(description = "Evento di variazione dello stato operativo di un connettore")
public class ChargerOperationalStateChangedEvent {

    @Schema(description = "ID del connettore")
    private String chargerId;

    @Schema(description = "Nuovo stato operativo del connettore (attiva, disattiva)")
    private ChargerOperationalState chargerOperationalState;

    @Schema(description = "Numero di connettori attivi in seguito all'attivazione/disattivazione della colonnina")
    private int nextActiveChargers;

    @Schema(description = "Potenza massima erogabile dell'hub in seguito all'attivazione/disattivazione della colonnina")
    private double nextAvailableMaxPower;
}
