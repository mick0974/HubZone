package com.sch.hubzone.charger_manager_service.integration.input.dto.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Stato di un singolo charger in un hub")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class ChargerStatus {

    private String chargerId;
    private boolean occupied;
    private boolean active;

    /*
    *  valore cumulativo di energia distribuita
    */
    private double energy;

    /*
    *  valore di energia istantanea che sta venendo erogata
    */
    private double charging_energy;

    @Schema(description = "ID del veicolo elettrico collegato (obbligatorio se occupied=true)")
    private String evId;
}
