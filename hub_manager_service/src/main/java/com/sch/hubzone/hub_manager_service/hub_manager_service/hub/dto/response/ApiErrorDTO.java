package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL) // Escludo i campi nulli dal JSON
@Schema(name = "ApiError", description = "Rappresenta un errore restituito dalle API, inclusi eventuali errori di validazione")
public class ApiErrorDTO {
    @Schema(description = "Codice applicativo univoco che categorizza l'errore", example = "HUB_NOT_FOUND")
    private final ApplicationErrorCode errorCode;

    @Schema(description = "Messaggio dettagliato dell'errore")
    private final String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp in cui si è verificato l'errore", type = "string", format = "date-time")
    private final Timestamp timestamp;

    @Schema(description = "Lista degli errori di validazione sui campi del DTO (presente solo in caso di errori di validazione)", nullable = true)
    private final List<ValidationError> validationErrors;

    public ApiErrorDTO(ApplicationErrorCode errorCode, String message, Timestamp timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.validationErrors = null;
    }

    public ApiErrorDTO(ApplicationErrorCode errorCode, String message, Timestamp timestamp, List<ValidationError> validationErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = timestamp;
        this.validationErrors = validationErrors;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL) // Escludo i campi nulli dal JSON
    @Schema(name = "ValidationError", description = "Errore di validazione relativo a un singolo campo")
    public static class ValidationError {

        @Schema(description = "Nome del campo che ha causato l'errore")
        private String field;

        @Schema(description = "Valore rifiutato durante la validazione")
        private Object rejectedValue;

        @Schema(description = "Messaggio di errore associato al campo")
        private String message;
    }
}
