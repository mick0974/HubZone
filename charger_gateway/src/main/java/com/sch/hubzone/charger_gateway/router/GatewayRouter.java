package com.sch.hubzone.charger_gateway.router;

import com.sch.hubzone.charger_gateway.dto.CommandResult;
import com.sch.hubzone.charger_gateway.service.GatewayService;
import com.sch.hubzone.charger_gateway.service.exception.ChargerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Component
public class GatewayRouter {

    private final GatewayService gatewayService;

    public GatewayRouter(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions
                .route(RequestPredicates.GET("/hub/state"), this::getAllChargersState)
                .andRoute(RequestPredicates.POST("/charger/{chargerId}/activate"), this::activateCharger)
                .andRoute(RequestPredicates.POST("/charger/{chargerId}/deactivate"), this::deactivateCharger);
    }

    private Mono<ServerResponse> getAllChargersState(ServerRequest request) {
        return gatewayService.getAllChargerStates()
                .flatMap(states ->
                        ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(states));
    }

    private Mono<ServerResponse> activateCharger(ServerRequest request) {
        return gatewayService.activateCharger(request.pathVariable("chargerId"))
                .flatMap(response ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(response))
                .onErrorResume(ChargerNotFoundException.class, ex ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).bodyValue(
                                CommandResult.builder().success(false).errorCode("NOT_FOUND").errorMessage(ex.getMessage()).build()));
    }

    private Mono<ServerResponse> deactivateCharger(ServerRequest request) {
        return gatewayService.deactivateCharger(request.pathVariable("chargerId"))
                .flatMap(response ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(response))
                .onErrorResume(ChargerNotFoundException.class, ex ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).bodyValue(
                                CommandResult.builder().success(false).errorCode("NOT_FOUND").errorMessage(ex.getMessage()).build()));
    }
}
