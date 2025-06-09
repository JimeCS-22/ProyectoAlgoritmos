package ucr.proyectoalgoritmos.Domain.flight; // Adjust package

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException; // This is the primary ListException
import ucr.proyectoalgoritmos.Domain.passanger.Passenger;
import ucr.proyectoalgoritmos.Domain.passanger.PassengerManager;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import ucr.proyectoalgoritmos.route.RouteManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit; // For Thread.sleep

public class FlightScheduleManager {
    public CircularDoublyLinkedList scheduledFlights; // Stores Flight objects
    private AirportManager airportManager; // Dependency to check airport validity
    private RouteManager routeManager;   // Dependency to get route distances
    private Random random;

    // A simple waiting list structure: Map<"Origin-Destination", SinglyLinkedList<Passenger>>
    private Map<String, SinglyLinkedList> passengerWaitingLists;

    public FlightScheduleManager(AirportManager airportManager, RouteManager routeManager) {
        this.scheduledFlights = new CircularDoublyLinkedList();
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.random = new Random();
        this.passengerWaitingLists = new HashMap<>();
    }

    // a. Create flight from an origin airport to a destination
    public void createFlight(String flightNumber, String originCode, String destinationCode,
                             LocalDateTime departureTime, int capacity) throws ListException {

        // Business Rule: No flights to the same airport.
        if (originCode.equalsIgnoreCase(destinationCode)) {
            throw new ListException("Cannot create flight from " + originCode + " to " + destinationCode + ": Origin and destination cannot be the same.");
        }

        // Basic validation
        if (airportManager.findAirport(originCode) == null) {
            throw new ListException("Origin airport " + originCode + " does not exist.");
        }
        if (airportManager.findAirport(destinationCode) == null) {
            throw new ListException("Destination airport " + destinationCode + " does not exist.");
        }
        // Check if flight number already exists
        if (findFlight(flightNumber) != null) {
            throw new ListException("Flight number " + flightNumber + " already exists.");
        }

        FlightSchedule newFlight = new FlightSchedule(flightNumber, originCode, destinationCode, departureTime, capacity);
        scheduledFlights.add(newFlight); // Add to the Circular Doubly Linked List
        System.out.println("[INFO] Flight created: " + newFlight.getFlightNumber() + " from " + originCode + " to " + destinationCode);
    }

    // 3. Ticket Purchase - Assign passengers to flight
    public void processTicketPurchase(Passenger passenger, String originCode, String destinationCode) throws ListException {
        if (originCode.equalsIgnoreCase(destinationCode)) {
            System.out.println("[TICKET] " + passenger.getId() + ": Cannot book flight to same airport.");
            return;
        }

        // Find available flight with capacity
        FlightSchedule availableFlight = null;

        for (int i = 0; i < scheduledFlights.size(); i++) {
            FlightSchedule flight = (FlightSchedule) scheduledFlights.get(i);
            if (flight.getOriginAirportCode().equalsIgnoreCase(originCode) &&
                    flight.getDestinationAirportCode().equalsIgnoreCase(destinationCode) &&
                    flight.getStatus() == FlightSchedule.FlightStatus.SCHEDULED &&
                    flight.getAvailableSeats() > 0) {
                availableFlight = flight;
                break; // Found one, take the first available
            }
        }

        if (availableFlight != null) {
            // Assign passenger to the flight
            if (availableFlight.assignPassenger(passenger)) {
                System.out.println("[TICKET] Passenger " + passenger.getId() + " assigned to flight " + availableFlight.getFlightNumber());
            } else {
                System.out.println("[TICKET] Error assigning passenger " + passenger.getId() + " to flight " + availableFlight.getFlightNumber() + " (unexpectedly full).");
            }
        } else {
            // If no capacity, add to waiting list
            System.out.println("[TICKET] No direct flight from " + originCode + " to " + destinationCode + " with available seats. Passenger " + passenger.getId() + " added to waiting list.");
            String routeKey = originCode + "-" + destinationCode;
            // Get the list, or create it if it doesn't exist
            SinglyLinkedList waitingList = passengerWaitingLists.computeIfAbsent(routeKey, k -> new SinglyLinkedList());
            waitingList.add(passenger);
        }
    }

