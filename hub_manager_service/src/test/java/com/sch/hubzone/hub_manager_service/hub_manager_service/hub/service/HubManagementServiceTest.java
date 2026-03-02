package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Hub;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.ChargerStateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.HubStateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.dto.response.HubStructureDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.HubManagementService;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.HubStateManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
class HubManagementServiceTest {

    private final HubStateManager hubStateManager;
    private final HubManagementService hubManagementService;
    
    public HubManagementServiceTest() {
        hubStateManager = mock(HubStateManager.class);
        hubManagementService = new HubManagementService(hubStateManager);
    }

    private Hub initializedHubForTest() {
        Hub hub = new Hub();
        hub.updateSimulationTimestamp(0.0);

        hub.addCharger("charger-1", "CA", 22.0);
        hub.updateChargerState("charger-1", 15.5, true, true);

        hub.addCharger("charger-2", "CCS", 50.0);
        hub.updateChargerState("charger-2", 0.0, false, false);

        hub.addCharger("charger-3", "CCS", 100.0);
        hub.updateChargerState("charger-3", 70.0, true, true);

        return hub;
    }
    
    /* ========================================================
       Verifica struttura hub
       ======================================================== */

    @Test
    void getHubStructure_hubInitialized_shouldReturnCorrectStructure() {
        // Inizializzo struttura e stato dell'hub
        Hub hub = initializedHubForTest();
        when(hubStateManager.getHubState()).thenReturn(hub);

        HubStructureDTO result = hubManagementService.getHubStructure();

        // Verifico che venga effettivamente chiamato il metodo dell'hubStateManager
        verify(hubStateManager, times(1)).getHubState();

        // Verifico che il dto restituito non sia nullo e contenga la struttura di 3 connettori
        assertThat(result).isNotNull();
        assertThat(result.getChargerStructureDTOs()).hasSize(3);

        // Verifico il dto del primo connettore
        assertThat(result.getChargerStructureDTOs())
                .filteredOn(dto -> "charger-1".equals(dto.getChargerId()))
                .singleElement()
                .extracting(
                        HubStructureDTO.ChargerStructureDTO::getChargerId,
                        HubStructureDTO.ChargerStructureDTO::getChargerType,
                        HubStructureDTO.ChargerStructureDTO::getPlugPowerKw
                )
                .containsExactly("charger-1", "CA", 22.0);


        // Verifico il dto del secondo connettore
        assertThat(result.getChargerStructureDTOs())
                .filteredOn(dto -> "charger-2".equals(dto.getChargerId()))
                .singleElement()
                .extracting(
                        HubStructureDTO.ChargerStructureDTO::getChargerId,
                        HubStructureDTO.ChargerStructureDTO::getChargerType,
                        HubStructureDTO.ChargerStructureDTO::getPlugPowerKw
                )
                .containsExactly("charger-2", "CCS", 50.0);

        // Verifico il dto del secondo connettore
        assertThat(result.getChargerStructureDTOs())
                .filteredOn(dto -> "charger-3".equals(dto.getChargerId()))
                .singleElement()
                .extracting(
                        HubStructureDTO.ChargerStructureDTO::getChargerId,
                        HubStructureDTO.ChargerStructureDTO::getChargerType,
                        HubStructureDTO.ChargerStructureDTO::getPlugPowerKw
                )
                .containsExactly("charger-3", "CCS", 100.0);
    }

    /* ========================================================
       Verifica restituzione stato corrente hub
       ======================================================== */

    @Test
    void getHubState_hubInitialized_shouldReturnCorrectState() {
        Hub hub = initializedHubForTest();
        when(hubStateManager.getHubState()).thenReturn(hub);

        HubStateDTO result = hubManagementService.getHubState();

        // Verifico che l'hubStateManager venga chiamato
        verify(hubStateManager, times(1)).getHubState();

        // Verifico che il dto restituito non sia nullo
        assertThat(result).isNotNull();

        //Verifico che lo stato dei connettori sia corretto
        assertThat(result.getChargerStates()).hasSize(3);
        assertThat(result.getChargerStates())
                .filteredOn(dto -> "charger-1".equals(dto.getChargerId()))
                .singleElement()
                .extracting(
                        ChargerStateDTO::getChargerOperationalState,
                        ChargerStateDTO::getCurrentPower,
                        ChargerStateDTO::isOccupied
                )
                .containsExactly(ChargerOperationalState.ACTIVE, 15.5, true);

        assertThat(result.getChargerStates())
                .filteredOn(dto -> "charger-2".equals(dto.getChargerId()))
                .singleElement()
                .extracting(
                        ChargerStateDTO::getChargerOperationalState,
                        ChargerStateDTO::getCurrentPower,
                        ChargerStateDTO::isOccupied
                )
                .containsExactly(ChargerOperationalState.INACTIVE, 0.0, false);

        assertThat(result.getChargerStates())
                .filteredOn(dto -> "charger-3".equals(dto.getChargerId()))
                .singleElement()
                .extracting(
                        ChargerStateDTO::getChargerOperationalState,
                        ChargerStateDTO::getCurrentPower,
                        ChargerStateDTO::isOccupied
                )
                .containsExactly(ChargerOperationalState.ACTIVE, 70.0, true);
    }

    /* ========================================================
       Verifica accensione/spegnimento connettore
       ======================================================== */

    @Test
    void changeChargerOperationalState_verifyDelegate() {
        String chargerId = "charger-1";
        ChargerOperationalState newState = ChargerOperationalState.IN_ACTIVATION;
        doNothing().when(hubStateManager)
                .updateChargerOperationalState(chargerId, newState);

        hubManagementService.changeChargerOperationalState(chargerId, newState);

        // Verifico che l'hubStateManager vanga chiamato correttamente
        verify(hubStateManager, times(1))
                .updateChargerOperationalState(chargerId, newState);
    }


}

