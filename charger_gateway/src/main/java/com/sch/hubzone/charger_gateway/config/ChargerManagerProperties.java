package com.sch.hubzone.charger_gateway.config;

import com.sch.hubzone.charger_gateway.domain.ChargerManagerInstance;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "init")
public class ChargerManagerProperties {
    private List<ChargerManagerInstance> instances;
}
