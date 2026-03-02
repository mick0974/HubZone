package com.sch.hubzone.hub_manager_service.hub_manager_service.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        name = "hub.init.onStartup",
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
        log.info("Inizializzazione hub in corso");
        try {
            startupService.initHub();
        } catch (StartupService.StartupException e) {
            log.error("Errore durante la fase di inizializzazione: {}", e.getMessage());
        }
    }
}