    // Method to check waiting lists and assign passengers to new flights
    public void assignWaitingPassengersToNewFlights(String originCode, String destinationCode) throws ListException {
        String routeKey = originCode + "-" + destinationCode;
        SinglyLinkedList waitingList = passengerWaitingLists.get(routeKey);

        if (waitingList == null || waitingList.isEmpty()) {
            return; // No waiting passengers for this route
        }

        // Find a suitable flight that is SCHEDULED and has capacity
        FlightSchedule targetFlight = null;

        for (int i = 0; i < scheduledFlights.size(); i++) {
            FlightSchedule flight = (FlightSchedule) scheduledFlights.get(i);
            if (flight.getOriginAirportCode().equalsIgnoreCase(originCode) &&
                    flight.getDestinationAirportCode().equalsIgnoreCase(destinationCode) &&
                    flight.getStatus() == FlightSchedule.FlightStatus.SCHEDULED &&
                    flight.getAvailableSeats() > 0) {
                targetFlight = flight;
                break;
            }
        }

        if (targetFlight != null) {
            System.out.println("[WAITLIST] Attempting to assign waiting passengers for " + routeKey + " to flight " + targetFlight.getFlightNumber());
            int assignedCount = 0;
            while (!waitingList.isEmpty() && targetFlight.getAvailableSeats() > 0) {
                Passenger waitingPassenger = (Passenger) waitingList.getFirst(); // Get first in queue
                if (targetFlight.assignPassenger(waitingPassenger)) {
                    waitingList.removeFirst(); // Remove from waiting list if assigned
                    assignedCount++;
                } else {
                    System.out.println("[WAITLIST] Flight became full unexpectedly during assignment for " + waitingPassenger.getId());
                    break;
                }
            }
            System.out.println("[WAITLIST] " + assignedCount + " passengers assigned from waiting list for " + routeKey + ". Remaining: " + waitingList.size());
        }
    }

    // c. Show active and completed flights
    public void listFlights(FlightSchedule.FlightStatus statusFilter) throws ListException {
        if (scheduledFlights.isEmpty()) {
            System.out.println("No flights to list.");
            return;
        }
        System.out.println("\n--- " + (statusFilter != null ? statusFilter.name() : "All") + " Flights ---");

        for (int i = 0; i < scheduledFlights.size(); i++) {
            FlightSchedule flight = (FlightSchedule) scheduledFlights.get(i);
            if (statusFilter == null || flight.getStatus() == statusFilter) {
                System.out.println(flight);
            }
        }

        System.out.println("--------------------------------");
    }

    public void listAllFlights() throws ListException {
        listFlights(null); // Show all flights
    }

