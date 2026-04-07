package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

public enum ChargerOperationalState {
    // Stato iniziale di un connettore, applicato all'inizializzazione della struttura dell'hub
    NOT_INITIALIZED, // indica che il connettore non ha ancora ricevuto il primo stato dal simulatore

    // Stati di transizione
    IN_ACTIVATION, // la richiesta di attivazione del connettore è stata accettata dal simulatore
    IN_DEACTIVATION, // la richiesta di disattivazione del connettore è stata accettata dal simulatore

    // Stati finali/concreti
    ON,
    OFF,
    ACTIVE,
    INACTIVE,

    // Stati di errore
    UNREACHABLE,
    FAULTED;

    /**
     * Indica se un connettore con questo stato contribuisce correntemente alla potenza massima disponibile dell'hub.
     */
    public boolean currentlyContributeToPower() {
        return this == IN_DEACTIVATION || this == ON || this == ACTIVE;
    }

    /**
     * Indica se un connettore con questo stato contribuirà alla potenza massima disponibile dell'hub.
     */
    public boolean willContributeToPower() {
        return this == IN_ACTIVATION || this == ON || this == ACTIVE;
    }

    /**
     * Indica se un connettore con questo stato viene considerato come connettore attivo nel determinare se un connettore
     * può essere disattivato o meno.
     */
    public boolean currentlyCountAsActive() {
        return this == ON || this == ACTIVE;
    }

    /**
     * Verifica se è possibile transitare allo stato di transizione specificato.
     *
     * @param transitState lo stato transitorio target
     * @return true se la transizione è valida
     * @throws IllegalArgumentException se transitState è null o non è uno stato di transizione
     */
    public boolean canGoToTransitState(ChargerOperationalState transitState) {
        if (transitState == null) {
            throw new IllegalArgumentException("transitState non può essere null");
        }
        if (!transitState.isTransitionState()) {
            throw new IllegalArgumentException(
                    "transitState deve essere uno stato di transizione, ricevuto: " + transitState
            );
        }

        if (transitState == IN_ACTIVATION)
            return this == OFF || this == INACTIVE;

        if (transitState == IN_DEACTIVATION)
            return this == ON || this == ACTIVE;

        return false;
    }

    /**
     * Verifica se è possibile transitare allo stato finale specificato.
     *
     * @param finalState lo stato finale target
     * @return true se la transizione è valida
     * @throws IllegalStateException se lo stato corrente non è uno stato di transizione
     * @throws IllegalArgumentException se finalState è null o non è uno stato finale
     */
    public boolean canGoToFinalState(ChargerOperationalState finalState) {
        if (!isTransitionState()) {
            throw new IllegalStateException(
                    "canGoToFinalState() richiede uno stato di transizione, stato corrente: " + this
            );
        }
        if (finalState == null) {
            throw new IllegalArgumentException("finalState non può essere null");
        }
        if (finalState.isTransitionState() || finalState.isErrorState()) {
            throw new IllegalArgumentException(
                    "finalState deve essere uno stato finale, ricevuto: " + finalState
            );
        }

        if (finalState == ACTIVE)
            return this == IN_ACTIVATION;

        if (finalState == INACTIVE)
            return this == IN_DEACTIVATION;

        return false;
    }

    public boolean isTransitionState() {
        return this == IN_ACTIVATION || this == IN_DEACTIVATION;
    }

    /**
     * Indica se un connettore con questo stato può accettare aggiornamenti dalla simulazione.
     */
    public boolean canAcceptUpdates() {
        return this == ON || this == ACTIVE || this == IN_ACTIVATION || this == IN_DEACTIVATION
                || this == FAULTED || this == UNREACHABLE;
    }

    /**
     * Indica se un connettore con questo stato deve ancora ricevere il primo stato dal simulatore.
     */
    public boolean hasToReceiveFirstUpdate() {
        return this == NOT_INITIALIZED;
    }

    /**
     * Indica se lo stato è uno stato di errore.
     */
    public boolean isErrorState() {
        return this == FAULTED || this == UNREACHABLE;
    }
}