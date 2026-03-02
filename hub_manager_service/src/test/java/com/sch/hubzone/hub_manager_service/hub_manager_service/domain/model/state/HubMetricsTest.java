package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.HubMetrics;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.StateChangeDelta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Test HubMetrics")
class HubMetricsTest {

    @Nested
    @DisplayName("Test per il metodo applyStateChangeDelta()")
    class ApplyStateChangeDeltaTests {

        @Test
        @DisplayName("Delta positivo. Deve incrementare le metriche.")
        void applyStateChangeDelta_positiveDelta_shouldUpdateMetrics() {
            HubMetrics metrics = new HubMetrics();

            StateChangeDelta delta = new StateChangeDelta(
                    2,
                    1,
                    100.0,
                    40.0,
                    60.0
            );

            metrics.applyStateChangeDelta(delta);

            assertEquals(2, metrics.getActiveChargers());
            assertEquals(1, metrics.getOccupiedChargers());
            assertEquals(100.0, metrics.getCurrentMaxPower());
            assertEquals(40.0, metrics.getCurrentPowerInUse());
            assertEquals(60.0, metrics.getCurrentPowerRemaining());
            assertEquals(40.0, metrics.getCurrentPowerInUsePercentage());
        }

        @Test
        @DisplayName("Delta negativo. Deve decrementare le metriche.")
        void applyStateChangeDelta_negativeDelta_shouldDecreaseMetrics() {
            HubMetrics metrics = new HubMetrics(
                    3, 2, 100.0, 60.0, 40.0, 60.0
            );

            StateChangeDelta delta = new StateChangeDelta(
                    -1,
                    -1,
                    -20.0,
                    -10.0,
                    -10.0
            );

            metrics.applyStateChangeDelta(delta);

            assertEquals(2, metrics.getActiveChargers());
            assertEquals(1, metrics.getOccupiedChargers());
            assertEquals(80.0, metrics.getCurrentMaxPower());
            assertEquals(50.0, metrics.getCurrentPowerInUse());
            assertEquals(30.0, metrics.getCurrentPowerRemaining());
            assertEquals(62.5, metrics.getCurrentPowerInUsePercentage());
        }
    }
}

