package com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class HubListDTO {
    List<HubDTO> hubs;
}
