package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@ToString
public class Hub {
    private final HubMetrics hubMetrics;
    private final Map<String, Charger> chargers;
    @Getter
    private double simulationTimestamp;
    @Getter
    private String simulationFormattedTimestamp;
    @Getter
    private double latitude;
    @Getter
    private double longitude;

    public Hub() {
        this.hubMetrics = new HubMetrics();
        this.chargers = new HashMap<>();
        this.simulationTimestamp = 0.0;
        this.simulationFormattedTimestamp = "";
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    /**
     * Aggiunge un connettore con stato non inizializzato all'hub. Da usare per l'inizializzazione della struttura dell'hub.
     */
    public void addCharger(String chargerId, String chargerType, double plugPowerKw) {
        chargers.put(chargerId, new Charger(chargerId, chargerType, plugPowerKw));
    }

    /**
     * Aggiunge un connettore con stato inizializzato all'hub.
     */
    public void addCharger(String chargerId, String chargerType, double plugPowerKw, double powerInUse, boolean occupied,
                           ChargerOperationalState operationalState) {
        chargers.put(chargerId, new Charger(chargerId, chargerType, plugPowerKw, powerInUse, occupied, operationalState));
    }

    /**
     * Restituisce una copia immutabile del connettore specificato.
     *
     * @param chargerId id del connettore da restituire
     */
    public Charger getCharger(String chargerId) {
        Charger charger = chargers.get(chargerId);
        if (charger == null)
            return null;
        return charger.deepCopy();
    }

    /**
     * Restituisce una copia immutabile delle metriche aggregate dell'hub.
     *
     */
    public HubMetrics getHubMetrics() {
        return hubMetrics.deepCopy();
    }

    /**
     * Verifica se il connettore specificato esiste nell'hub.
     */
    public boolean hasCharger(String chargerId) {
        return chargers.containsKey(chargerId);
    }

    /**
     * Verifica se il connettore specificato è attualmente operativo (inizializzato e non in uno stato di errore).
     */
    public boolean isChargerOperative(String chargerId) {
        return chargers.get(chargerId).isOperative();
    }

    /**
     * Restituisce tutti i connettori gestiti dall'hub.
     */
    public List<Charger> getAllChargers() {
        return chargers.values().stream().toList();
    }

    /**
     * Aggiorna le metriche aggregate dell'hub in seguito alla variazione di stato di un connettore.
     */
    public void updateAggregatedMetrics(StateChangeDelta delta) {
        hubMetrics.applyStateChangeDelta(delta);
    }

    /**
     * Aggiorna lo stato del connettore con lo stato ricevuto dal simulatore.
     */
    public Charger updateChargerState(String chargerId, double newPower, boolean newOccupied, boolean isActive) {
        Charger charger = chargers.get(chargerId);
        if (charger == null)
            return null;

        return charger.updateState(newPower, newOccupied, isActive);
    }

    /**
     * Aggiorna lo stato del connettore con lo stato ricevuto dal simulatore.
     */
    public Charger updateChargerStateAsFaulted(String chargerId) {
        Charger charger = chargers.get(chargerId);
        if (charger == null)
            return null;

        return charger.updateStateAsFaulted();
    }

    /**
     * Aggiorna lo stato del connettore con lo stato ricevuto dal simulatore.
     */
    public Charger updateChargerStateAsUnreachable(String chargerId) {
        Charger charger = chargers.get(chargerId);
        if (charger == null)
            return null;

        return charger.updateStateAsUnreachable();
    }

    /**
     * Aggiorna lo stato operativo del connettore con lo stato di transizione ricevuto.
     */
    public Charger updateChargerOperationalState(String chargerId, ChargerOperationalState newTransitionState) {
        Charger charger = chargers.get(chargerId);
        if (charger == null)
            return null;

        return charger.updateOperationalState(newTransitionState);
    }

    /**
     * Aggiorna lo stato operativo del connettore con lo stato di transizione ricevuto.
     */
    public void restoreChargerState(String chargerId, Charger stateToRestore) {
        Charger charger = chargers.get(chargerId);
        if (charger == null)
            return;

        charger.restoreFrom(stateToRestore);
    }

    /**
     * Verifica se è possibile disattivare altri connettore. Almeno un connettore hub deve essere sempre disponibile.
     * Gli stati transitori vengono considerato come inattivi.
     * @return true se ci sono almeno due connettori attivi nell'hub
     */
    public boolean canDeactivateChargers() {
        return getAllChargers().stream().filter(Charger::currentlyCountAsActive).count() > 1;
    }

    /**
     * Aggiorna il timestamp della simulazione.
     */
    public void updateSimulationTimestamp(Double timestamp) {
        if (timestamp == null)
            return;
        this.simulationTimestamp = timestamp;
    }

    /**
     * Aggiorna il timestamp formattato della simulazione.
     */
    public void updateSimulationFormattedTimestamp(String timestamp) {
        if (timestamp == null)
            return;
        this.simulationFormattedTimestamp = timestamp;
    }

    /**
     * Aggiorna la posizione in coordinate dell'hub.
     */
    public void setPosition(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Crea una deep copy dell'hub.
     */
    public Hub deepCopy() {
        Map<String, Charger> copyChargers = new HashMap<>();
        for (Map.Entry<String, Charger> entry : this.chargers.entrySet()) {
            Charger chargerCopy = entry.getValue().deepCopy();
            copyChargers.put(entry.getKey(), chargerCopy);
        }

        return new Hub(this.hubMetrics.deepCopy(), copyChargers, this.simulationTimestamp,
                this.simulationFormattedTimestamp, this.getLatitude(), this.getLongitude());
    }
}
