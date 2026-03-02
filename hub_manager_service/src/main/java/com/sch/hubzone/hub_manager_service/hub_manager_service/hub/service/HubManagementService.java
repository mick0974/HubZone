package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Hub;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.ChargerStateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.HubStateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.HubStructureDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.mapper.HubMapper;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.ChargerNotFoundException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.HubNotInitializedException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.InvalidChargerStateTransitionException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.RemoteCommandException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementa la logica di business legata alle funzionalità rese disponibili al gestore dell'hub tramite REST API.
 */
@Slf4j
@Service
public class HubManagementService {

    private final HubStateManager hubStateManager;

    public HubManagementService(HubStateManager hubStateManager) {
        this.hubStateManager = hubStateManager;
    }

    /**
     * Restituisce il connettore specificato associato all'hub.
     * @param id id del connettore da restituire
     * @return il DTO associato al connettore richiesto
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     * @throws ChargerNotFoundException se l'hub non contiene il connettore indicato
     */
    private Charger findChargerStateById(String id) {
        return hubStateManager.getCharger(id);
    }

    /**
     * Restituisce l'hub mantenuto in memoria (struttura + stato simulatore).
     * @return l'hub mantenuto in memoria
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     */
    private Hub extractHubState() {
        return hubStateManager.getHubState();
    }

    /**
     * Restituisce la struttura dell'hub (parte statica di {@link Hub}).
     * @return il DTO rappresentante la struttura dell'hub mantenuto in memoria
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     */
    public HubStructureDTO getHubStructure() {
        Hub hub = extractHubState();
        return HubMapper.toHubStructureDTO(hub);
    }

    /**
     * Restituisce lo stato dell'hub (parte dinamica di {@link Hub}).
     * @return il DTO rappresentante lo stato dell'hub mantenuto in memoria
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     */
    public HubStateDTO getHubState() {
        Hub hub = extractHubState();
        return HubMapper.toHubStateDTO(hub);
    }

    /**
     * Restituisce il connettore identificato da {@code chargerId} associato all'hub.
     *
     * @param chargerId id del connettore da restituire
     * @return il DTO rappresentante il connettore richiesto.
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     * @throws ChargerNotFoundException se l'hub non contiene il connettore indicato
     */
    public ChargerStateDTO getChargerStateById(String chargerId) {
        Charger charger = findChargerStateById(chargerId);
        return HubMapper.toChargerStateDTO(charger);
    }

    /**
     * Richiede la modifica dello stato operativo ({@link ChargerOperationalState}) del connettore specificato. Verifica
     * localmente se il connettore può essere portato nello stato transitorio {@code newOperationalState} e comunica tale
     * richiesta al simulatore.
     *
     * @param chargerId id del connettore da modificare
     * @param newOperationalState stato operativo di transizione ({@code IN_DEACTIVATION} o {@code IN_ACTIVATION}) in cui portare il connettore
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     * @throws ChargerNotFoundException se l'hub non contiene il connettore indicato
     * @throws InvalidChargerStateTransitionException se il connettore non può essere portato nello stato specificato
     * @throws RemoteCommandException se la richiesta di cambio di stato operativo al simulatore è fallita
     */
    public void changeChargerOperationalState(String chargerId, ChargerOperationalState newOperationalState) {
        hubStateManager.updateChargerOperationalState(chargerId, newOperationalState);
    }

}
