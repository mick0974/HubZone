package com.sch.hubzone.charger_manager_service.integration.input.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe base generica per tutti i messaggi di aggiornamento su WebSocket.
 *
 * @param <T> Il tipo di dato specifico contenuto nel payload.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Messaggio aggiornamento via WebSocket ha un campo payload generico da adattare in base al messaggio")
public class WebSocketUpdate<T> {

    // Getter e setter
    @Schema(description = "Tipo messaggio")
    private String type;

    @Schema(description = "Messaggio di stato opzionale")
    private String statusMessage;

    @Schema(description = "Payload personalizzabile")
    private T payload;

    @Override
    public String toString() {
        return "WebSocketUpdate{" +
                "type='" + type + '\'' +
                ", payload=" + payload +
                ", statusMessage='" + statusMessage + '\'' +
                '}';
    }
}
