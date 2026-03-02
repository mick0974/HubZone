package com.sch.hubzone.charger_gateway.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ChargerStateUpdateDTO {
    private String chargerId;
    private ChargerState chargerState;
    private String errorCode;
    private String errorMessage;
}
