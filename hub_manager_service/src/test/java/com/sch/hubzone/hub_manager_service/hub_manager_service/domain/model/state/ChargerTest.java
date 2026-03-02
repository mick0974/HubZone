package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ChargerTest {

    @Nested
    @DisplayName("Test per il metodo updateFromSimulation()")
    class UpdateFromSimulationTests {

        @Test
        @DisplayName("Stato connettore non ancora inizializzato. Deve accettare l'aggiornamento e settare 'chargerOperationalState', " +
                "'currentPowerInUse' e 'occupied' ai valori ricevuti. L'aggiornamento imposta il connettore come attivo.")
        void updateFromSimulation_stateNotInitialised_shouldUpdateAsActive() {
            Charger charger = new Charger("charger-1", "AC", 22.0);

            Charger snapshot = charger.updateState(15.0, true, true);

            assertNotNull(snapshot);
            assertEquals(ChargerOperationalState.NOT_INITIALIZED, snapshot.getOperationalState());
            assertEquals(0.0, snapshot.getCurrentPowerInUse());
            assertFalse(snapshot.isOccupied());

            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
            assertEquals(15.0, charger.getCurrentPowerInUse());
            assertTrue(charger.isOccupied());
        }

        @Test
        @DisplayName("Stato connettore non ancora inizializzato. Deve accettare l'aggiornamento e settare " +
                "'chargerOperationalState', 'currentPowerInUse' e 'occupied' ai valori ricevuti. L'aggiornamento imposta il connettore come inattivo.")
        void updateFromSimulation_stateNotInitialised_shouldUpdateAsInactive() {
            Charger charger = new Charger("charger-1", "AC", 22.0);

            Charger snapshot = charger.updateState(0.0, false, false);

            assertNotNull(snapshot);
            assertEquals(ChargerOperationalState.NOT_INITIALIZED, snapshot.getOperationalState());
            assertEquals(0.0, snapshot.getCurrentPowerInUse());
            assertFalse(snapshot.isOccupied());

            assertEquals(ChargerOperationalState.INACTIVE, charger.getOperationalState());
            assertEquals(0.0, charger.getCurrentPowerInUse());
            assertFalse(charger.isOccupied());
        }

        @Test
        @DisplayName("Stato connettore inizializzato. L'aggiornamento dal simulatore è differente dallo stato corrente. " +
                "Deve aggiornare 'currentPowerInUse' e 'occupied'.")
        void updateFromSimulation_stateInitialised_shouldUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.ACTIVE);

            Charger snapshot = charger.updateState(18.5, true, true);

            assertNotNull(snapshot);
            assertEquals(0.0, snapshot.getCurrentPowerInUse());
            assertFalse(snapshot.isOccupied());
            assertEquals(ChargerOperationalState.ACTIVE, snapshot.getOperationalState());

            assertEquals(18.5, charger.getCurrentPowerInUse());
            assertTrue(charger.isOccupied());
            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato. L'aggiornamento dal simulatore è è identico dallo stato corrente. " +
                "Non deve aggiornare 'currentPowerInUse' e 'occupied' e restituire null nulla chiamata del metodo.")
        void updateFromSimulation_stateInitialised_shouldNotUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    15.0, true, ChargerOperationalState.ACTIVE);

            Charger snapshot = charger.updateState(15.0, true, true);

            assertNull(snapshot);

            assertEquals(15.0, charger.getCurrentPowerInUse());
            assertTrue(charger.isOccupied());
            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo transitorio IN_ACTIVATION. L'aggiornamento dal " +
                "simulatore conferma l'attivazione. Deve aggiornare 'chargerOperationalState' = ACTIVE, 'currentPowerInUse' = 0.0 e 'occupied' = false")
        void updateFromSimulation_transitionStateInActivation_shouldGoToFinalStateAndUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.IN_ACTIVATION);

            Charger snapshot = charger.updateState(0.0, false, true);

            assertNotNull(snapshot);
            assertEquals(0.0, snapshot.getCurrentPowerInUse());
            assertFalse(snapshot.isOccupied());
            assertEquals(ChargerOperationalState.IN_ACTIVATION, snapshot.getOperationalState());

            assertEquals(0.0, charger.getCurrentPowerInUse());
            assertFalse(charger.isOccupied());
            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo transitorio IN_DEACTIVATION. L'aggiornamento dal " +
                "simulatore conferma l'attivazione. Deve aggiornare 'chargerOperationalState' = ACTIVE, 'currentPowerInUse' = 0.0 e 'occupied' = false")
        void updateFromSimulation_transitionStateInDeactivation_shouldGoToFinalStateAndUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    15.0, true, ChargerOperationalState.IN_DEACTIVATION);

            Charger snapshot = charger.updateState(0.0, false, false);

            assertNotNull(snapshot);
            assertEquals(15.0, snapshot.getCurrentPowerInUse());
            assertTrue(snapshot.isOccupied());
            assertEquals(ChargerOperationalState.IN_DEACTIVATION, snapshot.getOperationalState());

            assertEquals(0.0, charger.getCurrentPowerInUse());
            assertFalse(charger.isOccupied());
            assertEquals(ChargerOperationalState.INACTIVE, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo transitorio IN_ACTIVATION. L'aggiornamento dal " +
                "simulatore conferma l'attivazione. Deve aggiornare 'chargerOperationalState' = ACTIVE, 'currentPowerInUse' = 5.0 e 'occupied' = true")
        void updateFromSimulation_transitionStateInActivation_shouldGoToFinalStateAndUpdate_2() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.IN_ACTIVATION);

            Charger snapshot = charger.updateState(5.0, true, true);

            assertNotNull(snapshot);
            assertEquals(0.0, snapshot.getCurrentPowerInUse());
            assertFalse(snapshot.isOccupied());
            assertEquals(ChargerOperationalState.IN_ACTIVATION, snapshot.getOperationalState());

            assertEquals(5.0, charger.getCurrentPowerInUse());
            assertTrue(charger.isOccupied());
            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo transitorio IN_DEACTIVATION. L'aggiornamento dal " +
                "simulatore indica che il connettore è ancora disattivo. Deve aggiornare 'chargerOperationalState' = IN_DEACTIVATION, " +
                "'currentPowerInUse' = 0.0 e 'occupied' = false")
        void updateFromSimulation_transitionStateInDeactivation_shouldNotGoToFinalStateAndUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    15.0, true, ChargerOperationalState.IN_DEACTIVATION);

            Charger snapshot = charger.updateState(7.0, false, true);

            assertNotNull(snapshot);
            assertEquals(15.0, snapshot.getCurrentPowerInUse());
            assertTrue(snapshot.isOccupied());
            assertEquals(ChargerOperationalState.IN_DEACTIVATION, snapshot.getOperationalState());

            assertEquals(7.0, charger.getCurrentPowerInUse());
            assertFalse(charger.isOccupied());
            assertEquals(ChargerOperationalState.IN_DEACTIVATION, charger.getOperationalState());
        }

        @Test
        @DisplayName("Connettore disattivo. Non deve accettare l'aggiornamento.")
        void updateFromSimulation_chargerInactive_shouldNotUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.INACTIVE);

            Charger snapshot = charger.updateState(7.0, false, true);

            assertNull(snapshot);

            assertEquals(0.0, charger.getCurrentPowerInUse());
            assertFalse(charger.isOccupied());
            assertEquals(ChargerOperationalState.INACTIVE, charger.getOperationalState());
        }
    }

    @Nested
    @DisplayName("Test per il metodo updateOperationalState()")
    class UpdateOperationalStateTests {

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo ACTIVE. L'hub manager richiede la disattivazione" +
                " del connettore, portandolo nello stato transitorio IN_DEACTIVATION. L'aggiornamento deve avere successo.")
        void updateOperationalState_transitToInDeactivation_shouldUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    15.0, true, ChargerOperationalState.ACTIVE);

            Charger snapshot = charger.updateOperationalState(ChargerOperationalState.IN_DEACTIVATION);

            assertNotNull(snapshot);
            assertEquals(ChargerOperationalState.ACTIVE, snapshot.getOperationalState());
            assertEquals(ChargerOperationalState.IN_DEACTIVATION, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo DEACTIVE. L'hub manager richiede la disattivazione" +
                " del connettore, portandolo nello stato transitorio IN_ACTIVATION. L'aggiornamento deve avere successo.")
        void updateOperationalState_transitToInActivation_shouldUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.ACTIVE);

            Charger snapshot = charger.updateOperationalState(ChargerOperationalState.IN_DEACTIVATION);

            assertNotNull(snapshot);
            assertEquals(ChargerOperationalState.ACTIVE, snapshot.getOperationalState());
            assertEquals(ChargerOperationalState.IN_DEACTIVATION, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo transitorio IN_ACTIVATION. L'hub manager richiede " +
                "l'attivazione del connettore già in attivazione. L'aggiornamento deve essere negato.")
        void updateOperationalState_alreadyInActivation_shouldNotUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.IN_ACTIVATION);

            Charger snapshot = charger.updateOperationalState(ChargerOperationalState.IN_ACTIVATION);

            assertNull(snapshot);
            assertEquals(ChargerOperationalState.IN_ACTIVATION, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore non inizializzato. L'hub manager richiede l'attivazione del connettore. L'aggiornamento " +
                "deve essere negato non essendo inizializzato.")
        void updateOperationalState_notInitialized_shouldNotUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0);

            Charger snapshot = charger.updateOperationalState(ChargerOperationalState.IN_ACTIVATION);

            assertNull(snapshot);
            assertEquals(ChargerOperationalState.NOT_INITIALIZED, charger.getOperationalState());
        }

        @Test
        @DisplayName("Stato connettore inizializzato e in stato operativo ACRTIVA. L'hub manager richiede l'attivazione " +
                "del connettore. L'aggiornamento deve essere negato essendo già attivo.")
        void updateOperationalState_sameState_shouldNotUpdate() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, ChargerOperationalState.ACTIVE);

            Charger snapshot = charger.updateOperationalState(ChargerOperationalState.IN_ACTIVATION);

            assertNull(snapshot);
            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
        }
    }

    @Nested
    @DisplayName("Test per il metodo restoreFrom()")
    class RestoreFromTests {

        @Test
        @DisplayName("Snapshot non nulla e associala al connettore giusto. Deve ripristinare lo stato della snapshot.")
        void restoreFrom_snapshotCorrect_shouldRestore() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    20.0, true, ChargerOperationalState.ACTIVE);
            Charger snapshot = charger.deepCopy();

            charger.updateState(0.0, false, true);

            // Verifico la modifica
            assertEquals(0.0, charger.getCurrentPowerInUse());
            assertFalse(charger.isOccupied());

            charger.restoreFrom(snapshot);

            // Verifico il ripristino
            assertEquals(20.0, charger.getCurrentPowerInUse());
            assertTrue(charger.isOccupied());
            assertEquals(ChargerOperationalState.ACTIVE, charger.getOperationalState());
        }

        @Test
        @DisplayName("Snapshot nulla. Deve lanciare IllegalArgumentException.")
        void restoreFrom_snapshotNull_shouldThrowException() {
            Charger charger = new Charger("charger-1", "AC", 22.0);

            assertThrows(IllegalArgumentException.class, () -> charger.restoreFrom(null));
        }

        @Test
        @DisplayName("Snapshot associata ad un altro connettore. Deve lanciare IllegalArgumentException.")
        void restoreFrom_snapshotNotCompatible_shouldThrowException() {
            Charger charger = new Charger("charger-1", "AC", 22.0);
            Charger otherSnapshot = new Charger("C2", "AC", 22.0,
                    0.0, false, ChargerOperationalState.ACTIVE);

            assertThrows(IllegalArgumentException.class, () -> charger.restoreFrom(otherSnapshot));
        }
    }

    @Nested
    @DisplayName("Test per il metodo createSnapshot()")
    class CreateSnapshotTests {

        @Test
        @DisplayName("Deve creare una copia immutabile di Charger.")
        void shouldCreateImmutableSnapshot() {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    15.0, true, ChargerOperationalState.ACTIVE);

            Charger snapshot = charger.deepCopy();

            assertEquals(charger.getChargerId(), snapshot.getChargerId());
            assertEquals(charger.getChargerType(), snapshot.getChargerType());
            assertEquals(charger.getPlugPowerKw(), snapshot.getPlugPowerKw());
            assertEquals(charger.getCurrentPowerInUse(), snapshot.getCurrentPowerInUse());
            assertEquals(charger.isOccupied(), snapshot.isOccupied());
            assertEquals(charger.getOperationalState(), snapshot.getOperationalState());

            // Modifico l'originale
            charger.updateState(0.0, false, true);

            // Verifico che la copia non sia mutata
            assertEquals(15.0, snapshot.getCurrentPowerInUse());
            assertTrue(snapshot.isOccupied());
        }
    }

    @Nested
    @DisplayName("Test metodi currentlyContributeToPower() e willContributeToPower()")
    class PowerContributionTests {

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"ACTIVE", "ON", "IN_DEACTIVATION"})
        void currentlyContributeToPower_trueCases(ChargerOperationalState state) {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, state);

            assertTrue(charger.currentlyContributeToPower());
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "IN_ACTIVATION", "NOT_INITIALIZED"})
        void currentlyContributeToPower_falseCases(ChargerOperationalState state) {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, state);

            assertFalse(charger.currentlyContributeToPower());
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"ACTIVE", "ON", "IN_ACTIVATION"})
        void willContributeToPower_trueCases(ChargerOperationalState state) {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, state);

            assertTrue(charger.willContributeToPower());
        }

        @ParameterizedTest
        @EnumSource(value = ChargerOperationalState.class,
                names = {"INACTIVE", "OFF", "IN_DEACTIVATION", "NOT_INITIALIZED"})
        void willContributeToPower_falseCases(ChargerOperationalState state) {
            Charger charger = new Charger("charger-1", "AC", 22.0,
                    0.0, false, state);

            assertFalse(charger.willContributeToPower());
        }
    }

}