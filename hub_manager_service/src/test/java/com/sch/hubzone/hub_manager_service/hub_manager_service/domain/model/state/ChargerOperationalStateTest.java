package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test ChargerOperationalState")
class ChargerOperationalStateTest {

    @Nested
    @DisplayName("Test per metodo currentlyContributeToPower()")
    class CurrentlyContributeToPowerTests {

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"ACTIVE", "ON", "IN_DEACTIVATION"})
        void currentlyContributeToPower_trueCases(ChargerOperationalState state) {
            assertTrue(state.currentlyContributeToPower());
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "IN_ACTIVATION", "NOT_INITIALIZED"})
        void currentlyContributeToPower_falseCases(ChargerOperationalState state) {
            assertFalse(state.currentlyContributeToPower());
        }
    }

    @Nested
    @DisplayName("Test per willContributeToPower()")
    class WillContributeToPowerTests {

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"ACTIVE", "ON", "IN_DEACTIVATION"})
        void currentlyContributeToPower_trueCases(ChargerOperationalState state) {
            assertTrue(state.currentlyContributeToPower());
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "IN_ACTIVATION", "NOT_INITIALIZED"})
        void currentlyContributeToPower_falseCases(ChargerOperationalState state) {
            assertFalse(state.currentlyContributeToPower());
        }
    }

    @Nested
    @DisplayName("Test per currentlyCountAsActive()")
    class CurrentlyCountAsActiveTests {

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"ACTIVE", "ON",})
        void currentlyContributeToPower_trueCases(ChargerOperationalState state) {
            assertTrue(state.currentlyContributeToPower());
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "IN_ACTIVATION", "NOT_INITIALIZED"})
        void currentlyContributeToPower_falseCases(ChargerOperationalState state) {
            assertFalse(state.currentlyContributeToPower());
        }
    }

    @Nested
    @DisplayName("Test per canGoToTransitState()")
    class CanGoToTransitStateTests {

        @Test
        @DisplayName("OFF può transitare a IN_ACTIVATION.")
        void canGoToTransitState_offToInActivation_shouldReturnTrue() {
            assertTrue(
                    ChargerOperationalState.OFF
                            .canGoToTransitState(ChargerOperationalState.IN_ACTIVATION)
            );
        }

        @Test
        @DisplayName("INACTIVE può transitare a IN_ACTIVATION.")
        void canGoToTransitState_inactiveToInActivation_shouldReturnTrue() {
            assertTrue(
                    ChargerOperationalState.INACTIVE
                            .canGoToTransitState(ChargerOperationalState.IN_ACTIVATION)
            );
        }

        @Test
        @DisplayName("OFF non può transitare a IN_DEACTIVATION.")
        void canGoToTransitState_offToInDeactivation_shouldReturnFalse() {
            assertFalse(
                    ChargerOperationalState.OFF
                            .canGoToTransitState(ChargerOperationalState.IN_DEACTIVATION)
            );
        }

        @Test
        @DisplayName("INACTIVE non può transitare a IN_DEACTIVATION.")
        void canGoToTransitState_inactiveToInDeactivation_shouldReturnFalse() {
            assertFalse(
                    ChargerOperationalState.INACTIVE
                            .canGoToTransitState(ChargerOperationalState.IN_DEACTIVATION)
            );
        }

        @Test
        @DisplayName("ACTIVE può transitare a IN_DEACTIVATION.")
        void canGoToTransitState_activeToInDeactivation_shouldReturnTrue() {
            assertTrue(
                    ChargerOperationalState.ACTIVE
                            .canGoToTransitState(ChargerOperationalState.IN_DEACTIVATION)
            );
        }

        @Test
        @DisplayName("ON può transitare a IN_DEACTIVATION.")
        void canGoToTransitState_onToInDeactivation_shouldReturnTrue() {
            assertTrue(
                    ChargerOperationalState.ON
                            .canGoToTransitState(ChargerOperationalState.IN_DEACTIVATION)
            );
        }

        @Test
        @DisplayName("ACTIVE non può transitare a IN_ACTIVATION.")
        void canGoToTransitState_activeToInActivation_shouldReturnFalse() {
            assertFalse(
                    ChargerOperationalState.ACTIVE
                            .canGoToTransitState(ChargerOperationalState.IN_ACTIVATION)
            );
        }

        @Test
        @DisplayName("ON può transitare a IN_DEACTIVATION.")
        void canGoToTransitState_onToInActivation_shouldReturnFalse() {
            assertFalse(
                    ChargerOperationalState.ON
                            .canGoToTransitState(ChargerOperationalState.IN_ACTIVATION)
            );
        }

        @Test
        @DisplayName("Lancia eccezione se lo stato di transizione è null.")
        void canGoToTransitState_null_shouldThrowException() {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ChargerOperationalState.ON.canGoToTransitState(null)
            );
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "ACTIVE", "ON", "NOT_INITIALIZED"})
        @DisplayName("Lancia eccezione se lo stato non è di transizione.")
        void canGoToTransitState_notTransitionState_shouldThrowException(ChargerOperationalState state) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> state.canGoToTransitState(ChargerOperationalState.ACTIVE)
            );
        }
    }

    @Nested
    @DisplayName("Test per canGoToFinalState()")
    class CanGoToFinalStateTests {

        @Test
        @DisplayName("IN_ACTIVATION può transitare a ACTIVE.")
        void canGoToFinalState_inActivationToActive_shouldReturnTrue() {
            assertTrue(
                    ChargerOperationalState.IN_ACTIVATION
                            .canGoToFinalState(ChargerOperationalState.ACTIVE)
            );
        }

        @Test
        @DisplayName("IN_DEACTIVATION può transitare a INACTIVE.")
        void canGoToFinalState_inDeactivationToInactive_shouldReturnTrue() {
            assertTrue(
                    ChargerOperationalState.IN_DEACTIVATION
                            .canGoToFinalState(ChargerOperationalState.INACTIVE)
            );
        }

        @Test
        @DisplayName("IN_ACTIVATION non può transitare a INACTIVE.")
        void canGoToFinalState_inActivationToInactive_shouldReturnFalse() {
            assertFalse(
                    ChargerOperationalState.IN_ACTIVATION
                            .canGoToFinalState(ChargerOperationalState.INACTIVE)
            );
        }

        @Test
        @DisplayName("IN_DEACTIVATION non può transitare a ACTIVE.")
        void canGoToFinalState_inDeactivationToActive_shouldReturnFalse() {
            assertFalse(
                    ChargerOperationalState.IN_DEACTIVATION
                            .canGoToFinalState(ChargerOperationalState.ACTIVE)
            );
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "ACTIVE", "ON", "NOT_INITIALIZED"})
        @DisplayName("Lancia eccezione se chiamato su stato non di transizione.")
        void canGoToFinalState_notTransitionState_shouldThrowException(ChargerOperationalState state) {
            assertThrows(
                    IllegalStateException.class,
                    () -> state.canGoToFinalState(ChargerOperationalState.INACTIVE)
            );
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"IN_ACTIVATION", "IN_DEACTIVATION"})
        @DisplayName("Lancia eccezione se lo stato di destinazione è uno stato di transizione.")
        void canGoToFinalState_finalStateIsATransitState_shouldThrowException(ChargerOperationalState state) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> state.canGoToFinalState(state)
            );
        }
    }

    @Nested
    @DisplayName("Test per metodi di stato")
    class VerifyStateTypeTests {

        @Test
        @DisplayName("IN_ACTIVATION è uno stato di transizione")
        void isTransitionState_inActivation_shouldReturnTrue() {
            assertTrue(ChargerOperationalState.IN_ACTIVATION.isTransitionState());
        }

        @Test
        @DisplayName("ACTIVE non è uno stato di transizione")
        void isTransitionState_active_shouldReturnFalse() {
            assertFalse(ChargerOperationalState.ACTIVE.isTransitionState());
        }

        @Test
        @DisplayName("NOT_INITIALIZED deve ricevere il primo update")
        void hasToReceiveFirstUpdate_notInitialized_shouldReturnTrue() {
            assertTrue(ChargerOperationalState.NOT_INITIALIZED.hasToReceiveFirstUpdate());
        }

        @Test
        @DisplayName("ACTIVE può accettare aggiornamenti dalla simulazione")
        void canAcceptSimulationUpdates_active_shouldReturnTrue() {
            assertTrue(ChargerOperationalState.ACTIVE.canAcceptUpdates());
        }
    }
}

