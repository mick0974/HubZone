package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.StateChangeDelta;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class StateChangeDeltaTest {

    @Test
    void computeDelta_firstSimulationUpdate_chargerInUse() {
        Charger charger1 = new Charger("charger-1", "CA", 22.0);
        Charger oldState = charger1.deepCopy();
        charger1.updateState(15.5, true, true);

        StateChangeDelta delta = StateChangeDelta.computeDelta(charger1, oldState);

        assertThat(delta.activeChargersDelta()).isEqualTo(1);
        assertThat(delta.occupiedChargersDelta()).isEqualTo(1);
        assertThat(delta.currentMaxPowerDelta()).isEqualTo(22.0);
        assertThat(delta.currentPowerInUseDelta()).isEqualTo(15.5);
        assertThat(delta.currentPowerRemainingDelta()).isEqualTo(22.0 - 15.5);
    }

    @Test
    void computeDelta_firstSimulationUpdate_chargerNotInUse() {
        Charger charger1 = new Charger("charger-1", "CA", 22.0);
        Charger oldState = charger1.deepCopy();
        charger1.updateState(0.0, false, false);

        StateChangeDelta delta = StateChangeDelta.computeDelta(charger1, oldState);

        assertThat(delta.activeChargersDelta()).isEqualTo(0);
        assertThat(delta.occupiedChargersDelta()).isEqualTo(0);
        assertThat(delta.currentMaxPowerDelta()).isEqualTo(0);
        assertThat(delta.currentPowerInUseDelta()).isEqualTo(0);
        assertThat(delta.currentPowerRemainingDelta()).isEqualTo(0);
    }

    @Test
    void noChanges_computeZeroDelta() {
        StateChangeDelta zero = StateChangeDelta.noChanges();

        assertThat(zero.activeChargersDelta()).isEqualTo(0);
        assertThat(zero.occupiedChargersDelta()).isEqualTo(0);
        assertThat(zero.currentMaxPowerDelta()).isEqualTo(0);
        assertThat(zero.currentPowerInUseDelta()).isEqualTo(0);
        assertThat(zero.currentPowerRemainingDelta()).isEqualTo(0);

    }

    @Test
    void add_computeDeltaSum() {
        Charger charger1 = new Charger("charger-1", "CA", 22.0);
        Charger oldState = charger1.deepCopy();
        charger1.updateState(15.5, true, true);
        StateChangeDelta delta1 = StateChangeDelta.computeDelta(charger1, oldState);

        Charger charger2 = new Charger("charger-", "CA", 10.0);
        Charger oldState2 = charger2.deepCopy();
        charger2.updateState(7.0, true, true);
        StateChangeDelta delta2 = StateChangeDelta.computeDelta(charger2, oldState2);

        StateChangeDelta sum = StateChangeDelta.add(delta1, delta2);

        assertThat(sum.activeChargersDelta()).isEqualTo(2);
        assertThat(sum.occupiedChargersDelta()).isEqualTo(2);
        assertThat(sum.currentMaxPowerDelta()).isEqualTo(32.0);
        assertThat(sum.currentPowerInUseDelta()).isEqualTo(22.5);
        assertThat(sum.currentPowerRemainingDelta()).isEqualTo(32.0 - 22.5);
    }
}
