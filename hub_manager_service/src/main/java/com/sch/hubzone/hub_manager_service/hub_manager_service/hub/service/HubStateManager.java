package com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.error.ApplicationErrorCode;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Hub;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.StateChangeDelta;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.ChargerMetricsChangedEvent;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.ChargerOperationalStateChangedEvent;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.event.data.ChargerMetricsChange;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.ChargerNotFoundException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.HubNotInitializedException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.InvalidChargerStateTransitionException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.hub.service.exception.RemoteCommandException;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.ChargerManagerRestClient;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.ChargerStateUpdateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.CommandResponseDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.charger.dto.StateDTO;
import com.sch.hubzone.hub_manager_service.hub_manager_service.integration.input.simulation.dto.payload.ChargerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Implementa la logia di business legata alla gestione dello stato dell'hub in memoria. Non essendo prevista per ora la
 * persistenza dell'hub stesso, il service mantiene sia la struttura dell'hub (parte statica) che il suo stato (parte dinamica).
 * <p></p>
 *
 */
@Slf4j
@Service
public class HubStateManager {

    private final ChargerManagerRestClient chargerManagerRestClient;
    private final ApplicationEventPublisher eventPublisher;
    private Hub hub = null;

    public HubStateManager(ApplicationEventPublisher eventPublisher, ChargerManagerRestClient chargerManagerRestClient) {
        this.eventPublisher = eventPublisher;
        this.chargerManagerRestClient = chargerManagerRestClient;
    }

    /**
     * Inizializza l'hub con i connettori ricevuti dal simulatore. Nello specifico popola la parte legata alla struttura
     * dell'hub.
     *
     * @param chargers lista di {@link ChargerDTO} ottenuta dal simulatore
     */
    public synchronized void initHub(List<ChargerDTO> chargers, Double latitude, Double longitude) {
        hub = new Hub();

        for (ChargerDTO dto : chargers) {
            hub.addCharger(dto.getChargerId(), dto.getChargerType(), dto.getPlugPowerKw());
        }

        hub.setPosition(latitude, longitude);

        log.info("Hub inizializzato con {} connettori", chargers.size());
    }

    /**
     * Aggiorna lo stato corrente dei connettori con il nuovo stato ricevuto dal simulatore. Lo stato verrà aggiornato se:
     * <ul>
     *     <li>L'hub è stato inizializzato;</li>
     *     <li>Lo stato operativo del connettore accetta l'aggiornamento ({@link ChargerOperationalState});</li>
     *     <li>Il nuovo stato differisce da quello correntemente mantenuto.</li>
     * </ul>
     *
     * Il confronto sulla differenza di stato e il suo aggiornamento avvengono rispetto all'occupazione del connettore e
     * alla potenza che sta erogando al momento.
     * Se il connettore si trova in uno stato operativo transitorio ({@code IN_DEACTIVATION} o {@code IN_ACTIVATION}),
     * confronto e aggiornamento vengono estesi anche rispetto a questo parametro.
     * <p>
     * Nel caso venga aggiornato almeno uno stato, pubblica un evento interno {@link ChargerMetricsChangedEvent}.
     *
     * @param update stati aggiornati dei connettori ricevuti dal simulatore
     */
    public synchronized void updateHubState(List<ChargerStateUpdateDTO> update) {
        List<ChargerMetricsChange> changes = new ArrayList<>();

        StateChangeDelta totalDelta = StateChangeDelta.noChanges();
        log.debug("Update fetched from ChargerManagers: {}", update);
        for (ChargerStateUpdateDTO dto : update) {
            String chargerId = dto.chargerId();

            if (!hub.hasCharger(chargerId)) {
                log.warn("Received update for non-existing charger: {}", chargerId);
                continue;
            }

            Charger oldState = null;

            if (dto.isFetchFailed()) {
              oldState = hub.updateChargerStateAsUnreachable(chargerId);
              log.warn("Charger {} is unreachable", chargerId);
            } else {
                StateDTO updatedState = dto.chargerState();

                if (updatedState.healthStatus().isFaulted()) {
                    oldState = hub.updateChargerStateAsFaulted(chargerId);
                    log.warn("Charger {} has returned faulted state", chargerId);
                } else if (updatedState.healthStatus().isInitialized()) {
                    oldState = hub.updateChargerState(chargerId, Math.round(updatedState.currentPower() * 100.0) / 100.0,
                            updatedState.occupied(), updatedState.active());
                }
            }

            Charger newState = hub.getCharger(chargerId);
            log.debug("Connettore {} stato aggiornato: {} ", chargerId, newState);
            log.debug("Connettore {} stato precedente: {} ", chargerId, oldState);
            if (oldState != null) {
                // Computo il delta del cambiamento di stato e lo aggiungo al delta totale dell'aggiornamento
                StateChangeDelta chargerDelta = StateChangeDelta.computeDelta(newState, oldState);
                totalDelta = StateChangeDelta.add(totalDelta, chargerDelta);

                log.debug("Connettore {} delta cambiamento: {} ", chargerId, chargerDelta);
                changes.add(new ChargerMetricsChange(
                        chargerId,
                        newState.getCurrentPowerInUse(),
                        newState.isOccupied(),
                        newState.getOperationalState()
                ));
            }
        }

        UpdateTimestamp time = extractCommonUpdateTime(update);
        hub.updateSimulationTimestamp(time.lastUpdateTime);
        hub.updateSimulationFormattedTimestamp(time.lastUpdateFormattedTime);

        // Aggiorno le metriche aggregato dell'hub e pubblico l'evento solo se ci sono stati cambiamenti
        if (!changes.isEmpty()) {
            hub.updateAggregatedMetrics(totalDelta);

            ChargerMetricsChangedEvent event = new ChargerMetricsChangedEvent(
                    hub.getHubMetrics(),
                    changes,
                    hub.getSimulationTimestamp(),
                    hub.getSimulationFormattedTimestamp()
            );

            log.debug("Evento cambiamento generato: {}", event);
            eventPublisher.publishEvent(event);

            log.info("Aggiornati {} connettori dalla simulazione", changes.size());
        } else {
            log.info("Nessun cambiamento di stato dalla simulazione");
        }

        log.debug("=".repeat(50));
    }

