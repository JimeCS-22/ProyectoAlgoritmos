package ucr.proyectoalgoritmos.Domain.flight;

// Ajustar las importaciones
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList; // Usar DoublyLinkedList estándar
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.Domain.queue.QueueException; // Importar QueueException

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestiona la programación, creación, asignación y simulación de vuelos.
 * Coordina con AirportManager y RouteManager para validar la información de vuelos.
 */
public class FlightScheduleManager {
    private DoublyLinkedList scheduledFlights; // CAMBIO: Ahora DoublyLinkedList
    private AirportManager airportManager; // Manager para la gestión de aeropuertos
    private RouteManager routeManager; // Manager para la gestión de rutas de grafos
    // Mantiene CircularDoublyLinkedList para waitingLists si necesitas contains/remove específicos
    private Map<String, ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList> waitingLists; // Mantiene CircularDoublyLinkedList

    /**
     * Constructor para FlightScheduleManager.
     *
     * @param airportManager Instancia de AirportManager para validar aeropuertos.
     * @param routeManager Instancia de RouteManager para validar rutas.
     * @throws IllegalArgumentException Si alguno de los managers es nulo.
     */
    public FlightScheduleManager(AirportManager airportManager, RouteManager routeManager) {
        if (airportManager == null) {
            throw new IllegalArgumentException("AirportManager no puede ser nulo.");
        }
        if (routeManager == null) {
            throw new IllegalArgumentException("RouteManager no puede ser nulo.");
        }
        this.scheduledFlights = new DoublyLinkedList(); // CAMBIO: Inicializa DoublyLinkedList
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.waitingLists = new HashMap<>();
    }

    /**
     * Obtiene la lista de vuelos programados.
     *
     * @return Una DoublyLinkedList que contiene los vuelos programados.
     */
    public DoublyLinkedList getScheduledFlights() { // CAMBIO: Retorna DoublyLinkedList
        return scheduledFlights;
    }

    /**
     * **Para propósitos de prueba.**
     * Proporciona acceso al mapa interno de listas de espera.
     * En un entorno de producción, es preferible no exponer esta estructura directamente.
     *
     * @return El mapa de listas de espera.
     */
    public Map<String, ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList> getWaitingListsForTest() {
        return waitingLists;
    }

    /**
     * Crea un nuevo vuelo y lo añade a la lista de vuelos programados.
     * Realiza validaciones de número de vuelo, existencia de aeropuertos y rutas.
     *
     * @param number El número único de vuelo (ej. "AA123").
     * @param originCode El código IATA del aeropuerto de origen.
     * @param destinationCode El código IATA del aeropuerto de destino.
     * @param departureTime La fecha y hora de salida programada del vuelo.
     * @param capacity La capacidad de pasajeros del vuelo.
     * @return El objeto Flight recién creado y añadido.
     * @throws ListException Si el número de vuelo ya está en uso, los aeropuertos no son válidos
     * o no existe una ruta válida.
     * @throws IllegalArgumentException Si algún parámetro de entrada es nulo o inválido.
     * @throws QueueException Si ocurre un error al crear la cola de pasajeros del vuelo.
     */
    public Flight createFlight(String number, String originCode, String destinationCode,
                               LocalDateTime departureTime, int estimatedDurationMinutes, int capacity) throws ListException, IllegalArgumentException, QueueException {

        // Validar si el número de vuelo ya está en uso
        if (findFlight(number) != null) {
            throw new ListException("El número de vuelo " + number + " ya está en uso.");
        }

        // Validar que los aeropuertos de origen y destino existan
        if (airportManager.findAirport(originCode) == null) {
            throw new ListException("El aeropuerto de origen '" + originCode + "' no es válido.");
        }
        if (airportManager.findAirport(destinationCode) == null) {
            throw new ListException("El aeropuerto de destino '" + destinationCode + "' no es válido.");
        }

        // Validar que exista una ruta en el grafo entre origen y destino
        if (routeManager.calculateShortestRoute(originCode, destinationCode) == Integer.MAX_VALUE) {
            throw new ListException("No existe una ruta en el grafo entre " + originCode + " y " + destinationCode + ". No se puede crear el vuelo.");
        }

        // Crea el nuevo objeto Flight
        // El constructor de Flight ahora puede lanzar QueueException
        Flight newFlight = new Flight(number, originCode, destinationCode, departureTime, capacity);
        newFlight.setEstimatedDurationMinutes(estimatedDurationMinutes); // Asegurarse de setear la duración

        // Añade el vuelo a la lista de vuelos programados
        scheduledFlights.add(newFlight);
        return newFlight;
    }

