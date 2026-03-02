package com.sch.hubzone.charger_manager_service.controller;

import com.sch.hubzone.charger_manager_service.domain.ChargerState;
import com.sch.hubzone.charger_manager_service.dto.CommandResult;
import com.sch.hubzone.charger_manager_service.service.ChargerStateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/charger")
public class ChargerStateController {

    private final ChargerStateService chargerStateService;

    public ChargerStateController(ChargerStateService chargerStateService) {
        this.chargerStateService = chargerStateService;
    }

    @GetMapping("/state")
    public ResponseEntity<ChargerState> getChargerLocalState() {
        ChargerState state = chargerStateService.getCurrentState();
        return ResponseEntity.status(HttpStatus.OK).body(state);
    }

    @PostMapping("/activate")
    public ResponseEntity<CommandResult> activateCharger() {
        CommandResult result = chargerStateService.activateCharger();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("/deactivate")
    public ResponseEntity<CommandResult> deactivateCharger() {
        CommandResult result = chargerStateService.deactivateCharger();
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