    /**
     * Verifica se possibile e cambia lo stato operativo del connettore specificato nello stato transitorio indicato
     * ({@code IN_DEACTIVATION} o {@code IN_ACTIVATION}).
     * Se la verifica locale consente la transizione di stato, comunica tale cambiamento al simulatore. In caso di risposta
     * positiva, lancia un evento {@link ChargerOperationalStateChangedEvent}; in caso di risposta negativa, effettua un rollback
     * dello stato per ripristinare lo stato operativo precedente del connettore.
     *
     * @param chargerId id del connettore a cui cambiare lo stato
     * @param transitionState stato operativo transitorio in cui portare il connettore
     *
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     * @throws ChargerNotFoundException se l'hub non contiene il connettore indicato
     */
    public synchronized void updateChargerOperationalState(String chargerId, ChargerOperationalState transitionState) {
        checkIfHubIsInitialized();
        checkIfChargerExists(chargerId);

        if (!checkIfChargerIsOperative(chargerId))
            throw new InvalidChargerStateTransitionException("Impossibile attivate/disattivare il connettore: " +
                    "attualmente risulta in uno stato di errore", ApplicationErrorCode.CHARGER_IS_IN_ERROR_STATE);

        log.debug("Ricevuta richiesta transizione stato operativo per connettore {} a stato {}", chargerId, transitionState);
        Charger currentState = hub.getCharger(chargerId);
        ChargerOperationalState previousState = currentState.getOperationalState();

        if (!transitionState.willContributeToPower() && !hub.canDeactivateChargers()) {
            log.debug("Transizione impossibile, almeno 1 connettore nell'hub deve restare attivo: {}", getHubState());
            throw new InvalidChargerStateTransitionException(
                    "Impossibile disattivare il connettore: almeno un connettore dell'hub deve essere attivo",
                    ApplicationErrorCode.NOT_ENOUGH_ACTIVE_CHARGER
            );
        }

        // Eseguo l'aggiornamento locale
        Charger oldState = hub.updateChargerOperationalState(chargerId, transitionState);

        if (oldState == null) {
            log.debug("Transizione impossibile da stato {} a stato {}: {}", previousState, transitionState, getHubState());
            throw new InvalidChargerStateTransitionException(
                    "Transizione non valida da '%s' a '%s'".formatted(previousState, transitionState),
                    ApplicationErrorCode.INVALID_TRANSITION_STATE
            );
        }

        // Comunico il cambiamento al simulatore
        log.debug("Invio richiesta a simulatore");
        boolean requestActivation = transitionState.willContributeToPower();
        try {
            CommandResponseDTO result = chargerManagerRestClient.changeChargerOperationalState(chargerId, requestActivation);

            if (!result.success()) {
                restorePreviousState(chargerId, oldState);
                throw new RemoteCommandException(result.errorMessage(), ApplicationErrorCode.COMMAND_EXECUTION_ERROR);
            }

            log.info("Stato operativo connettore {} cambiato: {} → {}",
                    chargerId, previousState, transitionState);

        } catch (RemoteCommandException e) {
            log.error("Errore nella comunicazione api con il simulatore", e);
            restorePreviousState(chargerId, oldState);
            throw e;
        }

        // L'aggiornamento delle metriche viene fatto solo in updateFromSimulation per rimanere coerenti con la simulazione
        // Notifico al backend le metriche aggregate future
        Charger charger = getCharger(chargerId);
        int newActiveChargers =
                hub.getHubMetrics().getActiveChargers() + (requestActivation ? 1 : -1);
        double newCurrentMaxPower =
                hub.getHubMetrics().getCurrentMaxPower() + (requestActivation ? charger.getPlugPowerKw() : -charger.getPlugPowerKw());

        ChargerOperationalStateChangedEvent event = new ChargerOperationalStateChangedEvent(
                chargerId,
                transitionState,
                newActiveChargers,
                newCurrentMaxPower
        );

        log.debug("Evento cambiamento generato: {}", event);
        eventPublisher.publishEvent(event);

        log.debug("=".repeat(50));
    }

