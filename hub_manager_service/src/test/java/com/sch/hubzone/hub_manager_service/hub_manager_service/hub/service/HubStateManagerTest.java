package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Hub;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.HubMetrics;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.ChargerMetricsChangedEvent;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.ChargerOperationalStateChangedEvent;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.data.ChargerMetricsChange;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.HubStateManager;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.ChargerNotFoundException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.HubNotInitializedException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.InvalidChargerStateTransitionException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.RemoteCommandException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.ChargerManagerRestClient;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.ChargerStateUpdateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.StateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload.ChargerDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
class HubStateManagerTest {

    private ApplicationEventPublisher eventPublisher;
    private ChargerManagerRestClient restClient;
    private HubStateManager hubStateManager;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        restClient = mock(ChargerManagerRestClient.class);
        hubStateManager = new HubStateManager(eventPublisher, restClient);
    }

    /* ========================================================
       Verifica inizializzazione hub
       ======================================================== */

    @Nested
    class HubInitializationTest {
        private List<ChargerStateUpdateDTO> generateUpdate() {
            StateDTO state1 = new StateDTO(true, false, 0.0,
                    StateDTO.HealthStatus.HEALTY, 0.0, "00:00:00");
            ChargerStateUpdateDTO update1 = new ChargerStateUpdateDTO("charger-1", state1, null, null);

            return List.of(update1);
        }

        @Test
        void initHub_shouldCreateHubWithChargers() {
            List<ChargerDTO> chargers = List.of(
                    new ChargerDTO("charger-1", "AC", 23.0),
                    new ChargerDTO("charger-2", "CCS", 50.0)
            );

            hubStateManager.initHub(chargers, 0.0, 0.0);

            Hub hub = hubStateManager.getHubState();
            assertThat(hub.getAllChargers()).hasSize(2);
            assertThat(hub.hasCharger("charger-1")).isTrue();
            assertThat(hub.hasCharger("charger-2")).isTrue();
            assertThat(hub.getCharger("charger-1").getOperationalState()).isEqualTo(ChargerOperationalState.NOT_INITIALIZED);
            assertThat(hub.getCharger("charger-2").getOperationalState()).isEqualTo(ChargerOperationalState.NOT_INITIALIZED);
        }

        @Test
        void initHub_firstSimulationUpdate() {
            List<ChargerDTO> chargers = List.of(
                    new ChargerDTO("charger-1", "AC", 23.0),
                    new ChargerDTO("charger-2", "CCS", 50.0)
            );
            hubStateManager.initHub(chargers, 0.0, 0.0);

            hubStateManager.updateHubState(generateUpdate());

            Hub hub = hubStateManager.getHubState();
            HubMetrics metrics = hub.getHubMetrics();

            // Il connettore è inizializzato di default come attivo non in uso
            assertThat(metrics.getActiveChargers()).isEqualTo(1);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(0);
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(23.0);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(0.0);

            assertThat(hub.getCharger("charger-1").getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
            assertThat(hub.getCharger("charger-2").getOperationalState()).isEqualTo(ChargerOperationalState.NOT_INITIALIZED);

        }
    }

    /* ========================================================
       Verifica aggiornamento stato hub da simulazione
       ======================================================== */

    @Nested
    class UpdateFromSimulationTest {
        private ChargerStateUpdateDTO generateUpdate(String chargerId, boolean occupied, boolean active, double currentPowerInUse) {
            StateDTO state = new StateDTO(active, occupied, currentPowerInUse,
                    StateDTO.HealthStatus.HEALTY, 0.0, "00:00:00");
            return new ChargerStateUpdateDTO(chargerId, state, null, null);
        }

        @BeforeEach
        void initializeHub() {
            List<ChargerDTO> chargers = List.of(
                    new ChargerDTO("charger-1", "AC", 23.0),
                    new ChargerDTO("charger-2", "CCS", 50.0),
                    new ChargerDTO("charger-3", "CCS", 45.0)
            );
            hubStateManager.initHub(chargers, 0.0, 0.0);

            // Disattivo charger-2
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", false, true, 0.0),
                    generateUpdate("charger-2", false, false, 0.0),
                    generateUpdate("charger-3", false, true, 0.0)
            );
            hubStateManager.updateHubState(updates);

            // Resetto i mock per verificare correttamente i test
            reset(eventPublisher, restClient);
        }

        @Test
        void updateFromSimulation_withValidUpdate_shouldUpdateChargerAndPublishEvent() {
            // Costruisco l'aggiornamento dal simulatore
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", true, true, 10.0)
            );

            // Ricevo ed elaboro l'aggiornamento
            hubStateManager.updateHubState(updates);

            // Verifico che il nuovo stato del connettore corrisponda a quello ricevuto
            Charger charger = hubStateManager.getCharger("charger-1");
            assertThat(charger.getCurrentPowerInUse()).isEqualTo(10.0);
            assertThat(charger.isOccupied()).isTrue();

            // Verifico che l'evento di cambio stato sia stato generato
            ArgumentCaptor<ChargerMetricsChangedEvent> captor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            // Controllo che l'evento contenga 1 cambiamento di stato e il timestamp corrisponde all'aggiornamento della simulazione
            // che ha provocato il cambiamento
            ChargerMetricsChangedEvent event = captor.getValue();
            assertThat(event.getNewChargerStates()).hasSize(1);
            assertThat(event.getNewChargerStates().getFirst().getChargerId()).isEqualTo("charger-1");
        }

        @Test
        void updateFromSimulation_withNoChanges_shouldNotPublishEvent() {
            // Inizializzo lo stato dell'hub
            List<ChargerStateUpdateDTO> initialUpdate = List.of(
                    generateUpdate("charger-1", true, true, 10.0)
            );
            hubStateManager.updateHubState(initialUpdate);
            reset(eventPublisher);

            // Testo la ricezione di uno stato dal simulatore identico a quello mantenuto
            hubStateManager.updateHubState(initialUpdate);

            // Verifico che non vengano generati eventi di cambio stato
            verify(eventPublisher, never()).publishEvent(any(ChargerMetricsChangedEvent.class));
        }

        @Test
        void updateFromSimulation_withUnknownCharger_shouldLogWarningAndContinue() {
            // Costruisco l'aggiornamento dal simulatore
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", true, true, 10.0),
                    generateUpdate("unknown-charger", true, true, 15.0)
            );

            // Elaboro l'aggiornamento
            hubStateManager.updateHubState(updates);

            // Verifico che l'aggiornamento per "charger-1" venga elaborato, mentre quello per "unknown-charger" venga scartato
            assertThat(hubStateManager.getHubState().hasCharger("charger-1")).isTrue();
            assertThat(hubStateManager.getHubState().hasCharger("unknown-charger")).isFalse();

            ArgumentCaptor<ChargerMetricsChangedEvent> captor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().getNewChargerStates()).hasSize(1);
        }

        @Test
        void updateFromSimulation_withInactiveCharger_shouldNotUpdateState() {
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-2", true, false, 20.0)
            );

            hubStateManager.updateHubState(updates);

            // Verifico che l'aggiornamento venga ignorato essendo il connettore disattivo
            Charger charger = hubStateManager.getCharger("charger-2");
            assertThat(charger.getCurrentPowerInUse()).isEqualTo(0.0);
            assertThat(charger.isOccupied()).isFalse();

            verify(eventPublisher, never()).publishEvent(any(ChargerMetricsChangedEvent.class));
        }

        @Test
        void updateFromSimulation_withMultipleChargers_shouldUpdateAllAndPublishSingleEvent() {
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", true, true, 19.0),
                    generateUpdate("charger-3", true, true, 21.5)
            );

            hubStateManager.updateHubState(updates);

            Charger charger1 = hubStateManager.getCharger("charger-1");
            Charger charger3 = hubStateManager.getCharger("charger-3");

            assertThat(charger1.getCurrentPowerInUse()).isEqualTo(19.0);
            assertThat(charger1.isOccupied()).isTrue();
            assertThat(charger3.getCurrentPowerInUse()).isEqualTo(21.5);
            assertThat(charger3.isOccupied()).isTrue();

            ArgumentCaptor<ChargerMetricsChangedEvent> captor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(captor.capture());
            assertThat(captor.getValue().getNewChargerStates()).hasSize(2);
        }

        @Test
        void updateFromSimulation_shouldUpdateHubMetrics() {
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", true, true, 19.0),
                    generateUpdate("charger-3", true, true, 21.5)
            );

            hubStateManager.updateHubState(updates);

            Hub hub = hubStateManager.getHubState();
            HubMetrics metrics = hub.getHubMetrics();

            assertThat(metrics.getActiveChargers()).isEqualTo(2);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(2);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(19.0 + 21.5);
        }
    }

    /* ========================================================
       Verifica aggiornamento hub accensione/spegnimento connettore
       ======================================================== */

    @Nested
    class UpdateOperationalStateTest {
        private ChargerStateUpdateDTO generateUpdate(String chargerId, boolean occupied, boolean active, double currentPowerInUse) {
            StateDTO state = new StateDTO(active, occupied, currentPowerInUse,
                    StateDTO.HealthStatus.HEALTY, 0.0, "00:00:00");
            return new ChargerStateUpdateDTO(chargerId, state, null, null);
        }

        @BeforeEach
        void initializeHub() {
            List<ChargerDTO> chargers = List.of(
                    new ChargerDTO("charger-1", "AC", 23.0),
                    new ChargerDTO("charger-2", "CCS", 50.0),
                    new ChargerDTO("charger-3", "CCS", 45.0)
            );
            hubStateManager.initHub(chargers, 0.0, 0.0);

            // Disattivo charger-2
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", false, true, 0.0),
                    generateUpdate("charger-2", false, false, 0.0),
                    generateUpdate("charger-3", false, true, 0.0)
            );
            hubStateManager.updateHubState(updates);

            // Resetto i mock per verificare correttamente i test
            reset(eventPublisher, restClient);
        }

        @Test
        void updateOperationalState_validTransition_shouldUpdateAndPublishEvent() {
            hubStateManager.updateChargerOperationalState("charger-1", ChargerOperationalState.IN_DEACTIVATION);

            // Verifico il cambiamento di stato del connettore
            Charger charger = hubStateManager.getCharger("charger-1");
            assertThat(charger.getOperationalState()).isEqualTo(ChargerOperationalState.IN_DEACTIVATION);

            // Verifico che sia stata effettuata la chiamata api verso il simulatore per disattivare il connettore
            verify(restClient).changeChargerOperationalState("charger-1", false);

            // Verifico che sia stato generato l'evento di cambio stato
            ArgumentCaptor<ChargerOperationalStateChangedEvent> captor =
                    ArgumentCaptor.forClass(ChargerOperationalStateChangedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            // Verifico il contenuto dell'evento (solo "charger-3" attivo e potenza max hub erogabile pari a 45.0 di "charger-3")
            ChargerOperationalStateChangedEvent event = captor.getValue();
            assertThat(event.getChargerId()).isEqualTo("charger-1");
            assertThat(event.getChargerOperationalState()).isEqualTo(ChargerOperationalState.IN_DEACTIVATION);
            assertThat(event.getNextActiveChargers()).isEqualTo(1);
            assertThat(event.getNextAvailableMaxPower()).isEqualTo(45.0);
        }

        @Test
        void updateOperationalState_sameState_shouldThrowException() {
            assertThatThrownBy(() ->
                    hubStateManager.updateChargerOperationalState("charger-1", ChargerOperationalState.IN_ACTIVATION)
            )
                    .isInstanceOf(InvalidChargerStateTransitionException.class)
                    .hasMessageContaining("Transizione non valida");

            verify(restClient, never()).changeChargerOperationalState(anyString(), anyBoolean());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void updateOperationalState_unknownCharger_shouldThrowException() {
            assertThatThrownBy(() ->
                    hubStateManager.updateChargerOperationalState("unknown-charger", ChargerOperationalState.IN_ACTIVATION)
            )
                    .isInstanceOf(ChargerNotFoundException.class)
                    .hasMessageContaining("Nessun connettore con id unknown-charger");
        }

        @Test
        void updateOperationalState_restClientException_shouldRollback() {
            // Simulo il lancio dell'eccezione RestClientException quando viene eseguita la chiamata api
            doThrow(new RemoteCommandException("Il simulatore ha incontrato un errore durante l'aggiornamento del connettore", ApplicationErrorCode.COMMAND_EXECUTION_ERROR))
                    .when(restClient).changeChargerOperationalState(anyString(), anyBoolean());

            assertThatThrownBy(() ->
                    hubStateManager.updateChargerOperationalState("charger-1", ChargerOperationalState.IN_DEACTIVATION)
            )
                    .isInstanceOf(RemoteCommandException.class)
                    .hasMessageContaining("Il simulatore ha incontrato un errore durante l'aggiornamento del connettore");

            // Verifico ripristino stato precedente
            Charger charger = hubStateManager.getCharger("charger-1");
            assertThat(charger.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void updateOperationalState_deactivateLastActive_shouldThrowException() {
            // Imposto charger-3 come unico connettore attivo
            hubStateManager.updateChargerOperationalState("charger-1", ChargerOperationalState.IN_DEACTIVATION);
            reset(restClient, eventPublisher);

            // Verifico che non sia possibile avere tutti i connettori disattivi
            assertThatThrownBy(() ->
                    hubStateManager.updateChargerOperationalState("charger-3", ChargerOperationalState.IN_DEACTIVATION)
            )
                    .isInstanceOf(InvalidChargerStateTransitionException.class)
                    .hasMessageContaining("Impossibile disattivare il connettore: almeno un connettore dell'hub deve essere attivo");
        }

        @Test
        void updateOperationalState_deactivateWithOtherActive_shouldSucceed() {
            // Verifico che charger-1 sia disattivabile avendo charger-3 attivo
            hubStateManager.updateChargerOperationalState("charger-1", ChargerOperationalState.IN_DEACTIVATION);

            // Verifico che charger-1 venga disattivato, l'evento di cambio stato generato e il simulatore notificato tramite api
            Charger charger1 = hubStateManager.getCharger("charger-1");
            Charger charger2 = hubStateManager.getCharger("charger-2");
            Charger charger3 = hubStateManager.getCharger("charger-3");

            assertThat(charger1.getOperationalState()).isEqualTo(ChargerOperationalState.IN_DEACTIVATION);
            assertThat(charger1.getCurrentPowerInUse()).isEqualTo(0.0);
            assertThat(charger1.isOccupied()).isFalse();

            assertThat(charger2.getOperationalState()).isEqualTo(ChargerOperationalState.INACTIVE);
            assertThat(charger3.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);

            verify(restClient).changeChargerOperationalState("charger-1", false);
            verify(eventPublisher).publishEvent(any(ChargerOperationalStateChangedEvent.class));
        }

        @Test
        void updateOperationalState_shouldUpdateHubMetricsCorrectly() {
            // Attivo charger-2
            hubStateManager.updateChargerOperationalState("charger-2", ChargerOperationalState.IN_ACTIVATION);

            // Verifico che e metriche dell'hub rimangano invariate
            HubMetrics afterMetrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(afterMetrics.getActiveChargers()).isEqualTo(2);
            assertThat(afterMetrics.getCurrentPowerInUse()).isEqualTo(0.0);
            assertThat(afterMetrics.getCurrentMaxPower()).isEqualTo(23.0 + 45.0);
        }
    }

    /* ========================================================
       Verifica lancio eccezioni getter custom
       ======================================================== */

    @Nested
    class CustomGetMethodsExceptionTest {

        @Test
        void getHubState_whenNotInitialized_shouldThrowException() {
            assertThatThrownBy(() -> hubStateManager.getHubState())
                    .isInstanceOf(HubNotInitializedException.class)
                    .hasMessageContaining("Hub non ancora inizializzato");
        }

        @Test
        void getCharger_unknownCharger_shouldThrowException() {
            List<ChargerDTO> chargers = List.of(
                    new ChargerDTO("charger-1", "AC", 23.0)
            );
            hubStateManager.initHub(chargers, 0.0, 0.0);

            assertThatThrownBy(() -> hubStateManager.getCharger("unknown-charger"))
                    .isInstanceOf(ChargerNotFoundException.class)
                    .hasMessageContaining("Nessun connettore con id");
        }

    }

    @Nested
    class UpdateStateConsistencyTest {
        private ChargerStateUpdateDTO generateUpdate(String chargerId, boolean occupied, boolean active, double currentPowerInUse) {
            StateDTO state = new StateDTO(active, occupied, currentPowerInUse,
                    StateDTO.HealthStatus.HEALTY, 0.0, "00:00:00");
            return new ChargerStateUpdateDTO(chargerId, state, null, null);
        }

        @BeforeEach
        void initializeHub() {
            List<ChargerDTO> chargers = List.of(
                    new ChargerDTO("charger-1", "AC", 23.0),
                    new ChargerDTO("charger-2", "CCS", 50.0),
                    new ChargerDTO("charger-3", "CCS", 45.0),
                    new ChargerDTO("charger-4", "AC", 10.0),
                    new ChargerDTO("charger-5", "CCS", 100.0)
            );
            hubStateManager.initHub(chargers, 0.0, 0.0);

            // Resetto i mock per verificare correttamente i test
            reset(eventPublisher, restClient);
        }

        @Test
        void rapidUpdates() {
            // Simula 100 aggiornamenti rapidi con valori alternati
            for (int i = 0; i < 100; i++) {
                double power = (i % 2 == 0) ? 20.0 : 15.0;
                boolean occupied = (i % 2 == 0);

                List<ChargerStateUpdateDTO> updates = List.of(
                        generateUpdate("charger-1", occupied, true, power)
                );

                hubStateManager.updateHubState(updates);

                // Verifica coerenza ad ogni step
                HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
                Charger charger = hubStateManager.getCharger("charger-1");

                assertThat(charger.getCurrentPowerInUse()).isEqualTo(power);
                assertThat(charger.isOccupied()).isEqualTo(occupied);
                assertThat(metrics.getCurrentPowerInUse()).isEqualTo(power);
                assertThat(metrics.getOccupiedChargers()).isEqualTo(occupied ? 1 : 0);
            }

            // Verifica che siano stati pubblicati 100 eventi (tutti diversi)
            verify(eventPublisher, times(100)).publishEvent(any(ChargerMetricsChangedEvent.class));
        }

        @Test
        void complexLifecycle_multipleStateChanges() {
            doNothing().when(restClient).changeChargerOperationalState(anyString(), anyBoolean());

            // Primo aggiornamento dalla simulazione, i connettori non sono in uso
            step_1();

            // charger-1 e charger-3 diventano in uso nella simulazione
            step_2();

            // Simulo aumento erogazione potenza charger-1 e inizio ricarica charger-5
            step_3();

            // Simulo fine erogazione potenza per charger-1
            step_4();

            // Nessun cambiamento
            step_5();

            // Spegnimento charger-3
            step_6();
        }

        void step_1() {
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", false, true, 0),
                    generateUpdate("charger-2", false, false, 0),
                    generateUpdate("charger-3", false, true, 0),
                    generateUpdate("charger-4", false, true, 0),
                    generateUpdate("charger-5", false, true, 0)
            );
            hubStateManager.updateHubState(updates);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(4);
            assertThat(metrics.getOccupiedChargers()).isZero();
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(23 + 45 + 10 + 100);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(0.0);

            // Verifico la generazione dell'evento per il cambiamento di stato (5 stati modificati)
            ArgumentCaptor<ChargerMetricsChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerMetricsChangedEvent event = eventCaptor.getValue();
            // Verifico che il messaggio contenga gli stati corretti
            assertThat(event.getNewChargerStates()).hasSize(5);
            assertThat(event.getNewChargerStates())
                    .extracting(ChargerMetricsChange::getChargerId)
                    .containsExactlyInAnyOrder("charger-1", "charger-2", "charger-3", "charger-4", "charger-5");
            // Verifico che le metriche dell'hub notificate corrispondano a quelle in memoria
            assertThat(event.getAggregatedHubMetrics().getActiveChargers()).isEqualTo(4);
            assertThat(event.getAggregatedHubMetrics().getOccupiedChargers()).isZero();
            assertThat(event.getAggregatedHubMetrics().getCurrentMaxPower()).isEqualTo(23 + 45 + 10 + 100);
            assertThat(event.getAggregatedHubMetrics().getCurrentPowerInUse()).isEqualTo(0.0);
            // Verifico che il timestamp sia corretto
            assertThat(event.getSimulationTimestamp()).isEqualTo(0.0);
            reset(eventPublisher);
        }

        void step_2() {
            // Simulo inizio ricarica per charger-1 e charger-3
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", true, true, 20.0),
                    generateUpdate("charger-3", true, true, 40.0),
                    generateUpdate("charger-4", false, true, 0.0) // Nessun cambiamento
            );
            hubStateManager.updateHubState(updates);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(4);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(2);
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(23 + 45 + 10 + 100);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(20.0 + 40.0);

            // Verifico lo stato individuale dei charger modificati
            Charger c1 = hubStateManager.getCharger("charger-1");
            assertThat(c1.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
            assertThat(c1.isOccupied()).isTrue();
            assertThat(c1.getCurrentPowerInUse()).isEqualTo(20.0);

            Charger c3 = hubStateManager.getCharger("charger-3");
            assertThat(c3.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
            assertThat(c3.isOccupied()).isTrue();
            assertThat(c3.getCurrentPowerInUse()).isEqualTo(40.0);

            // Verifico che charger-4 non sia cambiato
            Charger c4 = hubStateManager.getCharger("charger-4");
            assertThat(c4.isOccupied()).isFalse();
            assertThat(c4.getCurrentPowerInUse()).isEqualTo(0.0);

            // Verifico la generazione dell'evento (solo charger-1 e charger-3 modificati)
            ArgumentCaptor<ChargerMetricsChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerMetricsChangedEvent event = eventCaptor.getValue();

            // Verifico che il messaggio contenga solo i 2 connettori modificati
            assertThat(event.getNewChargerStates()).hasSize(2);
            assertThat(event.getNewChargerStates())
                    .extracting(ChargerMetricsChange::getChargerId)
                    .containsExactlyInAnyOrder("charger-1", "charger-3");

            // Verifico che gli stati notificati siano corretti
            assertThat(event.getNewChargerStates())
                    .filteredOn(c -> c.getChargerId().equals("charger-1"))
                    .first()
                    .satisfies(c -> {
                        assertThat(c.getCurrentPower()).isEqualTo(20.0);
                        assertThat(c.isOccupied()).isTrue();
                        assertThat(c.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
                    });

            // Verifico che le metriche dell'hub notificate corrispondano a quelle in memoria
            assertThat(event.getAggregatedHubMetrics().getActiveChargers()).isEqualTo(4);
            assertThat(event.getAggregatedHubMetrics().getOccupiedChargers()).isEqualTo(2);
            assertThat(event.getAggregatedHubMetrics().getCurrentMaxPower()).isEqualTo(178.0);
            assertThat(event.getAggregatedHubMetrics().getCurrentPowerInUse()).isEqualTo(60.0);

            reset(eventPublisher);
        }

        void step_3() {
            // Simulo aumento erogazione potenza charger-1 e inizio ricarica charger-5
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", true, true, 23.0), // Max potenza
                    generateUpdate("charger-3", true, true, 40.0), // Nessun cambiamento
                    generateUpdate("charger-5", true, true, 80.0)  // Inizio ricarica
            );
            hubStateManager.updateHubState(updates);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(4);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(3);
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(178.0);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(23.0 + 40.0 + 80.0);

            // Verifico lo stato dei connettori in memoria
            Charger c1 = hubStateManager.getCharger("charger-1");
            assertThat(c1.getCurrentPowerInUse()).isEqualTo(23.0);
            assertThat(c1.isOccupied()).isTrue();

            Charger c5 = hubStateManager.getCharger("charger-5");
            assertThat(c5.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
            assertThat(c5.isOccupied()).isTrue();
            assertThat(c5.getCurrentPowerInUse()).isEqualTo(80.0);

            Charger c3 = hubStateManager.getCharger("charger-3");
            assertThat(c3.getCurrentPowerInUse()).isEqualTo(40.0);
            assertThat(c3.isOccupied()).isTrue();

            // Verifico la generazione dell'evento (solo charger-1 e charger-5 modificati)
            ArgumentCaptor<ChargerMetricsChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerMetricsChangedEvent event = eventCaptor.getValue();

            // Verifico che il messaggio contenga solo i 2 charger modificati
            assertThat(event.getNewChargerStates()).hasSize(2);
            assertThat(event.getNewChargerStates())
                    .extracting(ChargerMetricsChange::getChargerId)
                    .containsExactlyInAnyOrder("charger-1", "charger-5");

            // Verifico che le metriche dell'hub notificate corrispondano a quelle in memoria
            assertThat(event.getAggregatedHubMetrics().getActiveChargers()).isEqualTo(4);
            assertThat(event.getAggregatedHubMetrics().getOccupiedChargers()).isEqualTo(3);
            assertThat(event.getAggregatedHubMetrics().getCurrentMaxPower()).isEqualTo(178.0);
            assertThat(event.getAggregatedHubMetrics().getCurrentPowerInUse()).isEqualTo(143.0);

            reset(eventPublisher);
        }

        void step_4() {
            // Simulo fine utilizzo charger-1
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", false, true, 0.0), // Finisce ricarica
                    generateUpdate("charger-3", true, true, 40.0), // Continua
                    generateUpdate("charger-5", true, true, 80.0)  // Continua
            );
            hubStateManager.updateHubState(updates);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(4);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(2);
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(178.0);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(40.0 + 80.0);

            // Verifico lo stato dei connettori in memoria
            Charger c1 = hubStateManager.getCharger("charger-1");
            assertThat(c1.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
            assertThat(c1.isOccupied()).isFalse();
            assertThat(c1.getCurrentPowerInUse()).isEqualTo(0.0);

            Charger c3 = hubStateManager.getCharger("charger-3");
            assertThat(c3.isOccupied()).isTrue();
            assertThat(c3.getCurrentPowerInUse()).isEqualTo(40.0);

            Charger c5 = hubStateManager.getCharger("charger-5");
            assertThat(c5.isOccupied()).isTrue();
            assertThat(c5.getCurrentPowerInUse()).isEqualTo(80.0);

            // Verifico la generazione dell'evento (solo charger-1 modificato)
            ArgumentCaptor<ChargerMetricsChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerMetricsChangedEvent event = eventCaptor.getValue();

            // Verifico che il messaggio contenga solo charger-1
            assertThat(event.getNewChargerStates()).hasSize(1);
            assertThat(event.getNewChargerStates())
                    .extracting(ChargerMetricsChange::getChargerId)
                    .containsExactly("charger-1");

            // Verifico che lo stato notificato corrisponda a quello in memoria
            assertThat(event.getNewChargerStates().getFirst().getCurrentPower()).isEqualTo(0.0);
            assertThat(event.getNewChargerStates().getFirst().isOccupied()).isFalse();

            // Verifico che le metriche dell'hub notificate corrispondano a quelle in memoria
            assertThat(event.getAggregatedHubMetrics().getActiveChargers()).isEqualTo(4);
            assertThat(event.getAggregatedHubMetrics().getOccupiedChargers()).isEqualTo(2);
            assertThat(event.getAggregatedHubMetrics().getCurrentMaxPower()).isEqualTo(178.0);
            assertThat(event.getAggregatedHubMetrics().getCurrentPowerInUse()).isEqualTo(120.0);

            reset(eventPublisher);
        }

        void step_5() {
            // Simulo update senza cambiamenti
            List<ChargerStateUpdateDTO> updates = List.of(
                    generateUpdate("charger-1", false, true, 0.0),
                    generateUpdate("charger-3", true, true, 40.0),
                    generateUpdate("charger-4", false, true, 0.0),
                    generateUpdate("charger-5", true, true, 80.0)
            );
            hubStateManager.updateHubState(updates);

            // Verifico che le metriche aggregate dell'hub siano rimaste invariate
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(4);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(2);
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(178.0);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(120.0);

            // Verifico che NON sia stato pubblicato alcun evento
            verify(eventPublisher, never()).publishEvent(any(ChargerMetricsChangedEvent.class));
        }

        void step_6() {
            doNothing().when(restClient).changeChargerOperationalState(anyString(), anyBoolean());

            // Disattivo manualmente charger-3
            hubStateManager.updateChargerOperationalState("charger-3", ChargerOperationalState.IN_DEACTIVATION);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(4); // 1, 3, 4, 5
            assertThat(metrics.getOccupiedChargers()).isEqualTo(2); // Solo 5
            assertThat(metrics.getCurrentMaxPower()).isEqualTo(23 + 45 + 10 + 100);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(40.0 + 80.0); // Solo charger-5

            // Verifico che charger-3 sia portato nello stato transitiorio di disattivazione e il resto dello stato sia invariato
            Charger c3 = hubStateManager.getCharger("charger-3");
            assertThat(c3.getOperationalState()).isEqualTo(ChargerOperationalState.IN_DEACTIVATION);
            assertThat(c3.isOccupied()).isTrue();
            assertThat(c3.getCurrentPowerInUse()).isEqualTo(40.0);

            // Verifico che gli altri charger non siano stati modificati
            Charger c1 = hubStateManager.getCharger("charger-1");
            assertThat(c1.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);

            Charger c5 = hubStateManager.getCharger("charger-5");
            assertThat(c5.isOccupied()).isTrue();
            assertThat(c5.getCurrentPowerInUse()).isEqualTo(80.0);

            // Verifico la generazione dell'evento di cambio stato
            ArgumentCaptor<ChargerOperationalStateChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerOperationalStateChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerOperationalStateChangedEvent event = eventCaptor.getValue();

            // Verifico che i dati notificati siano corretti
            assertThat(event.getChargerId()).isEqualTo("charger-3");
            assertThat(event.getChargerOperationalState()).isEqualTo(ChargerOperationalState.IN_DEACTIVATION);
            assertThat(event.getNextActiveChargers()).isEqualTo(3);
            assertThat(event.getNextAvailableMaxPower()).isEqualTo(133.0);

            reset(eventPublisher, restClient);
        }


        /*
// ========================================================================
// STEP 7: Inizio ricarica charger-4
// ========================================================================

        @Test
        void step7_anotherChargingStart_shouldUpdateOneCharger() {
            step6_manualDeactivation_shouldResetChargerState();
            reset(eventPublisher);

            // Simulo inizio ricarica charger-4
            Map<String, ChargerStatus> update = Map.of(
                    "charger-1", generateUpdate("charger-1", false, true, 0.0),
                    "charger-4", generateUpdate("charger-4", true, true, 10.0), // Inizia (max potenza)
                    "charger-5", generateUpdate("charger-5", true, true, 80.0)  // Continua
            );
            hubStateManager.updateFromSimulation(update, 5.0);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(3);
            assertThat(metrics.getOccupiedChargers()).isEqualTo(2); // 4 e 5
            assertThat(metrics.getCurrentHubMaxPower()).isEqualTo(133.0);
            assertThat(metrics.getCurrentPowerInUse()).isEqualTo(10.0 + 80.0);

            // Verifico lo stato individuale del charger modificato
            Charger c4 = hubStateManager.getCharger("charger-4");
            assertThat(c4.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);
            assertThat(c4.isOccupied()).isTrue();
            assertThat(c4.getCurrentPowerInUse()).isEqualTo(10.0);

            // Verifico la generazione dell'evento (solo charger-4 modificato)
            ArgumentCaptor<ChargerMetricsChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerMetricsChangedEvent event = eventCaptor.getValue();

            // Verifico che il messaggio contenga solo charger-4
            assertThat(event.getNewChargerStates()).hasSize(1);
            assertThat(event.getNewChargerStates())
                    .extracting(ChargerMetricsChange::chargerId)
                    .containsExactly("charger-4");

            // Verifico che le metriche dell'hub notificate corrispondano a quelle in memoria
            assertThat(event.getAggregatedHubMetrics().getActiveChargers()).isEqualTo(3);
            assertThat(event.getAggregatedHubMetrics().getOccupiedChargers()).isEqualTo(2);
            assertThat(event.getAggregatedHubMetrics().getCurrentPowerInUse()).isEqualTo(90.0);

            // Verifico che il timestamp sia corretto
            assertThat(event.getSimulationTimestamp()).isEqualTo(5.0);
        }

// ========================================================================
// STEP 8: Fine ricarica per tutti i charger
// ========================================================================

        @Test
        void step8_allChargersEndCharging_shouldUpdateTwoChargers() {
            step7_anotherChargingStart_shouldUpdateOneCharger();
            reset(eventPublisher);

            // Simulo fine ricarica per tutti i charger
            Map<String, ChargerStatus> update = Map.of(
                    "charger-1", generateUpdate("charger-1", false, true, 0.0),
                    "charger-4", generateUpdate("charger-4", false, true, 0.0),
                    "charger-5", generateUpdate("charger-5", false, true, 0.0)
            );
            hubStateManager.updateFromSimulation(update, 6.0);

            // Verifico le metriche aggregate dell'hub in memoria
            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(3);
            assertThat(metrics.getOccupiedChargers()).isZero();
            assertThat(metrics.getCurrentHubMaxPower()).isEqualTo(133.0);
            assertThat(metrics.getCurrentPowerInUse()).isZero();

            // Verifico lo stato individuale dei charger modificati
            Charger c4 = hubStateManager.getCharger("charger-4");
            assertThat(c4.isOccupied()).isFalse();
            assertThat(c4.getCurrentPowerInUse()).isEqualTo(0.0);

            Charger c5 = hubStateManager.getCharger("charger-5");
            assertThat(c5.isOccupied()).isFalse();
            assertThat(c5.getCurrentPowerInUse()).isEqualTo(0.0);

            // Verifico la generazione dell'evento (charger-4 e charger-5 modificati)
            ArgumentCaptor<ChargerMetricsChangedEvent> eventCaptor =
                    ArgumentCaptor.forClass(ChargerMetricsChangedEvent.class);
            verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

            ChargerMetricsChangedEvent event = eventCaptor.getValue();

            // Verifico che il messaggio contenga i 2 charger modificati
            assertThat(event.getNewChargerStates()).hasSize(2);
            assertThat(event.getNewChargerStates())
                    .extracting(ChargerMetricsChange::chargerId)
                    .containsExactlyInAnyOrder("charger-4", "charger-5");

            // Verifico che le metriche dell'hub notificate corrispondano a quelle in memoria
            assertThat(event.getAggregatedHubMetrics().getActiveChargers()).isEqualTo(3);
            assertThat(event.getAggregatedHubMetrics().getOccupiedChargers()).isZero();
            assertThat(event.getAggregatedHubMetrics().getCurrentPowerInUse()).isZero();

            // Verifico che il timestamp sia corretto
            assertThat(event.getSimulationTimestamp()).isEqualTo(6.0);
        }

// ========================================================================
// STEP 9: Disattivazione manuale charger-4 e charger-5
// ========================================================================

        @Test
        void step9_deactivateTwoChargers_shouldLeaveOnlyOne() {
            step8_allChargersEndCharging_shouldUpdateTwoChargers();
            reset(eventPublisher);

            when(apiClient.changeChargerOperationalState(anyString(), eq(false))).thenReturn(null);

            // Disattivo charger-4
            hubStateManager.updateChargerOperationalState("charger-4", ChargerOperationalState.INACTIVE);

            HubMetrics metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(2); // 1, 5
            assertThat(metrics.getCurrentHubMaxPower()).isEqualTo(23 + 100);

            // Disattivo charger-5
            hubStateManager.updateChargerOperationalState("charger-5", ChargerOperationalState.INACTIVE);

            metrics = hubStateManager.getHubState().getHubMetrics();
            assertThat(metrics.getActiveChargers()).isEqualTo(1); // Solo 1
            assertThat(metrics.getOccupiedChargers()).isZero();
            assertThat(metrics.getCurrentHubMaxPower()).isEqualTo(23.0);
            assertThat(metrics.getCurrentPowerInUse()).isZero();

            // Verifico lo stato individuale dei charger disattivati
            Charger c4 = hubStateManager.getCharger("charger-4");
            assertThat(c4.getOperationalState()).isEqualTo(ChargerOperationalState.INACTIVE);

            Charger c5 = hubStateManager.getCharger("charger-5");
            assertThat(c5.getOperationalState()).isEqualTo(ChargerOperationalState.INACTIVE);

            // Verifico che charger-1 sia ancora attivo
            Charger c1 = hubStateManager.getCharger("charger-1");
            assertThat(c1.getOperationalState()).isEqualTo(ChargerOperationalState.ACTIVE);

            // Verifico che siano stati pubblicati 2 eventi
            verify(eventPublisher, times(2)).publishEvent(any(ChargerOperationalStateChangedEvent.class));
        }

// ========================================================================
// STEP 10: Verifica stato finale completo
// ========================================================================

        @Test
        void step10_finalStateVerification_shouldMatchExpectedState() {
            step9_deactivateTwoChargers_shouldLeaveOnlyOne();

            Hub finalHub = hubStateManager.getHubState();

            // Verifico singoli charger
            Charger c1 = finalHub.getCharger("charger-1");

         */

    }
}