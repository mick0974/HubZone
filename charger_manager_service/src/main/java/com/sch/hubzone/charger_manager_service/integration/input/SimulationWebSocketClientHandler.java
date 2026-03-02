package com.sch.hubzone.charger_manager_service.integration.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sch.hubzone.charger_manager_service.integration.input.dto.WebSocketUpdate;
import com.sch.hubzone.charger_manager_service.integration.input.dto.payload.ChargerStatus;
import com.sch.hubzone.charger_manager_service.integration.input.dto.payload.HubStatusPayload;
import com.sch.hubzone.charger_manager_service.integration.input.dto.payload.TimeStepPayload;
import com.sch.hubzone.charger_manager_service.service.ChargerStateService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
public class SimulationWebSocketClientHandler extends TextWebSocketHandler {

    private final ChargerStateService chargerStateService;
    private final ObjectMapper objectMapper;
    @Value("${init.hub}")
    private String targetHubId;
    @Value("${init.charger}")
    private String targetChargerId;

    public SimulationWebSocketClientHandler(ChargerStateService chargerStateService, ObjectMapper objectMapper) {
        this.chargerStateService = chargerStateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        log.info("[WebSocket] Connessione alla Simulazione completata con successo");
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
        log.debug("[WebSocket] Ricevuto messaggio: {}", message.getPayload());
        String payload = message.getPayload();

        try {
            JsonNode messageRoot = objectMapper.readTree(payload);
            JsonNode typeNode = messageRoot.get("type");

            if (typeNode == null || !typeNode.isTextual()) {
                log.error("[WebSocket] Il messaggio ricevuto non contiene il campo \"type\"");
                return;
            }

            String type = typeNode.asText();
            switch (type) {
                case "TimeStepUpdate" -> handleTimeStepPayload(payload);
                default -> log.warn("[WebSocket] Ricevuto messaggio con 'type' non gestito: {}", type);
            }
        } catch (JsonProcessingException e) {
            log.error("[WebSocket] Errore durante l'elaborazione del payload JSON ricevuto nel messaggio: {}", e.getMessage());
        }
    }

    private void handleTimeStepPayload(String payload) throws JsonProcessingException {
        WebSocketUpdate<TimeStepPayload> message =
                objectMapper.readValue(
                        payload,
                        new TypeReference<WebSocketUpdate<TimeStepPayload>>() {
                        }
                );

        if (!Objects.equals("success", message.getStatusMessage())) {
            log.warn("Messaggio scartato, campo 'statusMessage' = {}", message.getStatusMessage());
            return;
        }

        if (message.getPayload() == null || message.getPayload().getHubs() == null || message.getPayload().getHubs().isEmpty()) {
            log.warn("Messaggio scartato, payload hub mancante");
            return;
        }

        Optional<HubStatusPayload> hubTargetOpt = message.getPayload().getHubs().stream()
                .filter(dto -> dto.getHubId().equals(targetHubId))
                .findFirst();

        if (hubTargetOpt.isEmpty()) {
            log.warn("[WebSocket] Il messaggio ricevuto non aggiorna lo stato dell'hub target {}", targetHubId);
            return;
        }

        HubStatusPayload hubTarget = hubTargetOpt.get();

        if (hubTarget.getChargers() == null || hubTarget.getChargers().isEmpty()) {
            log.warn("[WebSocket] Nessun aggiornamento colonnine per l'hub target {}", targetHubId);
            return;
        }

        ChargerStatus chargerTarget = hubTarget.getChargers().get(targetChargerId);

        log.info("[WebSocket] Messaggio estratto con successo: {}", chargerTarget);
        chargerStateService.updateChargerFromSimulation(chargerTarget, message.getPayload().getTimestamp(),
                message.getPayload().getFormattedTime());
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) {
        log.error("[WebSocket] Errore di trasporto", exception);

    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        log.info("[WebSocket] Connessione alla simulazione chiusa con stato {}", status);
    }

}

