package com.sch.hubzone.charger_manager_service.startup;

import com.sch.hubzone.charger_manager_service.integration.input.SimulationWebSocketClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StartupService {

    private final SimulationWebSocketClient wsClient;

    public StartupService(SimulationWebSocketClient wsClient) {
        this.wsClient = wsClient;
    }


    public void connectWS() {
        wsClient.connect();
    }

    public void disconnectWS() {
        wsClient.disconnect();
    }

    public String isWsConnected() {
        return wsClient.isConnected() ? "Connesso" : "Non connesso";
    }

    @Getter
    public static class StartupException extends RuntimeException {
        private final HttpStatus httpStatus;

        public StartupException(HttpStatus status, String message) {
            super(message);
            this.httpStatus = status;
        }

    }
}
