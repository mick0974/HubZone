package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
public class ErrorStatusHandlerRegistry {
    private final Map<HttpStatus, Consumer<ClientHttpResponse>> errorHandlers;
    private final Consumer<ClientHttpResponse> fallbackHandler;

    private ErrorStatusHandlerRegistry(Builder builder) {
        errorHandlers = builder.errorHandlers;
        fallbackHandler = builder.fallbackHandler;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(Consumer<ClientHttpResponse> fallbackHandler) {
        return new Builder(fallbackHandler);
    }

    public void handle(HttpStatus status, ClientHttpResponse response) {
        Consumer<ClientHttpResponse> handler = errorHandlers.getOrDefault(status, fallbackHandler);
        handler.accept(response);
    }

    public static class Builder {
        private final Map<HttpStatus, Consumer<ClientHttpResponse>> errorHandlers = new EnumMap<>(HttpStatus.class);
        private final Consumer<ClientHttpResponse> fallbackHandler;

        private Builder() {
            fallbackHandler = (response) -> {
                try {
                    log.error("Errore interno API {} non gestito: {}", response.getStatusCode(), response.getStatusText());
                    throw new RestClientResponseException(response.getStatusText(), response.getStatusCode(), response.getStatusCode().toString(), null, null, null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };
        }

        private Builder(Consumer<ClientHttpResponse> fallbackHandler) {
            Objects.requireNonNull(fallbackHandler);
            this.fallbackHandler = fallbackHandler;
        }

        public Builder register(HttpStatus key, Consumer<ClientHttpResponse> handler) {
            errorHandlers.put(key, handler);
            return this;
        }

        public ErrorStatusHandlerRegistry build() {
            return new ErrorStatusHandlerRegistry(this);
        }
    }

}
