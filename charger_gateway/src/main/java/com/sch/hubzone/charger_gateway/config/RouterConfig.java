package com.sch.hubzone.charger_gateway.config;

import com.sch.hubzone.charger_gateway.router.GatewayRouter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> chargerRoutes(GatewayRouter router) {
        return router.routes();
    }
}
