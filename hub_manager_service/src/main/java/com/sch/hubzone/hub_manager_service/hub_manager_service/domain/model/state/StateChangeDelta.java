package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import lombok.extern.slf4j.Slf4j;

/**
 * Record rappresentante la variazione da applicare alle metriche aggregate dell'hub
 * in seguito al cambiamento di stato di un connettore (nuovo stato - vecchio stato).
 */
@Slf4j
public record StateChangeDelta(int activeChargersDelta, int occupiedChargersDelta, double currentMaxPowerDelta,
                               double currentPowerInUseDelta, double currentPowerRemainingDelta) {

    public static StateChangeDelta computeDelta(Charger newState, Charger oldState) {
        int activeDelta = activeContribution(newState) - activeContribution(oldState);
        int occupiedDelta = occupiedContribution(newState) - occupiedContribution(oldState);
        double maxPowerDelta = maxPowerContribution(newState) - maxPowerContribution(oldState);
        double powerInUseDelta = powerInUseContribution(newState) - powerInUseContribution(oldState);
        double remainingPowerDelta = maxPowerDelta - powerInUseDelta;

        StateChangeDelta delta = new StateChangeDelta(
                activeDelta,
                occupiedDelta,
                maxPowerDelta,
                powerInUseDelta,
                remainingPowerDelta
        );

        log.info(delta.toString());
        return delta;
    }

    public static StateChangeDelta noChanges() {
        return new StateChangeDelta(0, 0, 0, 0.0, 0.0);
    }

    public static StateChangeDelta add(StateChangeDelta d1, StateChangeDelta d2) {
        return new StateChangeDelta(
                d1.activeChargersDelta + d2.activeChargersDelta(),
                d1.occupiedChargersDelta + d2.occupiedChargersDelta(),
                d1.currentMaxPowerDelta + d2.currentMaxPowerDelta(),
                d1.currentPowerInUseDelta + d2.currentPowerInUseDelta(),
                d1.currentPowerRemainingDelta + d2.currentPowerRemainingDelta()
        );
    }

    /* ========================================================
       Metodi helper
       ======================================================== */

    private static int activeContribution(Charger charger) {
        return charger.currentlyContributeToPower() ? 1 : 0;
    }

    private static int occupiedContribution(Charger charger) {
        return charger.isOccupied() ? 1 : 0;
    }

    private static double maxPowerContribution(Charger charger) {
        return charger.currentlyContributeToPower()
                ? charger.getPlugPowerKw()
                : 0.0;
    }

    private static double powerInUseContribution(Charger charger) {
        return charger.getCurrentPowerInUse();
    }

    @Override
    public String toString() {
        return "StateChangeDelta{" +
                "activeChargersDelta=" + activeChargersDelta +
                ", occupiedChargersDelta=" + occupiedChargersDelta +
                ", currentMaxPowerDelta=" + currentMaxPowerDelta +
                ", currentPowerInUseDelta=" + currentPowerInUseDelta +
                '}';
    }
}