    /**
     * Restituisce una copia immutabile dello stato dell'hub.
     *
     * @return una copia di {@code hub}
     */
    public synchronized Hub getHubState() {
        checkIfHubIsInitialized();
        return hub.deepCopy();
    }

    /**
     * Restituisce una copia immutabile dello stato del connettore.
     *
     * @param chargerId id del connettore da restituire
     * @return una copia del connettore richiesto
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     * @throws ChargerNotFoundException se l'hub non contiene il connettore indicato
     */
    public synchronized Charger getCharger(String chargerId) {
        checkIfHubIsInitialized();
        checkIfChargerExists(chargerId);

        return hub.getCharger(chargerId);
    }

    /**
     * Verifica se l'hub è stato inizializzato.
     *
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     */
    public synchronized void checkIfHubIsInitialized() {
        if (hub == null) {
            throw new HubNotInitializedException("Hub non ancora inizializzato", ApplicationErrorCode.HUB_NOT_INITIALIZED);
        }
    }

    /**
     * Verifica se il connettore esiste nell'hub.
     *
     * @throws HubNotInitializedException se il metodo viene invocato prima di aver inizializzato l'hub
     * @throws ChargerNotFoundException se l'hub non contiene il connettore indicato
     */
    private void checkIfChargerExists(String chargerId) {
        checkIfHubIsInitialized();

        if (!hub.hasCharger(chargerId))
            throw new ChargerNotFoundException("Nessun connettore con id " + chargerId + " trovato", ApplicationErrorCode.CHARGER_NOT_FOUND);
    }

    private boolean checkIfChargerIsOperative(String chargerId) {
        if (!hub.hasCharger(chargerId))
            throw new ChargerNotFoundException("Nessun connettore con id " + chargerId + " trovato", ApplicationErrorCode.CHARGER_NOT_FOUND);

        return hub.isChargerOperative(chargerId);
    }

    private void restorePreviousState(String chargerId, Charger previousState) {
        // Rollback stato in memoria
        hub.restoreChargerState(chargerId, previousState);

        log.debug("Errore nella comunicazione col simulatore, rollback a stato precedente: {}", getHubState());
    }

    private UpdateTimestamp extractCommonUpdateTime(List<ChargerStateUpdateDTO> states) {
        return states.stream()
                .map(ChargerStateUpdateDTO::chargerState)
                .filter(Objects::nonNull)
                .max(Comparator.comparing(StateDTO::lastUpdateTime))
                .map(state -> new UpdateTimestamp(
                        state.lastUpdateTime(),
                        state.lastUpdateFormattedTime()
                ))
                .orElse(new UpdateTimestamp(null, null));
    }

    private record UpdateTimestamp(Double lastUpdateTime, String lastUpdateFormattedTime) {}
}
