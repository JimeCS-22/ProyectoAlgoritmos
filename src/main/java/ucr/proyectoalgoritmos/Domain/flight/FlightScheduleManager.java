package ucr.proyectoalgoritmos.Domain.flight;

import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // Corregido: passenger a passanger
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.route.RouteManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FlightScheduleManager {
    private DoublyLinkedList scheduledFlights; // List of all scheduled/active flights
    private AirportManager airportManager;
    private RouteManager routeManager;
    private Map<String, DoublyLinkedList> waitingLists; // Map<"ORIGIN-DESTINATION", List<Passenger>>

    public FlightScheduleManager(AirportManager airportManager, RouteManager routeManager) {
        this.scheduledFlights = new DoublyLinkedList();
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.waitingLists = new HashMap<>();
        //System.out.println("DEBUG FSM: FlightScheduleManager inicializado.");
    }

    // This method needs to return the created Flight object
    public Flight createFlight(String flightNumber, String originAirportCode, String destinationAirportCode,
                               LocalDateTime departureTime, int currentOccupancy, int capacity) throws ListException {
        // First, check for duplicate flight numbers to avoid ListException
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight existingFlight = (Flight) scheduledFlights.get(i);
            if (existingFlight.getFlightNumber().equals(flightNumber)) {
                throw new ListException("El número de vuelo " + flightNumber + " ya está en uso.");
            }
        }

        Flight newFlight = new Flight(flightNumber, originAirportCode, destinationAirportCode, departureTime, capacity);
        newFlight.setOccupancy(currentOccupancy); // Set initial occupancy (should be 0 when created, then add passengers)
        scheduledFlights.add(newFlight);
        //System.out.println("[INFO] Vuelo creado: " + newFlight.toString() + ". Total vuelos programados: " + scheduledFlights.size());
        return newFlight; // Return the created flight
    }

    // --- NEW / MODIFIED METHOD: processTicketPurchase ---
    // This method now takes the Flight object directly
    public void processTicketPurchase(Passenger passenger, Flight flight) throws ListException, StackException {
        if (flight == null) {
            System.err.println("ERROR TICKET: No se puede procesar la compra de billetes. El objeto vuelo es nulo.");
            return;
        }

        // Check if the flight is SCHEDULED and has capacity
        if (flight.getStatus() == Flight.FlightStatus.SCHEDULED && flight.getOccupancy() < flight.getCapacity()) {
            flight.addPassenger(passenger); // Add passenger to the flight's internal list
            // NOTE: passenger.addFlightToHistory(flight) will be handled by PassengerManager.processTicketPurchase
            // (or similar method) to avoid circular dependencies if PassengerManager calls this FSM method.
            System.out.println("TICKET: Pasajero " + passenger.getName() + " (" + passenger.getId() + ") reservado en el vuelo " + flight.getFlightNumber() + " de " + flight.getOriginAirportCode() + " a " + flight.getDestinationAirportCode() + ". Ocupación actual: " + flight.getOccupancy() + "/" + flight.getCapacity() + ".");

            // If you have a waiting list, you'd check here if this passenger was waiting
            // for this specific route and remove them from the waiting list.
            String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
            if (waitingLists.containsKey(routeKey)) {
                DoublyLinkedList routeWaitingList = waitingLists.get(routeKey);
                try {
                    if (routeWaitingList.contains(passenger)) { // Assuming contains and remove work with Passenger objects
                        routeWaitingList.remove(passenger);
                       // System.out.println("DEBUG TICKET: Pasajero " + passenger.getId() + " removido de la lista de espera para la ruta " + routeKey + ".");
                        if (routeWaitingList.isEmpty()) {
                            waitingLists.remove(routeKey); // Remove empty waiting list
                        }
                    }
                } catch (Exception e) {
                    System.err.println("ERROR TICKET: Error al intentar remover pasajero de la lista de espera: " + e.getMessage());
                }
            }

        } else {
            System.out.println("TICKET: El vuelo " + flight.getFlightNumber() + " no está disponible para reserva (Estado: " + flight.getStatus() + ", Ocupación: " + flight.getOccupancy() + "/" + flight.getCapacity() + "). El pasajero " + passenger.getId() + " añadido a lista de espera.");
            // Add to waiting list for this specific route
            String routeKey = flight.getOriginAirportCode() + "-" + flight.getDestinationAirportCode();
            DoublyLinkedList routeWaitingList = waitingLists.get(routeKey);
            if (routeWaitingList == null) {
                routeWaitingList = new DoublyLinkedList();
                waitingLists.put(routeKey, routeWaitingList);
            }
            routeWaitingList.add(passenger);
        }
    }

    // Helper method to find a flight by its number
    public Flight getFlight(String flightNumber) throws ListException {
        for (int i = 0; i < scheduledFlights.size(); i++) {
            Flight f = (Flight) scheduledFlights.get(i);
            if (f.getFlightNumber().equals(flightNumber)) {
                return f;
            }
        }
        return null;
    }

    // --- COMPLETED METHOD: getScheduledFlights ---
    public DoublyLinkedList getScheduledFlights() {
        return this.scheduledFlights;
    }

}