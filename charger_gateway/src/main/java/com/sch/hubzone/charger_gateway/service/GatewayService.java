package com.sch.hubzone.charger_gateway.service;

import com.sch.hubzone.charger_gateway.config.ChargerManagerProperties;
import com.sch.hubzone.charger_gateway.domain.ChargerManagerInstance;
import com.sch.hubzone.charger_gateway.dto.ChargerStateUpdateDTO;
import com.sch.hubzone.charger_gateway.dto.CommandResult;
import com.sch.hubzone.charger_gateway.integration.ChargerManagerClient;
import com.sch.hubzone.charger_gateway.service.exception.ChargerNotFoundException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class GatewayService {

    private final List<ChargerManagerInstance> instances;
    private final ChargerManagerClient chargerManagerClient;

    public GatewayService(ChargerManagerClient chargerManagerClient, ChargerManagerProperties chargerManagerProperties) {
        this.chargerManagerClient = chargerManagerClient;
        this.instances = chargerManagerProperties.getInstances();
    }

    public Mono<List<ChargerStateUpdateDTO>> getAllChargerStates() {
        log.info("instances: {}", instances);
        return Flux.fromIterable(instances)
                .flatMap(instance ->
                        chargerManagerClient.getChargerState(instance)
                                .map(chargerState -> ChargerStateUpdateDTO.builder()
                                        .chargerId(instance.chargerId())
                                        .chargerState(chargerState)
                                        .errorCode(null)
                                        .errorMessage(null)
                                        .build()
                                )
                                .onErrorResume(e -> getStateFallback(instance, e))
                )
                .collectList()
                .doOnSuccess(states ->
                        log.info("Aggregated {} charger states", states.size())
                );
    }

    public Mono<CommandResult> activateCharger(String chargerId) {
        return Mono.fromCallable(() -> findInstanceOrThrow(chargerId))
                .flatMap(instance -> chargerManagerClient.activateCharger(instance.url())
                        .onErrorResume(t -> changeOperationalStateFallback(chargerId, "activate", t)));
    }

    public Mono<CommandResult> deactivateCharger(String chargerId) {
        return Mono.fromCallable(() -> findInstanceOrThrow(chargerId))
                .flatMap(instance ->
                        chargerManagerClient.deactivateCharger(instance.url())
                            .onErrorResume(t -> changeOperationalStateFallback(chargerId, "deactivate", t)));
    }

    private ChargerManagerInstance findInstanceOrThrow(String chargerId) {
        return instances.stream()
                .filter(i -> i.chargerId().equals(chargerId))
                .findFirst()
                .orElseThrow(() -> new ChargerNotFoundException("Charger not found: " + chargerId));
    }

    private Mono<ChargerStateUpdateDTO> getStateFallback(ChargerManagerInstance instance, Throwable t) {
        log.warn("Circuit breaker / error for [{}] – returning fallback. Cause: {}",
                instance.chargerId(), t.getMessage());

        String errorCode;
        if (t instanceof CallNotPermittedException) {
            errorCode = "CIRCUIT_BREAKER_OPEN";
        } else if (t instanceof TimeoutException) {
            errorCode = "TIMEOUT";
        } else {
            errorCode = "REMOTE_ERROR";
        }

        return Mono.just(ChargerStateUpdateDTO.builder()
                .chargerId(instance.chargerId())
                .chargerState(null)
                .errorCode(errorCode)
                .errorMessage(t.getMessage())
                .build());
    }

    private Mono<CommandResult> changeOperationalStateFallback(String chargerId, String commandType, Throwable t) {
        log.error("Circuit breaker / error for [{}] – command [{}] not delivered. Cause: {}",
                chargerId, commandType, t.getMessage());

        String errorCode;

        if (t instanceof CallNotPermittedException) {
            errorCode = "CIRCUIT_BREAKER_OPEN";
        } else if (t instanceof TimeoutException) {
            errorCode = "TIMEOUT";
        } else {
            errorCode = "REMOTE_ERROR";
        }

        return Mono.just(
                CommandResult.builder()
                        .success(false)
                        .errorCode(errorCode)
                        .errorMessage(t.getMessage())
                        .build()
        );
    }

}
