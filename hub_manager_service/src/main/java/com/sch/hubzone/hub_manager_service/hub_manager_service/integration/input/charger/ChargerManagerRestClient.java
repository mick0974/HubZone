package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.RemoteCommandException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.ChargerStateUpdateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.CommandResponseDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.helper.ApiTemplateHelper;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.helper.ErrorStatusHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
public class ChargerManagerRestClient {

    private final ObjectMapper objectMapper;
    @Value("${hub.gateway.url}")
    private String gatewayUrl;

    private final ApiTemplateHelper apiTemplateHelper;

    public ChargerManagerRestClient(ApiTemplateHelper apiTemplateHelper, ObjectMapper objectMapper) {
        this.apiTemplateHelper = apiTemplateHelper;
        this.objectMapper = objectMapper;
    }

    public CommandResponseDTO changeChargerOperationalState(String chargerId, boolean activate) {
        ErrorStatusHandlerRegistry errorHandlers = ErrorStatusHandlerRegistry
                .builder()
                .register(HttpStatus.SERVICE_UNAVAILABLE, (response -> {
                    CommandResponseDTO responseBody = readResponseBody(response);
                    log.error("Errore {}: {}", responseBody.errorCode(), responseBody.errorMessage());

                    throw new RemoteCommandException(responseBody.errorMessage(), ApplicationErrorCode.COMMAND_EXECUTION_ERROR);
                }))
                .register(HttpStatus.NOT_FOUND, (response -> {
                    CommandResponseDTO responseBody = readResponseBody(response);
                    log.error("Errore {}: {}", responseBody.errorCode(), responseBody.errorMessage());

                    throw new RemoteCommandException(responseBody.errorMessage(), ApplicationErrorCode.COMMAND_EXECUTION_ERROR);
                }))
                .build();

        String url = activate ?
                gatewayUrl + "/charger/%s/activate".formatted(chargerId)
                : gatewayUrl + "/charger/%s/deactivate".formatted(chargerId);

        return apiTemplateHelper.execute(
                url,
                null,
                HttpMethod.POST,
                null,
                null,
                CommandResponseDTO.class,
                errorHandlers
        );
    }

    public List<ChargerStateUpdateDTO> fetchHubState() {
        ErrorStatusHandlerRegistry errorHandlers = ErrorStatusHandlerRegistry
                .builder()
                .build();

        return apiTemplateHelper.execute(
                gatewayUrl + "/hub/state",
                null,
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<List<ChargerStateUpdateDTO>>() {},
                errorHandlers
        );
    }

    private CommandResponseDTO readResponseBody(ClientHttpResponse response) {
        try (InputStream bodyStream = response.getBody()) {
            // Uso Jackson per leggere direttamente dall'InputStream
            return objectMapper.readValue(bodyStream, CommandResponseDTO.class);
        } catch (IOException e) {
            log.warn("Impossibile leggere la risposta", e);
            return new CommandResponseDTO(false, "RESPONSE_READING_ERROR", "Errore nella lettura della risposta");
        } catch (IllegalArgumentException e) {
            log.warn("Impossibile deserializzare la risposta", e);
            return new CommandResponseDTO(false, "RESPONSE_DESERIALIZATION_ERROR", "Errore nella deserializzazione della risposta");
        }
    }
}
