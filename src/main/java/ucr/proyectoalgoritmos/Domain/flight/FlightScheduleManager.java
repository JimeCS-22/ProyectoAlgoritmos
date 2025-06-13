package ucr.proyectoalgoritmos.Domain.flight;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList; // Import your CircularDoublyLinkedList
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
// import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // No longer needed for flights/waiting lists here
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.stack.StackException; // Keep for now, but will likely be removed if not needed by PassengerManager
import ucr.proyectoalgoritmos.route.RouteManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FlightScheduleManager {
    private CircularDoublyLinkedList scheduledFlights; // Use CircularDoublyLinkedList as required
    private AirportManager airportManager;
    private RouteManager routeManager;
    private Map<String, CircularDoublyLinkedList> waitingLists; // Waiting lists per route, using CircularDoublyLinkedList

    public FlightScheduleManager(AirportManager airportManager, RouteManager routeManager) {
        this.scheduledFlights = new CircularDoublyLinkedList();
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.waitingLists = new HashMap<>();
    }

    /**
     * Gets the list of all scheduled flights.
     * @return The CircularDoublyLinkedList containing all scheduled flights.
     */
    public CircularDoublyLinkedList getScheduledFlights() {
        return scheduledFlights;
    }

    /**
     * Creates a new flight and adds it to the list of scheduled flights.
     * Includes validation for existing flight number, valid airports, and existing routes.
     *
     * @param number        Unique flight number.
     * @param originCode    Code of the origin airport.
     * @param destinationCode Code of the destination airport.
     * @param departureTime Scheduled departure time.
     * @param initialOccupancy Initial number of passengers (usually 0).
     * @param capacity      Maximum passenger capacity of the flight.
     * @return The created Flight object.
     * @throws ListException If the flight number already exists, airports are invalid, or no route exists.
     * @throws IllegalArgumentException If flight details are invalid.
     */
    public Flight createFlight(String number, String originCode, String destinationCode,
                               LocalDateTime departureTime, int initialOccupancy, int capacity) throws ListException, IllegalArgumentException {

        // 1. Validate if the flight number already exists
        if (findFlight(number) != null) {
            throw new ListException("El número de vuelo " + number + " ya está en uso.");
        }

        // 2. Validate that origin and destination airports exist
        if (airportManager.findAirport(originCode) == null) {
            throw new ListException("El aeropuerto de origen '" + originCode + "' no es válido.");
        }
        if (airportManager.findAirport(destinationCode) == null) {
            throw new ListException("El aeropuerto de destino '" + destinationCode + "' no es válido.");
        }

        // 3. Verify if a route exists in the graph
        // This implicitly calls routeManager.calculateShortestRoute using the DirectedSinglyLinkedListGraph
        if (routeManager.calculateShortestRoute(originCode, destinationCode) == Integer.MAX_VALUE) {
            throw new ListException("No existe una ruta en el grafo entre " + originCode + " y " + destinationCode + ". No se puede crear el vuelo.");
        }

        // Create the new flight using the external Flight class
        // The Flight constructor handles initial occupancy setting to 0 and throws IllegalArgumentException for invalid capacity
        Flight newFlight = new Flight(number, originCode, destinationCode, departureTime, capacity);

        // If initialOccupancy was meant to be set from an external source, keep this:
        // Otherwise, if Flight constructor always sets to 0, this line is redundant for initial creation.
        // For now, let's keep it assuming initialOccupancy could be non-zero in some call contexts.
        if (initialOccupancy > 0) { // Only set if there's actual initial occupancy
            // Note: Flight.setOccupancy is private. You'd need to modify Flight to make it public,
            // or adjust how initial passengers are added.
            // For now, I'll assume you have a way to add initial passengers or that initialOccupancy is always 0
            // and the Flight constructor handles it.
            // If you truly need to set an initial occupancy, the Flight class needs a public setter for occupancy.
            // Or, you would loop and call newFlight.addPassenger() 'initialOccupancy' times.
            // For safety, I'll comment out the direct setter call and rely on addPassenger if needed, or constructor default.
        }

        scheduledFlights.add(newFlight);
        return newFlight;
    }

    /**
     * Processes a ticket purchase for a passenger on a specific flight.
     * If the flight is available (scheduled and has capacity), the passenger is added directly.
     * Otherwise, the passenger is added to a waiting list for that route.
     *
     * @param passenger The passenger attempting to purchase a ticket.
     * @param flight    The flight for which the ticket is being purchased.
     * @throws ListException If there's an issue adding the passenger to the flight (e.g., already on flight).
     * @throws StackException If there's an underlying issue with the passenger's TDA (unlikely with SinglyLinkedList, will check when PassengerManager is provided).
     * @throws IllegalArgumentException If passenger or flight is null.
     */
    public void processTicketPurchase(Passenger passenger, Flight flight) throws ListException, StackException, IllegalArgumentException {
        if (flight == null) {
            throw new IllegalArgumentException("No se puede procesar la compra de billetes. El objeto vuelo es nulo.");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("No se puede procesar la compra de billetes. El objeto pasajero es nulo.");
        }

        // Check if passenger is already on this flight (important to avoid duplicates)
        // Uses getPassengers() from the external Flight class, which returns CircularDoublyLinkedList
        if (flight.getPassengers().contains(passenger)) {
            // System.out.println("INFO: Pasajero " + passenger.getId() + " ya tiene un billete para el vuelo " + flight.getFlightNumber() + ".");
            return; // Passenger already booked
        }

        // Attempt to book directly if flight is scheduled and has space
        // Using getStatus() and isFull() from the external Flight class
        if (flight.getStatus() == Flight.FlightStatus.SCHEDULED && !flight.isFull()) {
            flight.addPassenger(passenger); // This method in Flight increments occupancy
            System.out.println("Pasajero " + passenger.getName() + " (" + passenger.getId() + ") reservado en el vuelo " + flight.getFlightNumber() + " de " + flight.getOriginAirportCode() + " a " + flight.getDestinationAirportCode() + ".");

            // If the passenger was on a waiting list for this route, remove them
            // Using getOriginAirportCode() and getDestinationAirportCode() from external Flight class
            String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
            if (waitingLists.containsKey(routeKey)) {
                CircularDoublyLinkedList routeWaitingList = waitingLists.get(routeKey);
                try {
                    if (routeWaitingList.contains(passenger)) {
                        routeWaitingList.remove(passenger);
                        // System.out.println("DEBUG: Pasajero " + passenger.getId() + " removido de la lista de espera para la ruta " + routeKey + ".");
                        if (routeWaitingList.isEmpty()) {
                            waitingLists.remove(routeKey); // Remove waiting list if empty
                        }
                    }
                } catch (Exception e) {
                    System.err.println("ERROR: Error al intentar remover pasajero de la lista de espera para la ruta " + routeKey + ": " + e.getMessage());
                }
            }

        } else {
            // If flight is not available for direct booking, add to waiting list
            System.out.println("ADVERTENCIA: El vuelo " + flight.getFlightNumber() + " no está disponible para reserva (Estado: " + flight.getStatus() + ", Ocupación: " + flight.getOccupancy() + "/" + flight.getCapacity() + "). Pasajero " + passenger.getId() + " añadido a lista de espera.");

            // Using getOriginAirportCode() and getDestinationAirportCode() from external Flight class
            String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
            waitingLists.putIfAbsent(routeKey, new CircularDoublyLinkedList()); // Create list if not exists
            CircularDoublyLinkedList routeWaitingList = waitingLists.get(routeKey);

            if (!routeWaitingList.contains(passenger)) { // Prevent duplicates in waiting list
                routeWaitingList.add(passenger);
            } else {
                System.out.println("INFO: Pasajero " + passenger.getId() + " ya está en la lista de espera para la ruta " + routeKey + ".");
            }
        }
    }

    /**
     * Finds a flight by its flight number.
     *
     * @param flightNumber The number of the flight to search for.
     * @return The Flight object if found, null otherwise.
     * @throws ListException If there's an issue iterating through the list.
     */
    public Flight findFlight(String flightNumber) throws ListException {
        if (scheduledFlights.isEmpty()) {
            return null;
        }
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight f = (Flight) scheduledFlights.get(i);
            // Use getFlightNumber() as per external Flight class
            if (f.getFlightNumber().equals(flightNumber)) {
                return f;
            }
        }
        return null;
    }

    /**
     * Displays active flights (SCHEDULED, IN_PROGRESS, ASSIGNED) and completed flights.
     * @throws ListException If there's an issue accessing list elements.
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
            // Include ASSIGNED in active flights
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
    }

    /**
     * Simulates the basic lifecycle of a flight (takeoff and landing).
     * Note: Detailed time-based simulation is handled by FlightSimulator.
     *
     * @param flightNumber The number of the flight to simulate.
     * @throws ListException If the flight is not found or already completed/in progress.
     */
    public void simulateFlight(String flightNumber) throws ListException {
        Flight flight = findFlight(flightNumber);
        if (flight == null) {
            throw new ListException("Vuelo " + flightNumber + " no encontrado para simular.");
        }

        if (flight.getStatus() == Flight.FlightStatus.COMPLETED || flight.getStatus() == Flight.FlightStatus.IN_PROGRESS) {
            System.out.println("Vuelo " + flightNumber + " ya está en progreso o completado. No se puede simular de nuevo.");
            return;
        }

        System.out.println("\nIniciando simulación básica para vuelo " + flightNumber + "...");
        flight.setStatus(Flight.FlightStatus.IN_PROGRESS);
        System.out.println("Vuelo " + flightNumber + ": Despegando de " + flight.getOriginAirportCode() + "."); // Use correct getter

        flight.setStatus(Flight.FlightStatus.COMPLETED);
        System.out.println("Vuelo " + flightNumber + ": Aterrizando en " + flight.getDestinationAirportCode() + "."); // Use correct getter
        System.out.println("Simulación básica para vuelo " + flightNumber + " completada.");
    }
    // The inner Flight class defined here has been removed.
    // Ensure that ucr.proyectoalgoritmos.Domain.flight.Flight is the correct and updated class.
}