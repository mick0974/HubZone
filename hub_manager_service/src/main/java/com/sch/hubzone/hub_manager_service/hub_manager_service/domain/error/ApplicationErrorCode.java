package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error;

import lombok.Getter;

@Getter
public enum ApplicationErrorCode {
    HUB_NOT_INITIALIZED("L'hub e la sua struttura non sono ancora stati inizializzati"),
    CHARGER_NOT_FOUND("Il connettore richiesto non è presente nella struttura dell'hub"),
    INVALID_TRANSITION_STATE("La richiesta di attivazione/disattivazione del connettore è stata rifiutata: " +
            "l'attivazione o disattivazione sono incompatibili con lo stato attuale del connettore"),
    NOT_ENOUGH_ACTIVE_CHARGER("La richiesta di attivazione/disattivazione del connettore è stata rifiutata: " +
            "almeno un connettore all'interno dell'hub deve rimanere sempre attivo"),
    CHARGER_IS_IN_ERROR_STATE("La richiesta di attivazione/disattivazione del connettore è stata rifiutata: " +
            "il connettore non risulta raggiungibile o si trova in uno stato di errore"),
    RESERVATION_NOT_FOUND("La prenotazione richiesta non è stata trovata tra quelle mantenute nell'hub"),
    COMMAND_EXECUTION_ERROR("Il simulatore ha incontrato un errore o ha restituito un esito negativo alla richiesta di attivazione/disattivazione del connettore"),
    VALIDATION_ERROR("Il corpo della richiesta HTTP non rispetto uno o più vincoli di validità");

    private final String description;

    ApplicationErrorCode(String description) {
        this.description = description;
    }
}
