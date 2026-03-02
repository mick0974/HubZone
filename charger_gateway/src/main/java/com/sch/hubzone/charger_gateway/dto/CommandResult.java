package com.sch.hubzone.charger_gateway.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CommandResult {
    private boolean success;
    private String errorCode;
    private String errorMessage;
}
