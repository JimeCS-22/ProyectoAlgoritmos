package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList; // Assuming this is your general DLL
import ucr.proyectoalgoritmos.Domain.flight.FlightHistory;
import ucr.proyectoalgoritmos.Domain.list.ListException; // Assuming this is your common ListException

public class Airplane {
    private String id;
    private int capacity;
    private String currentLocationAirportCode;
    private int passengersOnBoard;
    private CircularDoublyLinkedList flightHistory; // Stores FlightHistory objects
    private AirplaneStatus status; // --- NEW: Status Field ---

    public Airplane(String id, int capacity, String currentLocationAirportCode) {
        this.id = id;
        this.capacity = capacity;
        this.currentLocationAirportCode = currentLocationAirportCode;
        this.passengersOnBoard = 0;
        this.flightHistory = new CircularDoublyLinkedList();
        this.status = AirplaneStatus.IDLE; // --- NEW: Initialize as IDLE ---
    }

    // --- NEW: Getters/Setters for Status ---
    public AirplaneStatus getStatus() {
        return status;
    }

    public void setStatus(AirplaneStatus status) {
        this.status = status;
    }

    public boolean isIdle() {
        return this.status == AirplaneStatus.IDLE;
    }

    // --- Existing Getters ---
    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public String getCurrentLocationAirportCode() { return currentLocationAirportCode; }
    public int getPassengersOnBoard() { return passengersOnBoard; }

    // --- Existing Methods ---
    public void boardPassengers(int count) {
        this.passengersOnBoard += count;
        // Add logic to ensure passengersOnBoard doesn't exceed capacity
        if (this.passengersOnBoard > this.capacity) {
            this.passengersOnBoard = this.capacity; // Cap it
        }
        System.out.println("[PLANE " + id + "] " + count + " passengers boarded. On board: " + passengersOnBoard);
    }

    public void takeOff() {
        this.status = AirplaneStatus.IN_FLIGHT; // --- NEW: Set status on take off ---
        System.out.println("[PLANE " + id + "] Taking off from " + currentLocationAirportCode + ".");
        this.currentLocationAirportCode = null; // No longer at an airport while in flight
    }

    public void land(String destinationAirportCode, FlightHistory completedFlightHistory) {
        this.currentLocationAirportCode = destinationAirportCode;
        this.passengersOnBoard = 0; // All passengers disembark
        this.status = AirplaneStatus.IDLE; // --- NEW: Set status back to IDLE ---
        System.out.println("[PLANE " + id + "] Landed at " + destinationAirportCode + ". Passengers disembarked.");
        this.flightHistory.add(completedFlightHistory); // Add to history
    }

    public void printFlightHistory() {
        System.out.println("\n--- Flight History for Airplane " + id + " ---");
        try {
            if (flightHistory.isEmpty()) {
                System.out.println("  No flights recorded yet.");
            } else {
                for (int i = 0; i < flightHistory.size(); i++) {
                    System.out.println("  " + flightHistory.get(i));
                }
            }
        } catch (ListException e) {
            System.err.println("[ERROR] Accessing airplane flight history for " + id + ": " + e.getMessage());
        }
    }

    // --- NEW: Enum for Airplane Status ---
    public enum AirplaneStatus {
        IDLE,        // Ready for a new flight
        IN_FLIGHT,   // Currently flying
        MAINTENANCE  // Out of service
    }
}