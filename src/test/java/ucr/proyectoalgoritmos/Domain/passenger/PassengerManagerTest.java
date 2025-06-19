package ucr.proyectoalgoritmos.Domain.passenger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PassengerManagerTest {

    private PassengerManager passengerManager;

    @BeforeEach
    void setUp() {
        passengerManager = new PassengerManager();
    }

    @Test
    @DisplayName("Debe registrar y buscar un pasajero correctamente")
    void testRegisterAndSearchPassenger() throws TreeException, ListException {
        String id = "P123";
        String name = "Juan Perez";
        String nationality = "Costarricense";

        passengerManager.registerPassenger(id, name, nationality);

        Passenger foundPassenger = passengerManager.searchPassenger(id);
        assertNotNull(foundPassenger, "El pasajero debería ser encontrado.");
        assertEquals(id, foundPassenger.getId(), "El ID del pasajero encontrado debe coincidir.");
        assertEquals(name, foundPassenger.getName(), "El nombre del pasajero encontrado debe coincidir.");
        assertEquals(nationality, foundPassenger.getNationality(), "La nacionalidad del pasajero encontrado debe coincidir.");
        assertEquals(1, passengerManager.getPassengerCount(), "El conteo de pasajeros debe ser 1.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException al intentar registrar un pasajero duplicado")
    void testRegisterDuplicatePassenger() throws TreeException { // Keep throws TreeException
        String id = "P456";
        String name1 = "Maria Lopez";
        String nationality1 = "Mexicana";
        String name2 = "Maria Garcia";
        String nationality2 = "Costarricense";

        // FIRST, ensure the initial registration works and the count is 1.
        // This is where the problem likely is.
        try {
            passengerManager.registerPassenger(id, name1, nationality1);
        } catch (IllegalArgumentException e) {
            // This should not happen for the first registration, but if it does,
            // it means there's a problem with your test setup or PassengerManager logic
            // that is trying to register it as duplicate even if it's the first one.
            fail("La primera vez que se registra el pasajero '" + id + "' no debería lanzar IllegalArgumentException: " + e.getMessage());
        }

        // VERIFY that the first passenger was added successfully.
        // If this assertion fails, the problem is with the first registration, not the duplicate check.
        assertEquals(1, passengerManager.getPassengerCount(), "El conteo de pasajeros debe ser 1 después del primer registro exitoso.");


        // Now, attempt the duplicate registration and assert the exception
        assertThrows(IllegalArgumentException.class, () -> {
            passengerManager.registerPassenger(id, name2, nationality2);
        }, "Debe lanzar IllegalArgumentException si el pasajero ya está registrado.");

        // Finally, assert that the count remains 1 after the failed duplicate registration attempt
        assertEquals(1, passengerManager.getPassengerCount(), "El conteo de pasajeros debe seguir siendo 1 después de un intento de registro duplicado fallido.");
    }

    @Test
    @DisplayName("Debe retornar el conteo correcto de pasajeros")
    void testGetPassengerCount() throws TreeException {
        assertEquals(0, passengerManager.getPassengerCount(), "El conteo inicial debe ser 0.");
        passengerManager.registerPassenger("A1", "Test A", "USA");
        assertEquals(1, passengerManager.getPassengerCount(), "El conteo debe ser 1 después de añadir un pasajero.");
        passengerManager.registerPassenger("B2", "Test B", "CAN");
        assertEquals(2, passengerManager.getPassengerCount(), "El conteo debe ser 2 después de añadir dos pasajeros.");
    }

    @Test
    @DisplayName("Debe retornar null al buscar un pasajero inexistente")
    void testSearchNonExistentPassenger() throws TreeException {
        assertNull(passengerManager.searchPassenger("NonExistentID"), "Buscar un pasajero que no existe debe retornar null.");
    }

    @Test
    @DisplayName("Debe obtener todos los IDs de pasajeros registrados")
    void testGetAllPassengerIds() throws TreeException, ListException {
        passengerManager.registerPassenger("ID001", "Nombre1", "Nac1");
        passengerManager.registerPassenger("ID003", "Nombre3", "Nac3");
        passengerManager.registerPassenger("ID002", "Nombre2", "Nac2"); // IDs en orden no secuencial

        SinglyLinkedList ids = passengerManager.getAllPassengerIds();
        assertNotNull(ids, "La lista de IDs no debe ser nula.");
        assertEquals(3, ids.size(), "La lista de IDs debe contener 3 elementos.");

        // Asumiendo que el inOrderList del AVL los devuelve en orden ascendente por ID
        assertEquals("ID001", ids.get(0), "El primer ID debe ser 'ID001'.");
        assertEquals("ID002", ids.get(1), "El segundo ID debe ser 'ID002'.");
        assertEquals("ID003", ids.get(2), "El tercer ID debe ser 'ID003'.");
    }

    @Test
    @DisplayName("Debe obtener todos los objetos Passenger registrados")
    void testGetAllPassengers() throws TreeException, ListException {
        passengerManager.registerPassenger("PA01", "Pasajero Uno", "N1");
        passengerManager.registerPassenger("PA03", "Pasajero Tres", "N3");
        passengerManager.registerPassenger("PA02", "Pasajero Dos", "N2");

        DoublyLinkedList allPassengers = passengerManager.getAllPassengers();
        assertNotNull(allPassengers, "La lista de pasajeros no debe ser nula.");
        assertEquals(3, allPassengers.size(), "La lista de pasajeros debe contener 3 elementos.");

        Passenger p1 = (Passenger) allPassengers.get(0);
        Passenger p2 = (Passenger) allPassengers.get(1);
        Passenger p3 = (Passenger) allPassengers.get(2);

        assertEquals("PA01", p1.getId());
        assertEquals("PA02", p2.getId());
        assertEquals("PA03", p3.getId());
    }

    @Test
    @DisplayName("Debe procesar la compra de un billete añadiendo el vuelo al historial del pasajero")
    void testProcessTicketPurchase() throws QueueException, IllegalArgumentException, TreeException, ListException {
        String id = "HIST001";
        String name = "Pedro";
        String nationality = "Española";
        passengerManager.registerPassenger(id, name, nationality);
        Passenger passenger = passengerManager.searchPassenger(id);

        LocalDateTime departure = LocalDateTime.of(2025, 7, 1, 10, 0);
        Flight flight = new Flight("FL789", "MAD", "BCN", departure, 100);

        passengerManager.processTicketPurchase(passenger, flight);

        LinkedQueue history = passenger.getFlightHistory();
        assertNotNull(history, "El historial no debe ser nulo.");
        assertFalse(history.isEmpty(), "El historial no debe estar vacío.");
        assertEquals(1, history.size(), "El historial debe contener 1 vuelo.");
        assertEquals(flight, history.peek(), "El vuelo añadido debe ser el mismo que el recuperado del historial.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si pasajero o vuelo son nulos en processTicketPurchase")
    void testProcessTicketPurchaseNulls() throws ListException, QueueException {
        LocalDateTime departure = LocalDateTime.of(2025, 7, 1, 10, 0);
        Flight flight = new Flight("FL789", "MAD", "BCN", departure, 100);
        Passenger passenger = new Passenger("P999"); // Solo para tener un objeto pasajero

        assertThrows(IllegalArgumentException.class, () -> {
            passengerManager.processTicketPurchase(null, flight);
        }, "Debe lanzar IllegalArgumentException si el pasajero es null.");

        assertThrows(IllegalArgumentException.class, () -> {
            passengerManager.processTicketPurchase(passenger, null);
        }, "Debe lanzar IllegalArgumentException si el vuelo es null.");
    }

    @Test
    @DisplayName("Debe añadir un vuelo al historial de un pasajero existente")
    void testAddFlightToPassengerHistory() throws TreeException, QueueException, ListException {
        String id = "ADDHIST001";
        passengerManager.registerPassenger(id, "Ana", "Francesa");
        Passenger passenger = passengerManager.searchPassenger(id);

        LocalDateTime departure1 = LocalDateTime.of(2025, 8, 1, 12, 0);
        Flight flight1 = new Flight("FR101", "PAR", "ROM", departure1, 80);

        LocalDateTime departure2 = LocalDateTime.of(2025, 8, 15, 14, 0);
        Flight flight2 = new Flight("FR102", "ROM", "BER", departure2, 90);

        passengerManager.addFlightToPassengerHistory(id, flight1);
        passengerManager.addFlightToPassengerHistory(id, flight2);

        LinkedQueue history = passenger.getFlightHistory();
        assertEquals(2, history.size(), "El historial debe tener 2 vuelos.");
        assertEquals(flight1, history.deQueue(), "El primer vuelo debe ser el primero en salir.");
        assertEquals(flight2, history.deQueue(), "El segundo vuelo debe ser el segundo en salir.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el pasajero no se encuentra para añadir historial")
    void testAddFlightToPassengerHistoryPassengerNotFound() throws ListException, QueueException {
        LocalDateTime departure = LocalDateTime.of(2025, 9, 1, 9, 0);
        Flight flight = new Flight("GE001", "NYC", "LAX", departure, 200);

        assertThrows(IllegalArgumentException.class, () -> {
            passengerManager.addFlightToPassengerHistory("NONEXISTENT", flight);
        }, "Debe lanzar IllegalArgumentException si el pasajero no existe.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el vuelo es nulo al añadir al historial")
    void testAddFlightToPassengerHistoryNullFlight() throws TreeException {
        String id = "PNULLFLIGHT";
        passengerManager.registerPassenger(id, "Carlos", "Alemana");

        assertThrows(IllegalArgumentException.class, () -> {
            passengerManager.addFlightToPassengerHistory(id, null);
        }, "Debe lanzar IllegalArgumentException si el vuelo es null.");
    }

    @Test
    @DisplayName("Debe obtener el historial de vuelos de un pasajero")
    void testGetPassengerFlightHistory() throws TreeException, QueueException, ListException {
        String id = "GETHIST001";
        passengerManager.registerPassenger(id, "Laura", "Canadiense");
        Passenger passenger = passengerManager.searchPassenger(id);

        LocalDateTime departure1 = LocalDateTime.of(2025, 9, 10, 8, 0);
        Flight flight1 = new Flight("CA500", "YYZ", "YVR", departure1, 120);
        passengerManager.addFlightToPassengerHistory(id, flight1);

        LinkedQueue history = passengerManager.getPassengerFlightHistory(id);
        assertNotNull(history, "El historial no debe ser nulo.");
        assertFalse(history.isEmpty(), "El historial no debe estar vacío.");
        assertEquals(1, history.size(), "El historial debe contener 1 vuelo.");
        assertEquals(flight1, history.peek(), "El vuelo en el historial debe ser el esperado.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si el pasajero no se encuentra al obtener historial")
    void testGetPassengerFlightHistoryNotFound() throws TreeException {
        // Assert that an IllegalArgumentException is thrown
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> passengerManager.getPassengerFlightHistory("NONEXISTENTHISTORY"),
                "Debe lanzar IllegalArgumentException si el pasajero no existe."
        );

        // Optionally, check the exception message
        assertTrue(thrown.getMessage().contains("Pasajero con ID NONEXISTENTHISTORY no encontrado para obtener historial."),
                "El mensaje de excepción debe indicar que el pasajero no fue encontrado.");
    }
}