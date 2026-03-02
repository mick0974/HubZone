package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.output.kafka.message;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.persistency.Reservation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@JsonSerialize
@Schema(description = "Messaggio Kafka che descrive il cambiamento di stato rilevato dall'Hub manager")
public class HubStateMessage<T> {

    @Schema(description = "Identificativo univoco dell'hub", example = "hub_1")
    private String hubId;

    @Schema(description = "Tipo di cambiamento rilevato dall'hub", implementation = ChangeType.class)
    private ChangeType changeType;

    @Schema(description = "Payload del cambiamento, variabile in base al tipo")
    private T payload;

    @Schema(description = "Lista delle prenotazioni attive nell'hub, con data prenotazione pari o successiva a quella odierna")
    private List<Reservation> hubReservations;

    public enum ChangeType {
        CHARGER_OPERATIONAL_STATE_CHANGED,
        CHARGER_METRICS_CHANGED
    }
}
