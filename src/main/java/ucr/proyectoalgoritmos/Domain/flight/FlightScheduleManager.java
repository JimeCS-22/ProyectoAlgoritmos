package ucr.proyectoalgoritmos.Domain.flight;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException; // Importar ListException para errores de lista
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.Domain.route.RouteManager;
import ucr.proyectoalgoritmos.UtilJson.FlightJson;
// import ucr.proyectoalgoritmos.Domain.queue.QueueException; // REMOVIDO: Ya no es necesario si Flight usa CircularDoublyLinkedList

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static ucr.proyectoalgoritmos.UtilJson.FlightJson.saveFlightsToJson;

/**
 * Gestiona la programación, creación, asignación y simulación de vuelos.
 * Coordina con AirportManager y RouteManager para validar la información de vuelos.
 */
public class FlightScheduleManager {
    private CircularDoublyLinkedList scheduledFlights;
    private AirportManager airportManager;
    private RouteManager routeManager;
    private Map<String, ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList> waitingLists;
    private static FlightScheduleManager instance;
    private FlightJson flightJson;

    public FlightScheduleManager(AirportManager airportManager, RouteManager routeManager) {
        if (airportManager == null) {
            throw new IllegalArgumentException("AirportManager no puede ser nulo.");
        }
        if (routeManager == null) {
            throw new IllegalArgumentException("RouteManager no puede ser nulo.");
        }
        this.scheduledFlights = new CircularDoublyLinkedList();
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.waitingLists = new HashMap<>();
    }

    /**
     * **Para propósitos de prueba.**
     */
    public Map<String, ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList> getWaitingListsForTest() {
        return waitingLists;
    }

    /**
     * Crea un nuevo vuelo y lo añade a la lista de vuelos programados.
     * Realiza validaciones de número de vuelo, existencia de aeropuertos y rutas.
     */
    public Flight createFlight(String number, String originCode, String destinationCode,
                               LocalDateTime departureTime, int estimatedDurationMinutes, int capacity) throws ListException, IllegalArgumentException {
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
        Flight newFlight = new Flight(number, originCode, destinationCode, departureTime, capacity);
        newFlight.setEstimatedDurationMinutes(estimatedDurationMinutes);

        // Añade el vuelo a la lista de vuelos programados
        scheduledFlights.add(newFlight);
        return newFlight;
    }

    /**
     * Procesa la compra de un billete para un pasajero en un vuelo específico.
     * Maneja la adición de pasajeros a vuelos y la gestión de listas de espera.
     */
    public void processTicketPurchase(Passenger passenger, Flight flight) throws IllegalArgumentException, ListException {
        if (flight == null) {
            throw new IllegalArgumentException("No se puede procesar la compra de billetes. El objeto vuelo es nulo.");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("No se puede procesar la compra de billetes. El objeto pasajero es nulo.");
        }

        // Si el vuelo está programado y no está lleno
        if (flight.getStatus() == Flight.FlightStatus.SCHEDULED && !flight.isFull()) {
            try {
                flight.addPassenger(passenger); // Intenta añadir al pasajero al vuelo.
                System.out.println("Pasajero " + passenger.getName() + " (" + passenger.getId() + ") reservado en el vuelo " + flight.getFlightNumber() + " de " + flight.getOriginAirportCode() + " a " + flight.getDestinationAirportCode() + ".");

                // Si el pasajero estaba en lista de espera para esta ruta, se le remueve.
                String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
                if (waitingLists.containsKey(routeKey)) {
                    ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList routeWaitingList = waitingLists.get(routeKey);
                    try {
                        if (routeWaitingList.contains(passenger)) {
                            routeWaitingList.remove(passenger);
                            if (routeWaitingList.isEmpty()) {
                                waitingLists.remove(routeKey);
                            }
                        }
                    } catch (ListException e) {
                        System.err.println("ERROR: Error al intentar remover pasajero de la lista de espera para la ruta " + routeKey + ": " + e.getMessage());
                    }
                }
            } catch (ListException e) {
                // Si flight.addPassenger lanza ListException (ej. por vuelo lleno o pasajero duplicado), se maneja aquí
                System.out.println("ADVERTENCIA: No se pudo reservar al pasajero " + passenger.getId() + " en el vuelo " + flight.getFlightNumber() + ". Detalle: " + e.getMessage() + ". Añadiendo a lista de espera.");
                addToWaitingList(passenger, flight); // Añadir a la lista de espera si no se pudo reservar
            }

        } else { // El vuelo no está disponible para reserva (lleno o estado no 'SCHEDULED')
            System.out.println("ADVERTENCIA: El vuelo " + flight.getFlightNumber() + " no está disponible para reserva (Estado: " + flight.getStatus() + ", Ocupación: " + flight.getOccupancy() + "/" + flight.getCapacity() + "). Pasajero " + passenger.getId() + " añadido a lista de espera.");
            addToWaitingList(passenger, flight);
        }
    }

