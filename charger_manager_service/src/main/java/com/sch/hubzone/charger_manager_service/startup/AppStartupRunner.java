package com.sch.hubzone.charger_manager_service.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "init.onStartup",
        havingValue = "true",
        matchIfMissing = false
)
public class AppStartupRunner implements ApplicationRunner {

    private final StartupService startupService;

    public AppStartupRunner(StartupService startupService) {
        this.startupService = startupService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Inizializzazione ChargerManagerService in corso");
        try {
            startupService.connectWS();
        } catch (StartupService.StartupException e) {
            log.error("Errore durante la fase di inizializzazione: {}", e.getMessage());
        }
    }
}
