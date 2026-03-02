package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class Charger {
    // Struttura connettore (immutabile)
    private final String chargerId;
    private final String chargerType;
    private final double plugPowerKw;

    // Stato connettore
    private double currentPowerInUse;
    private boolean occupied;
    private ChargerOperationalState operationalState;

    /**
     * Costruttore per inizializzare la struttura del connettore.
     */
    public Charger(String chargerId, String chargerType, double plugPowerKw) {
        this.chargerId = chargerId;
        this.chargerType = chargerType;
        this.plugPowerKw = plugPowerKw;
        this.currentPowerInUse = 0.0;
        this.occupied = false;
        this.operationalState = ChargerOperationalState.NOT_INITIALIZED;
    }

    /**
     * Aggiorna lo stato corrente del connettore con lo stato comunicato dal simulatore. Lo stato verrà aggiornato solo se:
     * <ul>
     *     <li>Lo stato non è mai stato aggiornato;</li>
     *     <li>Il connettore è in uno stato che permette di accettare aggiornamenti.</li>
     * </ul>
     *
     * Se il connettore si trova in uno stato transitorio (si sta attivando o si sta disattivando), il metodo aggiornerà anche
     * {@link #operationalState} se lo stato comunicato dal simulatore porta il connettore allo stato finale atteso.
     *
     * @param newPower potenza erogata dal connettore nel simulatore al momento della snapshot
     * @param newOccupied indica se il connettore è occupato o meno al momento della snapshot del simulatore
     * @param isActive indica se il connettore è attivo o meno al momento della snapshot nel simulatore
     * @return snapshot dello stato precedente, oppure null se non vi sono stati cambiamenti allo stato o se l'aggiornamento
     *         non può essere accettato
     */
    protected Charger updateState(double newPower, boolean newOccupied, boolean isActive) {
        if (operationalState.hasToReceiveFirstUpdate()) {
            Charger snapshot = deepCopy();
            this.currentPowerInUse = newPower;
            this.occupied = newOccupied;
            this.operationalState = isActive ? ChargerOperationalState.ACTIVE : ChargerOperationalState.INACTIVE;
            return snapshot;
        } else if (operationalState.canAcceptUpdates()) {
            ChargerOperationalState finalState = isActive ? ChargerOperationalState.ACTIVE : ChargerOperationalState.INACTIVE;

            if (operationalState.isTransitionState() && operationalState.canGoToFinalState(finalState)) {
                Charger snapshot = deepCopy();
                this.operationalState = finalState;
                this.currentPowerInUse = newPower;
                this.occupied = newOccupied;
                return snapshot;
            } else {
                if (Double.compare(this.currentPowerInUse, newPower) == 0 && this.occupied == newOccupied) {
                    return null;
                }

                Charger snapshot = deepCopy();
                this.currentPowerInUse = newPower;
                this.occupied = newOccupied;
                return snapshot;
            }
        }

        return null;
    }

    protected Charger updateStateAsFaulted() {
        if (operationalState.equals(ChargerOperationalState.FAULTED)) {
            return null;
        }

        Charger snapshot = deepCopy();

        this.currentPowerInUse = 0.0;
        this.occupied = false;
        this.operationalState = ChargerOperationalState.FAULTED;

        return snapshot;
    }

    protected Charger updateStateAsUnreachable() {
        if (operationalState.equals(ChargerOperationalState.UNREACHABLE)) {
            return null;
        }

        Charger snapshot = deepCopy();

        this.currentPowerInUse = 0.0;
        this.occupied = false;
        this.operationalState = ChargerOperationalState.UNREACHABLE;

        return snapshot;
    }

    /**
     * Porta il connettore nello stato transitorio di IN_ACTIVATION o IN_DEACTIVATION in base al valore ricevuto.
     *
     * @param transitionState  stato di transizione in cui portare il connettore
     * @return snapshot dello stato precedente, oppure null se non vi sono stati cambiamenti allo stato
     */
    protected Charger updateOperationalState(ChargerOperationalState transitionState) {
        if (this.operationalState.equals(transitionState))
            return null;

        if (this.operationalState.hasToReceiveFirstUpdate())
            return null;

        if (!this.operationalState.canGoToTransitState(transitionState))
            return null;

        Charger snapshot = deepCopy();
        this.operationalState = transitionState;

        return snapshot;
    }

    /**
     * Ripristina lo stato da una snapshot.
     */
    protected void restoreFrom(Charger snapshot) {
        if (snapshot == null || !snapshot.chargerId.equals(this.chargerId)) {
            throw new IllegalArgumentException("Snapshot invalida per il connettore " + chargerId);
        }
        this.currentPowerInUse = snapshot.currentPowerInUse;
        this.occupied = snapshot.occupied;
        this.operationalState = snapshot.operationalState;
    }

    /**
     * Crea una snapshot immutabile dello stato corrente.
     */
    protected Charger deepCopy() {
        return new Charger(chargerId, chargerType, plugPowerKw,
                currentPowerInUse, occupied, operationalState);
    }

    /**
     * Verifica se il connettore contribuisce alla potenza massima corrente disponibile dell'hub. Considera gli stati transitori
     * come gli stati finali passati.
     */
    protected boolean currentlyContributeToPower() {
        return operationalState.currentlyContributeToPower();
    }

    /**
     * Verifica se il connettore contribuirà alla potenza massima corrente disponibile dell'hub. Considera gli stati transitori
     * come gli stati finali futuri.
     */
    protected boolean willContributeToPower() {
        return operationalState.willContributeToPower();
    }

    /**
     * Verifica se il connettore contribuisce al numero di connettori considerati attivi correntemente. Considera gli stati transitori
     * come stati inattivi.
     */
    protected boolean currentlyCountAsActive() {
        return operationalState.currentlyCountAsActive();
    }

    /**
     * Verifica se il connettore risulta attualmente operativo (inizializzato e non in errore).
     */
    protected boolean isOperative() {
        return !operationalState.isErrorState() && !operationalState.hasToReceiveFirstUpdate();
    }
}