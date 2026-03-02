package com.sch.hubzone.charger_gateway.integration;

import com.sch.hubzone.charger_gateway.domain.ChargerManagerInstance;
import com.sch.hubzone.charger_gateway.dto.ChargerState;
import com.sch.hubzone.charger_gateway.dto.CommandResult;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ChargerManagerClient {

    private final WebClient.Builder webClientBuilder ;
    private final CircuitBreaker circuitBreaker;

    public ChargerManagerClient(WebClient.Builder webClientBuilder, CircuitBreakerRegistry registry) {
        this.webClientBuilder = webClientBuilder;
        this.circuitBreaker = registry.circuitBreaker("chargerManagerService");
    }

    public Mono<ChargerState> getChargerState(ChargerManagerInstance instance) {
        log.debug("Fetching state from charger instance [{}] at {}", instance.chargerId(), instance.url());

        return webClientBuilder
                .baseUrl(instance.url())
                .build()
                .get()
                .uri("/api/charger/state")
                .retrieve()
                .bodyToMono(ChargerState.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnNext(responseBody -> log.debug("Received state from [{}]", instance.chargerId()))
                .doOnError(e -> log.warn("Error fetching state from [{}]", instance.chargerId(), e));
    }

    public Mono<CommandResult> activateCharger(String url) {
        return webClientBuilder
                .baseUrl(url)
                .build()
                .post()
                .uri("/api/charger/activate")
                .retrieve()
                .bodyToMono(CommandResult.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnNext(responseBody -> log.debug("Received activate state from [{}]", url))
                .doOnError(t -> log.warn("Error fetching activate state from [{}]", url));

    }

    public Mono<CommandResult> deactivateCharger(String url) {
        return webClientBuilder
                .baseUrl(url)
                .build()
                .post()
                .uri("/api/charger/deactivate")
                .retrieve()
                .bodyToMono(CommandResult.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .doOnNext(responseBody -> log.debug("Received activate state from [{}]", url))
                .doOnError(t -> log.warn("Error fetching activate state from [{}]", url));
    }




}
