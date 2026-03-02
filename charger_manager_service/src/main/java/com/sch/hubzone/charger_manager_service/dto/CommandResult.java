package com.sch.hubzone.charger_manager_service.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class CommandResult {
    private boolean success;
    private String errorCode;
    private String errorMessage;
}