    /**
     * Procesa la compra de un billete para un pasajero en un vuelo específico.
     * Maneja la adición de pasajeros a vuelos y la gestión de listas de espera.
     *
     * @param passenger El objeto {@link Passenger} que desea comprar un billete.
     * @param flight El objeto {@link Flight} en el que se desea reservar.
     * @throws IllegalArgumentException Si el vuelo o el pasajero son nulos.
     * @throws ListException Si ocurre un error con la lista de espera (no con la lista de pasajeros del vuelo directamente).
     * @throws QueueException Si el vuelo está lleno y no se puede añadir al pasajero.
     */
    public void processTicketPurchase(Passenger passenger, Flight flight) throws IllegalArgumentException, ListException, QueueException {
        if (flight == null) {
            throw new IllegalArgumentException("No se puede procesar la compra de billetes. El objeto vuelo es nulo.");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("No se puede procesar la compra de billetes. El objeto pasajero es nulo.");
        }

        // El check de 'flight.getPassengers().contains(passenger)' se remueve
        // porque Flight ahora usa LinkedQueue, que no tiene un contains eficiente.
        // Asumimos que la lógica de negocio a este nivel asegura que un pasajero
        // no intente comprar el mismo billete dos veces, o que la unicidad
        // dentro de la cola del vuelo no es un requisito directo de la cola misma.

        // Si el vuelo está programado y no está lleno
        if (flight.getStatus() == Flight.FlightStatus.SCHEDULED && !flight.isFull()) {
            try {
                flight.addPassenger(passenger); // Intenta añadir al pasajero al vuelo. Ahora lanza QueueException.
                System.out.println("Pasajero " + passenger.getName() + " (" + passenger.getId() + ") reservado en el vuelo " + flight.getFlightNumber() + " de " + flight.getOriginAirportCode() + " a " + flight.getDestinationAirportCode() + ".");

                // Si el pasajero estaba en lista de espera para esta ruta, se le remueve.
                String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
                if (waitingLists.containsKey(routeKey)) {
                    ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList routeWaitingList = waitingLists.get(routeKey);
                    try {
                        if (routeWaitingList.contains(passenger)) { // Esto sigue funcionando porque waitingLists es CircularDoublyLinkedList
                            routeWaitingList.remove(passenger);
                            if (routeWaitingList.isEmpty()) {
                                waitingLists.remove(routeKey);
                            }
                        }
                    } catch (ListException e) {
                        System.err.println("ERROR: Error al intentar remover pasajero de la lista de espera para la ruta " + routeKey + ": " + e.getMessage());
                    }
                }
            } catch (QueueException e) {
                // Si flight.addPassenger lanza QueueException (ej. por vuelo lleno), se maneja aquí
                System.out.println("ADVERTENCIA: El vuelo " + flight.getFlightNumber() + " está lleno. Pasajero " + passenger.getId() + " añadido a lista de espera. Detalle: " + e.getMessage());
                // Caerá al else if si no se maneja, o se puede forzar aquí a añadir a la lista de espera
                addToWaitingList(passenger, flight); // Llamar a un método auxiliar para manejar la lista de espera
            }

        } else { // El vuelo no está disponible para reserva (lleno o estado no 'SCHEDULED')
            System.out.println("ADVERTENCIA: El vuelo " + flight.getFlightNumber() + " no está disponible para reserva (Estado: " + flight.getStatus() + ", Ocupación: " + flight.getOccupancy() + "/" + flight.getCapacity() + "). Pasajero " + passenger.getId() + " añadido a lista de espera.");
            addToWaitingList(passenger, flight); // Llamar a un método auxiliar
        }
    }

    /**
     * Método auxiliar para añadir un pasajero a la lista de espera de una ruta específica.
     * @param passenger Pasajero a añadir.
     * @param flight Vuelo para el que se está en lista de espera.
     * @throws ListException Si ocurre un error con la lista de espera.
     */
    private void addToWaitingList(Passenger passenger, Flight flight) throws ListException {
        String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
        waitingLists.putIfAbsent(routeKey, new ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList()); // Crea la lista si no existe
        ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList routeWaitingList = waitingLists.get(routeKey);

        if (!routeWaitingList.contains(passenger)) { // Evitar duplicados en la lista de espera
            routeWaitingList.add(passenger);
        }
    }


