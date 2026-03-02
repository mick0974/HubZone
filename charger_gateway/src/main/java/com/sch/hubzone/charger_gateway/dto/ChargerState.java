package com.sch.hubzone.charger_gateway.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class ChargerState {
    private boolean active;
    private boolean occupied;
    private double currentPower;
    private String healthStatus;
    private double lastUpdateTime;
    private String lastUpdateFormattedTime;
}