    // d. Simulate flight (with route and passenger boarding)
    // This method now also handles updating passenger history
    public void simulateFlight(String flightNumber, AirportManager airportManager, PassengerManager passengerManager, Airplane airplane) throws ListException, StackException {
        FlightSchedule flight = findFlight(flightNumber);
        if (flight == null) {
            throw new ListException("Flight " + flightNumber + " not found for simulation.");
        }

        if (flight.getStatus() != FlightSchedule.FlightStatus.SCHEDULED) {
            System.out.println("[SIM] Flight " + flightNumber + " is not in SCHEDULED status. Cannot simulate.");
            return;
        }

        // --- 4. Flight Simulation: Execute Dijkstra to find the shortest route ---
        int routeDuration = routeManager.calculateShortestRoute(
                flight.getOriginAirportCode(),
                flight.getDestinationAirportCode());

        if (routeDuration == Integer.MAX_VALUE) {
            flight.setStatus(FlightSchedule.FlightStatus.CANCELLED); // Mark as cancelled if unreachable
            throw new ListException("No route found from " + flight.getOriginAirportCode() + " to " + flight.getDestinationAirportCode() + ". Flight " + flightNumber + " cancelled.");
        }

        System.out.println("\n--- Simulating Flight " + flightNumber + " ---");
        System.out.println("From: " + flight.getOriginAirportCode() + " (" + airportManager.findAirport(flight.getOriginAirportCode()).getName() + ")");
        System.out.println("To: " + flight.getDestinationAirportCode() + " (" + airportManager.findAirport(flight.getDestinationAirportCode()).getName() + ")");
        System.out.println("Occupancy: " + flight.getOccupancy() + "/" + flight.getCapacity());
        System.out.println("Estimated flight duration: " + routeDuration + " minutes");

        // Update airport departures board (optional, but good for realism)
        Airport originAirport = airportManager.findAirport(flight.getOriginAirportCode());
        if (originAirport != null) {
            originAirport.removeFlightFromBoard(flight); // Assuming this method exists
        }

        airplane.boardPassengers(flight.getOccupancy()); // Transfer assigned passengers from Flight object to Airplane object

        flight.setStatus(FlightSchedule.FlightStatus.ACTIVE);
        airplane.takeOff(); // Airplane takes off

        // --- Console Animation Logic ---
        System.out.println("\n-----------------------------------------------------");
        System.out.println("FLIGHT IN PROGRESS: " + flight.getFlightNumber());
        System.out.print(flight.getOriginAirportCode() + " ");

        int animationSteps = 30;
        long totalAnimationTimeMs = routeDuration * 10L; // Simulate 10ms per minute of flight
        long sleepTimePerStep = totalAnimationTimeMs / animationSteps;
        if (sleepTimePerStep == 0) sleepTimePerStep = 1;

        for (int i = 0; i < animationSteps; i++) {
            try {
                System.out.print(">");
                if (i == animationSteps / 2) {
                    System.out.print("✈️");
                }
                TimeUnit.MILLISECONDS.sleep(sleepTimePerStep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[SIM] Flight animation interrupted for " + flightNumber + ".");
                flight.setStatus(FlightSchedule.FlightStatus.CANCELLED);
                System.out.println("\n-----------------------------------------------------");
                return;
            }
        }
        System.out.println(" " + flight.getDestinationAirportCode());
        System.out.println("-----------------------------------------------------\n");
        // --- END Console Animation Logic ---

        // --- 4. Flight Simulation: Upon arrival, empty the airplane's stack and restart the process. ---
        flight.setStatus(FlightSchedule.FlightStatus.COMPLETED);
        System.out.println("[SIM] Flight " + flightNumber + " has landed at " + flight.getDestinationAirportCode() + "!");

        // Create the history Flight object for the airplane and passengers
        FlightHistory historyFlight =
                new FlightHistory(
                        flight.getOriginAirportCode(), flight.getDestinationAirportCode(),
                        flight.getOccupancy(), flight.getDepartureTime(), LocalDateTime.now(), // Arrival time is now
                        airplane.getId());

        // Update each passenger's flight history *before* emptying the flight's assigned passenger list
        SinglyLinkedList passengersOnThisFlight = flight.getAssignedPassengers();
        for (int i = 0; i < passengersOnThisFlight.size(); i++) {
            Passenger p = (Passenger) passengersOnThisFlight.get(i);
            passengerManager.addFlightToPassengerHistory(p.getId(), historyFlight);
        }

        flight.emptyPassengers(); // Clears assignedPassengers list on the current Flight object
        airplane.land(flight.getDestinationAirportCode(), historyFlight); // Empties airplane's passenger count, adds flight to airplane's history

        // Assign waiting passengers to potentially new future flights (if you have them)
        assignWaitingPassengersToNewFlights(flight.getOriginAirportCode(), flight.getDestinationAirportCode());
    }

    // Helper method to find a flight by number
    public FlightSchedule findFlight(String flightNumber) throws ListException {
        if (scheduledFlights.isEmpty()) {
            return null;
        }

        for (int i = 0; i < scheduledFlights.size(); i++) {
            FlightSchedule flight = (FlightSchedule) scheduledFlights.get(i);
            if (flight.getFlightNumber().equalsIgnoreCase(flightNumber)) {
                return flight;
            }
        }

        return null; // Not found
    }

    public FlightHistory getFlightByIndex(int index) throws ListException {
        if (index < 0 || index >= scheduledFlights.size()) {
            throw new ListException("Flight index out of bounds: " + index);
        }
        // This method should return FlightSchedule, not FlightHistory, if scheduledFlights holds FlightSchedule objects
        return (FlightHistory) scheduledFlights.get(index); // Casting directly may cause ClassCastException
    }
}