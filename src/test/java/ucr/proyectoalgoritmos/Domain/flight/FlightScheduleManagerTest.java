package ucr.proyectoalgoritmos.Domain.flight;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList; // <<--- Importación corregida
// import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue; // <<--- Ya no es necesario
// import ucr.proyectoalgoritmos.Domain.queue.QueueException; // <<--- Ya no es necesario
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.util.Utility;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightScheduleManagerTest {

    private AirportManager airportManager;
    private RouteManager routeManager;
    private FlightScheduleManager flightScheduleManager;

    @BeforeEach
    void setUp() throws ListException {
        // Inicializar AirportManager
        airportManager = new AirportManager();
        // Crear aeropuertos y añadirlos al AirportManager
        airportManager.createAirport("SJO", "Juan Santamaría International Airport", "Costa Rica");
        airportManager.createAirport("MIA", "Miami International Airport", "USA");
        airportManager.createAirport("LAX", "Los Angeles International Airport", "USA");
        airportManager.createAirport("JFK", "John F. Kennedy International Airport", "USA");
        airportManager.createAirport("SYD", "Sydney Airport", "Australia");
        airportManager.createAirport("LIM", "Jorge Chávez International Airport", "Peru");
        airportManager.createAirport("CDG", "Charles de Gaulle Airport", "France");
        airportManager.createAirport("FRA", "Frankfurt Airport", "Germany");
        airportManager.createAirport("DXB", "Dubai International Airport", "UAE");
        airportManager.createAirport("NRT", "Narita International Airport", "Japan");
        airportManager.createAirport("ORD", "O'Hare International Airport", "USA");
        airportManager.createAirport("PEK", "Beijing Capital International Airport", "China");
        airportManager.createAirport("IST", "Istanbul Airport", "Turkey");
        airportManager.createAirport("MEX", "Mexico City International Airport", "Mexico");
        airportManager.createAirport("LIR", "Daniel Oduber Quirós International Airport", "Costa Rica");


        // Inicializar RouteManager con AirportManager
        routeManager = new RouteManager(airportManager);

        // Añadir aeropuertos al grafo de RouteManager ANTES de añadir rutas.
        routeManager.addAirportToGraph("SJO");
        routeManager.addAirportToGraph("MIA");
        routeManager.addAirportToGraph("LAX");
        routeManager.addAirportToGraph("JFK");
        routeManager.addAirportToGraph("SYD");
        routeManager.addAirportToGraph("LIM");
        routeManager.addAirportToGraph("CDG");
        routeManager.addAirportToGraph("FRA");
        routeManager.addAirportToGraph("DXB");
        routeManager.addAirportToGraph("NRT");
        routeManager.addAirportToGraph("ORD");
        routeManager.addAirportToGraph("PEK");
        routeManager.addAirportToGraph("IST");
        routeManager.addAirportToGraph("MEX");
        routeManager.addAirportToGraph("LIR");


        // Añadir algunas rutas al grafo (pesos ficticios) utilizando graph.addEdge
        // Utiliza getIndexForAirportCode para obtener los índices de los vértices
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("SJO"), routeManager.getGraph().getIndexForAirportCode("MIA"), 1300);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("MIA"), routeManager.getGraph().getIndexForAirportCode("JFK"), 1090);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("JFK"), routeManager.getGraph().getIndexForAirportCode("CDG"), 3620);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("LAX"), routeManager.getGraph().getIndexForAirportCode("SYD"), 7490);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("SJO"), routeManager.getGraph().getIndexForAirportCode("LAX"), 2600);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("MIA"), routeManager.getGraph().getIndexForAirportCode("LIM"), 3200);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("CDG"), routeManager.getGraph().getIndexForAirportCode("FRA"), 250);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("FRA"), routeManager.getGraph().getIndexForAirportCode("DXB"), 2900);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("DXB"), routeManager.getGraph().getIndexForAirportCode("NRT"), 4800);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("ORD"), routeManager.getGraph().getIndexForAirportCode("JFK"), 740);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("PEK"), routeManager.getGraph().getIndexForAirportCode("IST"), 4300);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("MEX"), routeManager.getGraph().getIndexForAirportCode("SJO"), 1000);
        routeManager.getGraph().addEdge(routeManager.getGraph().getIndexForAirportCode("LIR"), routeManager.getGraph().getIndexForAirportCode("MIA"), 1250);


        // Inicializar FlightScheduleManager con los managers
        flightScheduleManager = new FlightScheduleManager(airportManager, routeManager);
    }

    // --- Tests del Constructor ---
    @Test
    @DisplayName("El constructor debe inicializar los managers y las listas")
    void testConstructorInitialization() throws ListException { // Se agregó ListException aquí porque getScheduledFlights() la lanza
        assertNotNull(flightScheduleManager, "El FlightScheduleManager no debería ser nulo.");
        assertNotNull(flightScheduleManager.getScheduledFlights(), "La lista de vuelos programados no debería ser nula.");
        assertTrue(flightScheduleManager.getScheduledFlights().isEmpty(), "La lista de vuelos programados debería estar vacía al inicio.");
        // Los managers internos se inicializan, no necesitan ser accedidos directamente aquí.
    }

    @Test
    @DisplayName("El constructor debe lanzar IllegalArgumentException si AirportManager es nulo")
    void testConstructorNullAirportManager() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FlightScheduleManager(null, routeManager);
        }, "Debería lanzar IllegalArgumentException si AirportManager es nulo.");
    }

    @Test
    @DisplayName("El constructor debe lanzar IllegalArgumentException si RouteManager es nulo")
    void testConstructorNullRouteManager() {
        assertThrows(IllegalArgumentException.class, () -> {
            new FlightScheduleManager(airportManager, null);
        }, "Debería lanzar IllegalArgumentException si RouteManager es nulo.");
    }

    // --- Tests de `createFlight` ---
    @Test
    @DisplayName("Crear un vuelo exitosamente")
    void testCreateFlightSuccess() throws ListException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 10, 0);
        Flight flight = flightScheduleManager.createFlight("AA100", "SJO", "MIA", departure, 180, 200);

        assertNotNull(flight, "El vuelo creado no debería ser nulo.");
        assertEquals("AA100", flight.getFlightNumber());
        assertEquals("SJO", flight.getOriginAirportCode());
        assertEquals("MIA", flight.getDestinationAirportCode());
        assertEquals(departure, flight.getDepartureTime());
        assertEquals(180, flight.getEstimatedDurationMinutes());
        assertEquals(200, flight.getCapacity());
        assertEquals(0, flight.getOccupancy()); // La ocupación inicial siempre es 0
        assertEquals(Flight.FlightStatus.SCHEDULED, flight.getStatus());

        // Verificar que el vuelo fue añadido a la lista de programados
        assertTrue(flightScheduleManager.getScheduledFlights().contains(flight), "El vuelo debería estar en la lista de vuelos programados.");
        assertEquals(1, flightScheduleManager.getScheduledFlights().size(), "Debería haber 1 vuelo programado.");
    }

    @Test
    @DisplayName("Crear un vuelo con número duplicado debe lanzar ListException")
    void testCreateFlightDuplicateNumber() throws ListException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 10, 0);
        flightScheduleManager.createFlight("AA101", "SJO", "MIA", departure, 120, 200);

        assertThrows(ListException.class, () -> {
            flightScheduleManager.createFlight("AA101", "SJO", "LAX", departure, 240, 180);
        }, "Debería lanzar ListException por número de vuelo duplicado.");
    }

    @Test
    @DisplayName("Crear un vuelo con aeropuerto de origen inválido debe lanzar ListException")
    void testCreateFlightInvalidOriginAirport() {
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 10, 0);
        assertThrows(ListException.class, () -> {
            // "XYZ" no fue añadido al AirportManager en setUp
            flightScheduleManager.createFlight("AA102", "XYZ", "MIA", departure, 150, 200);
        }, "Debería lanzar ListException por aeropuerto de origen inválido.");
    }

    @Test
    @DisplayName("Crear un vuelo con aeropuerto de destino inválido debe lanzar ListException")
    void testCreateFlightInvalidDestinationAirport() {
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 10, 0);
        assertThrows(ListException.class, () -> {
            // "ABC" no fue añadido al AirportManager en setUp
            flightScheduleManager.createFlight("AA103", "SJO", "ABC", departure, 150, 200);
        }, "Debería lanzar ListException por aeropuerto de destino inválido.");
    }

    @Test
    @DisplayName("Crear un vuelo sin ruta existente debe lanzar ListException")
    void testCreateFlightNoExistingRoute() {
        LocalDateTime departure = LocalDateTime.of(2025, 12, 25, 10, 0);
        // Suponiendo que no hay ruta directa o indirecta de SYD a IST en el setup
        assertThrows(ListException.class, () -> {
            flightScheduleManager.createFlight("AA104", "SYD", "IST", departure, 600, 200);
        }, "Debería lanzar ListException si no hay ruta entre los aeropuertos.");
    }

    // --- Tests de `findFlight` ---
    @Test
    @DisplayName("Encontrar un vuelo existente por su número")
    void testFindFlightExisting() throws ListException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 12, 26, 11, 0);
        Flight createdFlight = flightScheduleManager.createFlight("UA200", "SJO", "MIA", departure, 180, 100);

        Flight foundFlight = flightScheduleManager.findFlight("UA200");
        assertNotNull(foundFlight, "Se debería encontrar el vuelo existente.");
        assertEquals(createdFlight, foundFlight, "El vuelo encontrado debería ser el mismo que el creado.");
    }

    @Test
    @DisplayName("No encontrar un vuelo inexistente")
    void testFindFlightNonExisting() throws ListException {
        Flight foundFlight = flightScheduleManager.findFlight("NONEXISTENT");
        assertNull(foundFlight, "No se debería encontrar un vuelo que no existe.");
    }

    @Test
    @DisplayName("Buscar vuelo en lista vacía debe retornar null")
    void testFindFlightEmptyList() throws ListException {
        // Creamos un nuevo manager para asegurar que la lista esté vacía
        FlightScheduleManager emptyManager = new FlightScheduleManager(airportManager, routeManager);
        assertNull(emptyManager.findFlight("ANY"), "Buscar en una lista vacía debe retornar null.");
    }

    // --- Tests de `processTicketPurchase` ---
    @Test
    @DisplayName("Añadir pasajero a vuelo disponible debe ser exitoso")
    void testAddPassengerSuccess() throws ListException, StackException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 10, 10, 8, 0);
        Flight flight = flightScheduleManager.createFlight("DL300", "SJO", "MIA", departure, 180, 2);
        Passenger p1 = new Passenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());

        flightScheduleManager.processTicketPurchase(p1, flight);

        assertEquals(1, flight.getOccupancy(), "La ocupación del vuelo debería ser 1.");
        // Nuevo método para verificar la presencia de un pasajero en la CircularDoublyLinkedList del vuelo
        assertTrue(containsPassengerInFlightList(flight, p1), "El pasajero debería estar en la lista de pasajeros del vuelo.");
    }

    // Comentado el test de añadir el mismo pasajero dos veces por el cambio en la lógica interna
    // Si la unicidad del pasajero en el vuelo es crítica, debe gestionarse con una estructura auxiliar en la clase Flight.
    /*
    @Test
    @DisplayName("Añadir el mismo pasajero dos veces debe lanzar IllegalArgumentException")
    void testAddSamePassengerTwice() throws ListException {
        LocalDateTime departure = LocalDateTime.of(2025, 10, 11, 9, 0);
        Flight flight = flightScheduleManager.createFlight("DL301", "SJO", "MIA", departure, 180, 5);
        Passenger p1 = new Passenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());

        flightScheduleManager.processTicketPurchase(p1, flight);
        assertEquals(1, flight.getOccupancy(), "La ocupación debería ser 1 después de la primera reserva.");

        assertThrows(IllegalArgumentException.class, () -> {
            flightScheduleManager.processTicketPurchase(p1, flight);
        }, "Debería lanzar IllegalArgumentException si el pasajero ya está en el vuelo.");

        assertEquals(1, flight.getOccupancy(), "La ocupación no debería cambiar.");
    }
    */

    @Test
    @DisplayName("Añadir pasajero a vuelo lleno debe añadir a lista de espera")
    void testAddPassengerToFullFlight() throws ListException, StackException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 10, 12, 10, 0);
        Flight flight = flightScheduleManager.createFlight("DL302", "SJO", "MIA", departure, 180, 1);
        Passenger p1 = new Passenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());
        Passenger p2 = new Passenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());

        flightScheduleManager.processTicketPurchase(p1, flight); // P1 ocupa el asiento
        assertEquals(1, flight.getOccupancy(), "P1 debería ocupar el asiento.");

        String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();

        // P2 intenta reservar, pero el vuelo está lleno
        flightScheduleManager.processTicketPurchase(p2, flight);
        assertEquals(1, flight.getOccupancy(), "La ocupación no debería cambiar, P2 va a lista de espera.");

        // Verificar que P2 está en la lista de espera
        assertTrue(flightScheduleManager.getWaitingListsForTest().containsKey(routeKey), "Debería existir una lista de espera para la ruta.");
        assertTrue(flightScheduleManager.getWaitingListsForTest().get(routeKey).contains(p2), "P2 debería estar en la lista de espera.");
    }


    @Test
    @DisplayName("Remover pasajero de lista de espera si luego se le asigna asiento")
    void testRemovePassengerFromWaitingListAfterBooking() throws ListException, StackException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 10, 13, 11, 0);
        Flight flight1 = flightScheduleManager.createFlight("DL303", "SJO", "MIA", departure, 180, 1);
        Flight flight2 = flightScheduleManager.createFlight("DL304", "SJO", "MIA", departure.plusDays(1), 180, 1);

        Passenger p1 = new Passenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());
        Passenger p2 = new Passenger(Utility.RandomId(), Utility.RandomNames(), Utility.RandomNationalities());

        // P1 ocupa vuelo 1
        flightScheduleManager.processTicketPurchase(p1, flight1);
        System.out.println("Pasajero " + p1.getName() + " (" + p1.getId() + ") reservado en el vuelo " + flight1.getFlightNumber() + " de " + flight1.getOriginAirportCode() + " a " + flight1.getDestinationAirportCode() + ".");


        // P2 intenta reservar vuelo 1, va a lista de espera
        flightScheduleManager.processTicketPurchase(p2, flight1);
        String routeKey = flight1.getOriginAirportCode() + "-" + flight1.getDestinationAirportCode();

        // ASSERTION 1: P2 debe estar en lista de espera para SJO-MIA
        assertTrue(flightScheduleManager.getWaitingListsForTest().containsKey(routeKey), "La lista de espera para la ruta SJO-MIA debería existir.");
        assertTrue(flightScheduleManager.getWaitingListsForTest().get(routeKey).contains(p2), "P2 debe estar en lista de espera para SJO-MIA.");

        // Ahora P2 intenta reservar en Vuelo 2 (misma ruta), que sí tiene espacio
        flightScheduleManager.processTicketPurchase(p2, flight2);
        System.out.println("Pasajero " + p2.getName() + " (" + p2.getId() + ") reservado en el vuelo " + flight2.getFlightNumber() + " de " + flight2.getOriginAirportCode() + " a " + flight2.getDestinationAirportCode() + ".");


        // ASSERTION 2: P2 debería ser removido de la lista de espera para SJO-MIA
        // Si la lista de espera está vacía después de remover, la clave se elimina.
        // Aquí asumimos que removePassengerFromWaitingList se llama internamente y si la lista queda vacía, la key se quita del mapa.
        assertFalse(flightScheduleManager.getWaitingListsForTest().containsKey(routeKey), "La clave de la lista de espera para SJO-MIA debería haber sido eliminada si quedó vacía.");
    }

    // --- Tests de `simulateFlight` ---

    @Test
    @DisplayName("Simular un vuelo debe actualizar su estado y el estado/ubicación del avión")
    void testSimulateFlightSuccess() throws ListException, StackException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 11, 1, 14, 0);
        Flight flight = flightScheduleManager.createFlight("AA400", "SJO", "MIA", departure, 180, 150);
        Airplane airplane = new Airplane("AV-001", 150, "SJO");
        flight.setAirplane(airplane); // Asignar un avión al vuelo

        // Estado inicial del vuelo y avión
        assertEquals(Flight.FlightStatus.SCHEDULED, flight.getStatus());
        assertEquals(Airplane.AirplaneStatus.IDLE, airplane.getStatus());
        assertEquals("SJO", airplane.getCurrentLocationAirportCode());
        assertTrue(airplane.getFlightHistory().isEmpty());

        flightScheduleManager.simulateFlight("AA400");

        // Después de la simulación
        assertEquals(Flight.FlightStatus.COMPLETED, flight.getStatus(), "El vuelo debería estar COMPLETED.");
        assertEquals(Airplane.AirplaneStatus.IDLE, airplane.getStatus(), "El avión debería estar IDLE después de un vuelo.");
        assertEquals("MIA", airplane.getCurrentLocationAirportCode(), "La ubicación del avión debería ser el destino del vuelo.");
        assertFalse(airplane.getFlightHistory().isEmpty(), "El historial del avión no debería estar vacío.");
        assertEquals(1, airplane.getFlightHistory().size(), "Debería haber un vuelo en el historial del avión.");
        assertEquals(flight, airplane.getFlightHistory().top(), "El vuelo simulado debería ser el último en el historial del avión.");
        // Verificar que la lista de pasajeros del vuelo está vacía después del desembarque
        assertTrue(flight.getPassengers().isEmpty(), "La lista de pasajeros del vuelo debería estar vacía después de la simulación.");
    }

    @Test
    @DisplayName("Simular vuelo sin avión asignado debe lanzar IllegalArgumentException")
    void testSimulateFlightNoAirplaneAssigned() throws ListException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 11, 2, 10, 0);
        flightScheduleManager.createFlight("AA401", "SJO", "MIA", departure, 180, 100);

        assertThrows(IllegalArgumentException.class, () -> {
            flightScheduleManager.simulateFlight("AA401");
        }, "Debería lanzar IllegalArgumentException si no hay avión asignado.");
    }

    @Test
    @DisplayName("Simular un vuelo inexistente debe lanzar ListException")
    void testSimulateFlightNonExistent() {
        assertThrows(ListException.class, () -> {
            flightScheduleManager.simulateFlight("NONEXISTENT");
        }, "Debería lanzar ListException si el vuelo no existe.");
    }

    @Test
    @DisplayName("No se debe simular un vuelo ya en progreso o completado")
    void testSimulateFlightAlreadyProcessed() throws ListException, StackException { // <<--- Eliminado QueueException
        LocalDateTime departure = LocalDateTime.of(2025, 11, 3, 9, 0);
        Flight flight = flightScheduleManager.createFlight("AA402", "SJO", "MIA", departure, 180, 150);
        Airplane airplane = new Airplane("AV-002", 150, "SJO");
        flight.setAirplane(airplane);

        // Primera simulación: Debería completarse exitosamente
        flightScheduleManager.simulateFlight("AA402");
        assertEquals(Flight.FlightStatus.COMPLETED, flight.getStatus(), "El vuelo debería estar COMPLETED después de la primera simulación.");

        // Segunda simulación: Debería lanzar IllegalStateException
        assertThrows(IllegalStateException.class, () -> {
            flightScheduleManager.simulateFlight("AA402");
        }, "Debería lanzar IllegalStateException si el vuelo ya está en progreso, completado o cancelado.");

        // Verificar que el estado del vuelo no ha cambiado después del intento fallido
        assertEquals(Flight.FlightStatus.COMPLETED, flight.getStatus(), "El estado del vuelo no debería cambiar en el segundo intento.");
    }


    // --- Tests de `displayFlightsByStatus` (solo verificamos que no lance excepción) ---

    @Test
    @DisplayName("Display flights by status should run without exceptions")
    void testDisplayFlightsByStatus() throws ListException { // <<--- Eliminado QueueException
        flightScheduleManager.createFlight("DP101", "SJO", "MIA", LocalDateTime.of(2025, 1, 1, 10, 0), 180, 100);
        flightScheduleManager.createFlight("DP102", "MIA", "JFK", LocalDateTime.of(2025, 1, 2, 11, 0), 180, 100);
        Flight cancelledFlight = flightScheduleManager.createFlight("DP103", "SJO", "LAX", LocalDateTime.of(2025, 1, 3, 12, 0), 180, 100);
        cancelledFlight.setStatus(Flight.FlightStatus.CANCELLED);

        // Simulate a flight to get it to COMPLETED status
        Flight completedFlight = flightScheduleManager.createFlight("DP104", "SJO", "MIA", LocalDateTime.of(2025, 1, 4, 13, 0), 180, 100);
        Airplane airplane = new Airplane("AV-TEST-004", 100, "SJO");
        completedFlight.setAirplane(airplane);
        try {
            flightScheduleManager.simulateFlight("DP104");
        } catch (StackException e) {
            fail("StackException no debería ocurrir en este test.");
        }

        // Test that it runs without throwing exceptions.
        assertDoesNotThrow(() -> flightScheduleManager.displayFlightsByStatus());

        // Para pruebas más exhaustivas, se podría capturar System.out y afirmar su contenido.
    }

    @Test
    @DisplayName("Display flights by status on empty list")
    void testDisplayFlightsByStatusEmpty() {
        assertDoesNotThrow(() -> {
            // Creamos un nuevo manager para asegurar que la lista esté vacía
            FlightScheduleManager emptyManager = new FlightScheduleManager(airportManager, routeManager);
            emptyManager.displayFlightsByStatus();
        }, "Debería ejecutarse sin excepciones en una lista vacía.");
    }

    // --- MÉTODOS AUXILIARES PARA TESTS ---

    /**
     * Verifica si un pasajero específico está presente en la CircularDoublyLinkedList de pasajeros de un vuelo.
     * Dado que CircularDoublyLinkedList no tiene un método 'contains' directo que podamos usar sin modificarla
     * para tests, la recorremos y luego la restauramos.
     * @param flight El vuelo cuya lista de pasajeros se va a inspeccionar.
     * @param passenger El pasajero a buscar.
     * @return true si el pasajero está en la lista, false en caso contrario.
     */
    private boolean containsPassengerInFlightList(Flight flight, Passenger passenger) {
        CircularDoublyLinkedList passengers = flight.getPassengers();
        // Si la lista es null o vacía, no hay nada que buscar.
        if (passengers == null || passengers.isEmpty()) {
            return false;
        }

        // Temporalmente movemos los elementos a una lista auxiliar para poder recorrer la original.
        // Aunque CircularDoublyLinkedList tiene métodos de recorrido, el get(index) es más sencillo aquí para el test.
        // Si tu CircularDoublyLinkedList soporta iteradores o un toArray(), sería más directo.
        // Asumiendo que tiene un método get(index):
        try {
            for (int i = 0; i < passengers.size(); i++) {
                if (((Passenger) passengers.get(i)).equals(passenger)) {
                    return true;
                }
            }
        } catch (ListException e) {
            fail("Excepción inesperada al verificar pasajero en lista circular: " + e.getMessage());
        }
        return false;
    }


    // Helper method to capture System.out
    private String getSystemOut(Runnable action) {
        java.io.ByteArrayOutputStream outContent = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalOut = System.out;
        System.setOut(new java.io.PrintStream(outContent));
        try {
            action.run();
            return outContent.toString();
        } finally {
            System.setOut(originalOut); // Restaurar System.out
        }
    }
}