package com.sch.hubzone.charger_manager_service.integration.input.dto.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Map;

@Schema(description = "payload aggiornamento hub via WebSocket")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class HubStatusPayload {

    private String hubId;
    private double energy;                          // energia totale consumata dall’hub
    private int occupancy;                          // numero di veicoli in carica
    private Map<String, ChargerStatus> chargers;    // stato dei charger
    private ArrayList<Double> position;             // [lat, lon] della posizione dell'hub
}
