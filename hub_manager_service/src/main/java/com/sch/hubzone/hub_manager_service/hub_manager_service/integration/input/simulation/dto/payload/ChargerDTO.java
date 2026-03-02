package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload;

import lombok.*;

/**
 * DTO per un charger con supporto a tipo misto e potenza variabile.
 * <p>
 * Campi:
 * - chargerId: ID univoco della colonnina
 * - chargerType: Tipo di charger (AC, CCS, etc.)
 * - plugPowerKw: Potenza in kW
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChargerDTO {

    private String chargerId;
    private String chargerType;
    private double plugPowerKw;

}