    /**
     * Método auxiliar para añadir un pasajero a la lista de espera de una ruta específica.
     */
    private void addToWaitingList(Passenger passenger, Flight flight) throws ListException {
        String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
        waitingLists.putIfAbsent(routeKey, new ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList()); // Crea la lista si no existe
        ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList routeWaitingList = waitingLists.get(routeKey);

        if (!routeWaitingList.contains(passenger)) { // Evitar duplicados en la lista de espera
            routeWaitingList.add(passenger);
            System.out.println("Pasajero " + passenger.getName() + " (" + passenger.getId() + ") añadido a la lista de espera para la ruta " + routeKey + ".");
        } else {
            System.out.println("Pasajero " + passenger.getName() + " (" + passenger.getId() + ") ya está en la lista de espera para la ruta " + routeKey + ".");
        }
    }


    /**
     * Busca un vuelo programado por su número de vuelo.
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

        // Paso 2: Vuelo completado
        flight.setStatus(Flight.FlightStatus.COMPLETED);
        assignedAirplane.setCurrentLocationAirportCode(flight.getDestinationAirportCode()); // Actualizar ubicación del avión
        assignedAirplane.setStatus(Airplane.AirplaneStatus.IDLE); // El avión está de nuevo disponible
        assignedAirplane.addFlightToHistory(flight); // Añadir el vuelo al historial del avión

        // Desembarcar a todos los pasajeros del vuelo
        try {
            while (!flight.getPassengers().isEmpty()) {
                // Obtener el primer pasajero y luego removerlo
                Passenger p = (Passenger) flight.getPassengers().get(0); // Get the first passenger
                flight.removePassenger(p); // Remove that specific passenger
                System.out.println("  - Pasajero " + p.getName() + " (" + p.getId() + ") desembarcado.");
                // Aquí podrías añadir lógica para actualizar el historial de vuelos del pasajero si tienes un PassengerManager global.
            }
            System.out.println("Todos los pasajeros han sido desembarcados del vuelo " + flightNumber + ".");
        } catch (ListException e) {
            System.err.println("ERROR al desembarcar pasajeros del vuelo " + flightNumber + ": " + e.getMessage());
        }


        System.out.println("Vuelo " + flightNumber + ": Aterrizando en " + flight.getDestinationAirportCode() + ".");
        System.out.println("Simulación para el vuelo " + flightNumber + " completada. Avión " + assignedAirplane.getId() + " ahora en " + assignedAirplane.getCurrentLocationAirportCode() + ".");
    }

    /**
     * Muestra las listas de espera actuales para todas las rutas.
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
            // Imprimir los IDs y nombres de los pasajeros en la lista de espera
            for (int i = 0; i < list.size(); i++) {
                Passenger p = (Passenger) list.get(i);
                System.out.println("  - " + p.getId() + " (" + p.getName() + ")");
            }
        }
    }


    public static synchronized FlightScheduleManager getInstance(AirportManager airportManager, RouteManager routeManager) {
        if (instance == null) {
            instance = new FlightScheduleManager(airportManager, routeManager);
            instance.setScheduledFlights(instance.flightJson.loadFlightsFromJson(airportManager, routeManager));
        }
        return instance;
    }

    public void setScheduledFlights(CircularDoublyLinkedList scheduledFlights) {
        this.scheduledFlights = scheduledFlights;
    }

    public void addFlight(Flight newFlight) {
        try {
            this.scheduledFlights.add(newFlight);
        } catch (Exception e) {

            System.err.println("Error al añadir vuelo a la lista de vuelos programados: " + e.getMessage());

        }
    }

    public CircularDoublyLinkedList getAllFlights() {

        return this.scheduledFlights;
    }

    public CircularDoublyLinkedList getScheduledFlights() { // <-- CAMBIO AQUÍ TAMBIÉN EL TIPO DE RETORNO
        return scheduledFlights;
    }

    public void reloadFlightsFromJson() {
        // Este método fuerza la relectura del archivo JSON
        this.scheduledFlights = flightJson.loadFlightsFromJson(this.airportManager, this.routeManager);
        if (this.scheduledFlights == null) {
            this.scheduledFlights = new CircularDoublyLinkedList();
        }
        System.out.println("Vuelos recargados desde JSON.");
    }

    public boolean removeFlight(String flightNumberToDelete) throws ListException {

        if (flightNumberToDelete == null || flightNumberToDelete.trim().isEmpty()) {
            return false;
        }

        reloadFlightsFromJson();

        Flight flightToRemove = findFlight(flightNumberToDelete);

        if (flightToRemove != null) {

            boolean removed = scheduledFlights.remove(flightToRemove);

            if (removed) {

                saveFlightsToJson(this.scheduledFlights);
                return true;
            }
        }
        return false;

    }

    public boolean updateFlight(Flight flightToUpdate) throws ListException {
        if (flightToUpdate == null || flightToUpdate.getFlightNumber().trim().isEmpty()) {

            return false;
        }

        reloadFlightsFromJson();

        int indexToUpdate = -1;
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Object obj = scheduledFlights.get(i);
            if (obj instanceof Flight) {
                Flight currentFlightInList = (Flight) obj;

                if (currentFlightInList.getFlightNumber().equalsIgnoreCase(flightToUpdate.getFlightNumber())) {
                    indexToUpdate = i;
                    break;
                }
            }
        }

        if (indexToUpdate != -1) {

            scheduledFlights.set(indexToUpdate, flightToUpdate);

            saveFlightsToJson(this.scheduledFlights);
            return true;
        }

        return false;
    }
}