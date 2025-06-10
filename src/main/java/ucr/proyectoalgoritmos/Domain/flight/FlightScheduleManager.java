package ucr.proyectoalgoritmos.Domain.flight; // Adjust package

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;

import ucr.proyectoalgoritmos.Domain.aeropuetos.Airport;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.passenger.PassengerManager;
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
        this.scheduledFlights = new CircularDoublyLinkedList(); // Initialize the Circular Doubly Linked List
        this.airportManager = airportManager;
        this.routeManager = routeManager;
        this.random = new Random();
        this.passengerWaitingLists = new HashMap<>();
        System.out.println("FSM DEBUG: FlightScheduleManager initialized.");
    }

    // a. Create flight from an origin airport to a destination
    public void createFlight(String flightNumber, String originCode, String destinationCode,
                             LocalDateTime departureTime, int capacity) throws ListException {
        System.out.println("FSM DEBUG: Attempting to create flight " + flightNumber);
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
        System.out.println("[INFO] Flight created: " + newFlight.getFlightNumber() + " from " + originCode + " to " + destinationCode + ". Total scheduled flights: " + scheduledFlights.size());
    }

    // 3. Ticket Purchase - Assign passengers to flight
    public void processTicketPurchase(Passenger passenger, String originCode, String destinationCode) throws ListException {
        if (originCode.equalsIgnoreCase(destinationCode)) {
            System.out.println("[TICKET] " + passenger.getId() + ": Cannot book flight to same airport.");
            return;
        }

        // Find available flight with capacity
        FlightSchedule availableFlight = null;

        // Ensure scheduledFlights is not empty before iterating
        if (scheduledFlights.isEmpty()) {
            System.out.println("FSM DEBUG: No scheduled flights available for ticket purchase.");
            // Add to waiting list directly if no flights exist
            System.out.println("[TICKET] No flights exist at all. Passenger " + passenger.getId() + " added to waiting list for " + originCode + "-" + destinationCode + ".");
            String routeKey = originCode + "-" + destinationCode;
            SinglyLinkedList waitingList = passengerWaitingLists.computeIfAbsent(routeKey, k -> new SinglyLinkedList());
            waitingList.add(passenger);
            return;
        }


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
            System.out.println("FSM DEBUG: No waiting passengers for route " + routeKey);
            return; // No waiting passengers for this route
        }

        // Find a suitable flight that is SCHEDULED and has capacity
        FlightSchedule targetFlight = null;

        // Check if there are any scheduled flights before attempting to find one
        if (scheduledFlights.isEmpty()) {
            System.out.println("FSM DEBUG: No scheduled flights available to assign waiting passengers to.");
            return;
        }

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
                if (waitingPassenger == null) { // Defensive check
                    System.err.println("FSM ERROR: Waiting list returned null passenger for " + routeKey);
                    waitingList.removeFirst(); // Try to clear the null entry if it somehow got there
                    continue;
                }
                if (targetFlight.assignPassenger(waitingPassenger)) {
                    waitingList.removeFirst(); // Remove from waiting list if assigned
                    assignedCount++;
                } else {
                    System.out.println("[WAITLIST] Flight became full unexpectedly during assignment for " + waitingPassenger.getId());
                    break;
                }
            }
            System.out.println("[WAITLIST] " + assignedCount + " passengers assigned from waiting list for " + routeKey + ". Remaining: " + waitingList.size());
        } else {
            System.out.println("[WAITLIST] No suitable flight found for waiting passengers on route " + routeKey);
        }
    }

    // c. Show active and completed flights
    public void listFlights(FlightSchedule.FlightStatus statusFilter) throws ListException {
        System.out.println("FSM DEBUG: listFlights() called. Total scheduled flights: " + scheduledFlights.size());
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
        System.out.println("FSM DEBUG: simulateFlight(" + flightNumber + ") called.");
        FlightSchedule flight = findFlight(flightNumber);
        if (flight == null) {
            throw new ListException("Flight " + flightNumber + " not found for simulation. Scheduled flights count: " + scheduledFlights.size());
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
            System.err.println("[SIM ERROR] No route found from " + flight.getOriginAirportCode() + " to " + flight.getDestinationAirportCode() + ". Flight " + flightNumber + " cancelled.");
            // Consider removing the cancelled flight from scheduledFlights if it won't be retried
            // scheduledFlights.remove(flight); // Only if you want to remove it
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
            // Assuming removeFlightFromBoard expects a FlightSchedule object
            // You might need to adjust Airport class if it's not ready for FlightSchedule
            // originAirport.removeFlightFromBoard(flight);
            System.out.println("FSM INFO: Flight " + flightNumber + " departed from " + originAirport.getName());
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
        // NOTE: Ensure FlightHistory is a separate class or record if it's meant for history.
        // If FlightSchedule also serves as history, then this object might not be needed.
        // Assuming FlightHistory is distinct from FlightSchedule for historical records.
        FlightHistory historyFlight =
                new FlightHistory(
                        flight.getFlightNumber(), // Add flight number to history for better tracking
                        flight.getOriginAirportCode(),
                        flight.getDestinationAirportCode(),
                        flight.getOccupancy(),
                        flight.getDepartureTime(),
                        LocalDateTime.now(), // Arrival time is now
                        airplane.getId());

        // Update each passenger's flight history *before* emptying the flight's assigned passenger list
        SinglyLinkedList passengersOnThisFlight = flight.getAssignedPassengers();
        if (passengersOnThisFlight != null && !passengersOnThisFlight.isEmpty()) {
            for (int i = 0; i < passengersOnThisFlight.size(); i++) {
                try {
                    Passenger p = (Passenger) passengersOnThisFlight.get(i);
                    passengerManager.addFlightToPassengerHistory(p.getId(), historyFlight);
                } catch (ListException e) {
                    System.err.println("FSM ERROR: Could not get passenger from assigned list: " + e.getMessage());
                }
            }
        } else {
            System.out.println("FSM DEBUG: No passengers on flight " + flightNumber + " to update history for.");
        }


        flight.emptyPassengers(); // Clears assignedPassengers list on the current FlightSchedule object
        airplane.land(flight.getDestinationAirportCode(), historyFlight); // Empties airplane's passenger count, adds flight to airplane's history

        // Remove the completed flight from the scheduledFlights list (if you want to track active vs. completed)
        // If you want to keep all flights in `scheduledFlights` and filter by status, don't remove here.
        // If `scheduledFlights` is only for *pending* flights, then remove it.
        // Let's assume you want to remove it from `scheduledFlights` if it's completed and no longer 'scheduled'.
        try {
            boolean removed = scheduledFlights.remove(flight);
            if (removed) {
                System.out.println("FSM INFO: Flight " + flightNumber + " removed from scheduled flights list after completion. Remaining scheduled flights: " + scheduledFlights.size());
            } else {
                System.err.println("FSM ERROR: Flight " + flightNumber + " not found in scheduled flights list for removal after completion.");
            }
        } catch (ListException e) {
            System.err.println("FSM ERROR: Could not remove completed flight " + flightNumber + " from scheduled flights: " + e.getMessage());
        }

        // Assign waiting passengers to potentially new future flights (if you have them)
        // This makes sense if the arrival frees up resources or triggers new flight creation.
        // If it's for *this* route, it should be done after the flight itself is completed.
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

    // Changed return type to FlightSchedule as scheduledFlights holds FlightSchedule objects
    public FlightSchedule getFlightScheduleByIndex(int index) throws ListException {
        if (index < 0 || index >= scheduledFlights.size()) {
            throw new ListException("Flight index out of bounds: " + index + ", List size: " + scheduledFlights.size());
        }
        return (FlightSchedule) scheduledFlights.get(index);
    }

    // New helper to get a random scheduled flight
    public FlightSchedule getRandomScheduledFlight() throws ListException {
        if (scheduledFlights.isEmpty()) {
            System.out.println("FSM DEBUG: No scheduled flights available to pick a random one.");
            return null; // No flights to return
        }
        int randomIndex = random.nextInt(scheduledFlights.size());
        return (FlightSchedule) scheduledFlights.get(randomIndex);
    }
}