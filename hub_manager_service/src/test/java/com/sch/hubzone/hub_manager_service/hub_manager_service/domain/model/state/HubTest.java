package com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state;

import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Charger;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.ChargerOperationalState;
import com.sch.hubzone.hub_manager_service.hub_manager_service.domain.model.state.Hub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HubTest {

    @Nested
    @DisplayName("Test per il metodo addCharger()")
    class AddChargerTests {

        @Test
        @DisplayName("Deve aggiungere un connettore all'hub non inizializzato.")
        void addCharger_toEmptyHub_shouldAddCharger() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);
            
            assertEquals(1, hub.getAllChargers().size());
            assertTrue(hub.hasCharger("charger-1"));
            
            Charger charger = hub.getCharger("charger-1");
            assertEquals("charger-1", charger.getChargerId());
            assertEquals("AC", charger.getChargerType());
            assertEquals(22.0, charger.getPlugPowerKw());
        }

        @Test
        @DisplayName("Deve aggiungere più connettori all'hub non inizializzato.")
        void addCharger_multipleChargers_shouldAddAll() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);
            hub.addCharger("charger-2", "CCS", 50.0);
            hub.addCharger("charger-3", "AC", 22.0);

            assertEquals(3, hub.getAllChargers().size());
            assertTrue(hub.hasCharger("charger-1"));
            assertTrue(hub.hasCharger("charger-2"));
            assertTrue(hub.hasCharger("charger-3"));
        }
    }

    @Nested
    @DisplayName("Test per il metodo getCharger()")
    class GetChargerTests {

        @Test
        @DisplayName("Il connettore esiste. Deve restituirlo.")
        void getCharger_chargerExists_shouldReturnCharger() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);

            Charger result = hub.getCharger("charger-1");

            assertNotNull(result);
            assertEquals("charger-1", result.getChargerId());
            assertEquals("AC", result.getChargerType());
            assertEquals(22.0, result.getPlugPowerKw());
        }

        @Test
        @DisplayName("Il connettore non esiste. Deve restituire null.")
        void getCharger_chargerNotExist_shouldReturnNull() {
            Hub hub = new Hub();

            Charger result = hub.getCharger("charger-1");

            assertNull(result);
        }

        @Test
        @DisplayName("L'id richiesto è null. Deve restituire null.")
        void getCharger_nullId_shouldReturnNull() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);

            Charger result = hub.getCharger(null);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Test per il metodo hasCharger()")
    class HasChargerTests {

        @Test
        @DisplayName("Il connettore esiste nell'hub. Deve restituire true.")
        void hasCharger_chargerExists_shouldReturnTrue() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);

            boolean result = hub.hasCharger("charger-1");

            assertTrue(result);
        }

        @Test
        @DisplayName("Il connettore non esiste nell'hub. Deve restituire false.")
        void hasCharger_ChargerNotExist_shouldReturnFalse() {
            Hub hub = new Hub();

            boolean result = hub.hasCharger("charger-1");

            assertFalse(result);
        }

        @Test
        @DisplayName("L'id richiesto è null. Deve restituire false.")
        void hasCharger_nullId_shouldReturnFalse() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);

            boolean result = hub.hasCharger(null);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Test per il metodo getAllChargers()")
    class GetAllChargersTests {

        @Test
        @DisplayName("L'hub non è inizializzato e non contiene connettori. Deve restituire una lista vuota.")
        void getAllChargers_hubNotInitialized_shouldReturnEmptyList() {
            Hub hub = new Hub();

            List<Charger> result = hub.getAllChargers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("L'hub è inizializzato. Deve restituire tutti i connettori present.")
        void getAllChargers_withChargers_shouldReturnAllChargers() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);
            hub.addCharger("charger-2", "CCS", 50.0);
            hub.addCharger("charger-3", "AC", 22.0);

            List<Charger> result = hub.getAllChargers();

            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("La lista restituita deve essere composta da elementi immutabili.")
        void getAllChargers_shouldReturnImmutableList() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);

            List<Charger> result = hub.getAllChargers();
            int originalSize = hub.getAllChargers().size();
            
            assertThrows(UnsupportedOperationException.class, () ->
                    result.add(new Charger("charger-2", "AC", 22.0))
            );

            assertEquals(originalSize, hub.getAllChargers().size());
        }
    }

    @Nested
    @DisplayName("Test per il metodo canDeactivateChargers()")
    class CanDeactivateChargersTests {

        @Test
        @DisplayName("L'hub contiene due connettore attivi. Deve restituire true.")
        void canDeactivateChargers_twoActiveChargers_shouldReturnTrue() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-2", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);

            boolean result = hub.canDeactivateChargers();

            assertTrue(result);
        }

        @Test
        @DisplayName("L'hub contiene tre connettore attivi. Deve restituire true.")
        void canDeactivateChargers_multipleActiveChargers_shouldReturnTrue() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-2", "CCS", 50.0, 0.0, false, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-3", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);

            boolean result = hub.canDeactivateChargers();

            assertTrue(result);
        }

        @Test
        @DisplayName("L'hub contiene un connettore attivo. Deve restituire false.")
        void canDeactivateChargers_oneActiveCharger_shouldReturnFalse() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-2", "AC", 22.0, 0.0, false, ChargerOperationalState.INACTIVE);

            boolean result = hub.canDeactivateChargers();

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve considerare IN_ACTIVATION come inattivo.")
        void canDeactivateChargers_chargerInActivation_shouldCountAsFutureActive() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-2", "AC", 22.0, 0.0, false, ChargerOperationalState.IN_ACTIVATION);

            boolean result = hub.canDeactivateChargers();

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve considerare IN_DEACTIVATION come inattivo.")
        void canDeactivateChargers_chargerInDeactivation_shouldCountAsFutureInactive() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-2", "AC", 22.0, 0.0, false, ChargerOperationalState.IN_DEACTIVATION);

            boolean result = hub.canDeactivateChargers();

            assertFalse(result);
        }

        @Test
        @DisplayName("Deve considerare NOT_INITIALIZED come inattivo.")
        void canDeactivateChargers_withNotInitializedChargers_shouldIgnoreThem() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0);
            hub.addCharger("charger-2", "AC", 22.0);
            hub.addCharger("charger-3", "AC", 22.0, 0.0, false, ChargerOperationalState.ACTIVE);

            boolean result = hub.canDeactivateChargers();

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Test per il metodo updateSimulationTimestamp()")
    class UpdateSimulationTimestampTests {

        @Test
        @DisplayName("Deve aggiornare il timestamp della simulazione")
        void updateSimulationTimestamp_validTimestamp_shouldUpdate() {
            Hub hub = new Hub();
            assertEquals(0.0, hub.getSimulationTimestamp());

            hub.updateSimulationTimestamp(123.45);

            assertEquals(123.45, hub.getSimulationTimestamp());
        }
    }

    @Nested
    @DisplayName("Test per il metodo deepCopy()")
    class DeepCopyTests {

        @Test
        @DisplayName("Deve creare una copia con tutti i connettori.")
        void deepCopy_hubInitialized_shouldCopyAllChargers() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 15.0, true, ChargerOperationalState.ACTIVE);
            hub.addCharger("charger-2", "CCS", 50.0, 0.0, false, ChargerOperationalState.INACTIVE);
            hub.updateSimulationTimestamp(123.45);

            Hub copy = hub.deepCopy();

            assertEquals(hub.getAllChargers().size(), copy.getAllChargers().size());
            assertEquals(hub.getSimulationTimestamp(), copy.getSimulationTimestamp());

            assertNotSame(hub.getCharger("charger-1"), copy.getCharger("charger-1"));
            assertNotSame(hub.getCharger("charger-2"), copy.getCharger("charger-2"));

            assertEquals(hub.getCharger("charger-1").getChargerId(), copy.getCharger("charger-1").getChargerId());
            assertEquals(hub.getCharger("charger-1").getCurrentPowerInUse(), copy.getCharger("charger-1").getCurrentPowerInUse());
            assertEquals(hub.getCharger("charger-1").isOccupied(), copy.getCharger("charger-1").isOccupied());
        }

        @Test
        @DisplayName("Modifiche alla copia non devono influenzare l'originale.")
        void deepCopy_copyEdited_shouldNotAffectOriginal() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 15.0, true, ChargerOperationalState.ACTIVE);
            hub.updateSimulationTimestamp(100.0);

            Hub copy = hub.deepCopy();

            copy.addCharger("charger-2", "CCS", 50.0);
            copy.updateSimulationTimestamp(200.0);
            copy.updateChargerState("charger-1", 0.0, false, true);

            assertEquals(1, hub.getAllChargers().size());
            assertEquals(100.0, hub.getSimulationTimestamp());
            assertEquals(15.0, hub.getCharger("charger-1").getCurrentPowerInUse());
            assertTrue(hub.getCharger("charger-1").isOccupied());

            assertEquals(2, copy.getAllChargers().size());
            assertEquals(200.0, copy.getSimulationTimestamp());
            assertEquals(0.0, copy.getCharger("charger-1").getCurrentPowerInUse());
            assertFalse(copy.getCharger("charger-1").isOccupied());
        }

        @Test
        @DisplayName("Modifiche all'originale non devono influenzare la copia.")
        void deepCopy_originalEdited_shouldNotAffectCopy() {
            Hub hub = new Hub();
            hub.addCharger("charger-1", "AC", 22.0, 15.0, true, ChargerOperationalState.ACTIVE);

            Hub copy = hub.deepCopy();

            hub.addCharger("charger-2", "CCS", 50.0);
            hub.updateSimulationTimestamp(300.0);
            hub.updateChargerState("charger-1", 0.0, false, true);

            assertEquals(1, copy.getAllChargers().size());
            assertEquals(0.0, copy.getSimulationTimestamp());
            assertEquals(15.0, copy.getCharger("charger-1").getCurrentPowerInUse());
            assertTrue(copy.getCharger("charger-1").isOccupied());
        }
    }


}