    /**
     * Busca un vuelo programado por su número de vuelo.
     *
     * @param flightNumber El número de vuelo a buscar.
     * @return El objeto {@link Flight} si se encuentra, o {@code null} si no existe.
     * @throws ListException Si ocurre un error al acceder a la lista de vuelos.
     */
    public Flight findFlight(String flightNumber) throws ListException {
        if (scheduledFlights.isEmpty()) {
            return null;
        }
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight f = (Flight) scheduledFlights.get(i);
            if (f.getFlightNumber().equals(flightNumber)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Muestra todos los vuelos programados, categorizados por su estado.
     *
     * @throws ListException Si ocurre un error al acceder a la lista de vuelos.
     */
    public void displayFlightsByStatus() throws ListException {
        if (scheduledFlights.isEmpty()) {
            System.out.println("No hay vuelos programados en el sistema.");
            return;
        }

        System.out.println("\n--- VUELOS ACTIVOS (Programados, Asignados y en Progreso) ---");
        boolean activeFound = false;
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight flight = (Flight) scheduledFlights.get(i);
            if (flight.getStatus() == Flight.FlightStatus.SCHEDULED ||
                    flight.getStatus() == Flight.FlightStatus.IN_PROGRESS ||
                    flight.getStatus() == Flight.FlightStatus.ASSIGNED) {
                System.out.println(flight);
                activeFound = true;
            }
        }
        if (!activeFound) {
            System.out.println("No hay vuelos activos actualmente.");
        }

        System.out.println("\n--- VUELOS COMPLETADOS ---");
        boolean completedFound = false;
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight flight = (Flight) scheduledFlights.get(i);
            if (flight.getStatus() == Flight.FlightStatus.COMPLETED) {
                System.out.println(flight);
                completedFound = true;
            }
        }
        if (!completedFound) {
            System.out.println("No hay vuelos completados actualmente.");
        }

        System.out.println("\n--- VUELOS CANCELADOS ---");
        boolean cancelledFound = false;
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight flight = (Flight) scheduledFlights.get(i);
            if (flight.getStatus() == Flight.FlightStatus.CANCELLED) {
                System.out.println(flight);
                cancelledFound = true;
            }
        }
        if (!cancelledFound) {
            System.out.println("No hay vuelos cancelados actualmente.");
        }
    }

    /**
     * Simula el progreso de un vuelo desde despegue hasta aterrizaje.
     * Actualiza el estado del vuelo y el estado y ubicación del avión asignado.
     *
     * @param flightNumber El número de vuelo a simular.
     * @throws ListException Si el vuelo no se encuentra o hay un problema en la lista.
     * @throws StackException Si ocurre un error al añadir el vuelo al historial del avión.
     * @throws IllegalArgumentException Si el avión asignado es nulo.
     * @throws IllegalStateException Si el vuelo ya está en progreso, completado o cancelado.
     */
    public void simulateFlight(String flightNumber) throws ListException, StackException {
        Flight flight = findFlight(flightNumber);
        if (flight == null) {
            throw new ListException("Vuelo " + flightNumber + " no encontrado para simular.");
        }

        // Previene la simulación de vuelos ya en progreso o completados/cancelados
        if (flight.getStatus() == Flight.FlightStatus.IN_PROGRESS ||
                flight.getStatus() == Flight.FlightStatus.COMPLETED ||
                flight.getStatus() == Flight.FlightStatus.CANCELLED) {
            throw new IllegalStateException("El vuelo " + flightNumber + " ya está en progreso, completado o cancelado. No se puede simular de nuevo.");
        }

        // Asegurarse de que el vuelo tenga un avión asignado para simular
        Airplane assignedAirplane = flight.getAirplane();
        if (assignedAirplane == null) {
            throw new IllegalArgumentException("No se puede simular el vuelo " + flightNumber + " sin un avión asignado.");
        }

        System.out.println("\nIniciando simulación para el vuelo " + flightNumber + "...");

        // Paso 1: Vuelo en progreso
        flight.setStatus(Flight.FlightStatus.IN_PROGRESS);
        assignedAirplane.setStatus(Airplane.AirplaneStatus.IN_FLIGHT); // Actualizar estado del avión
        System.out.println("Vuelo " + flightNumber + ": Despegando de " + flight.getOriginAirportCode() + " con avión " + assignedAirplane.getId() + ".");

        // Simulación de tiempo de vuelo (simplificado)
        // Puedes añadir Thread.sleep() aquí para una simulación más realista

        // Paso 2: Vuelo completado
        flight.setStatus(Flight.FlightStatus.COMPLETED);
        assignedAirplane.setCurrentLocationAirportCode(flight.getDestinationAirportCode()); // Actualizar ubicación del avión
        assignedAirplane.setStatus(Airplane.AirplaneStatus.IDLE); // El avión está de nuevo disponible
        assignedAirplane.addFlightToHistory(flight); // Añadir el vuelo al historial del avión

        System.out.println("Vuelo " + flightNumber + ": Aterrizando en " + flight.getDestinationAirportCode() + ".");
        System.out.println("Simulación para el vuelo " + flightNumber + " completada. Avión " + assignedAirplane.getId() + " ahora en " + assignedAirplane.getCurrentLocationAirportCode() + ".");
    }

    /**
     * Muestra las listas de espera actuales para todas las rutas.
     *
     * @throws ListException Si ocurre un error al acceder a las listas de espera.
     */
    public void displayWaitingLists() throws ListException {
        if (waitingLists.isEmpty()) {
            System.out.println("No hay listas de espera activas.");
            return;
        }

        System.out.println("\n--- LISTAS DE ESPERA POR RUTA ---");
        for (Map.Entry<String, ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList> entry : waitingLists.entrySet()) {
            String routeKey = entry.getKey();
            ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList list = entry.getValue();
            System.out.println("Ruta: " + routeKey + " (" + list.size() + " pasajeros en espera)");
            // Puedes imprimir los IDs de los pasajeros si lo deseas:
            // for (int i = 0; i < list.size(); i++) {
            //     Passenger p = (Passenger) list.get(i);
            //     System.out.println("  - " + p.getId() + " (" + p.getName() + ")");
            // }
        }
    }
}