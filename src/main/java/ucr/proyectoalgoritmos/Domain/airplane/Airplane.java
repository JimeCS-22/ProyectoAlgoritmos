package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack; // Your LinkedStack
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.stack.StackException;

public class Airplane {
    private String id;
    private int capacity;
    private int currentPassengers;
    private String currentLocationAirportCode; // Current airport code
    private LinkedStack flightHistory; // Stack to store completed Flight (history) objects

    public Airplane(String id, int capacity, String initialLocationAirportCode) {
        this.id = id;
        this.capacity = capacity;
        this.currentPassengers = 0;
        this.currentLocationAirportCode = initialLocationAirportCode;
        this.flightHistory = new LinkedStack(); // Use your LinkedStack here
        System.out.println("[INFO] Airplane '" + id + "' (Capacity: " + capacity + ") created at " + initialLocationAirportCode);
    }

    // Getters
    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public int getCurrentPassengers() { return currentPassengers; }
    public String getCurrentLocationAirportCode() { return currentLocationAirportCode; }
    public int getAvailableCapacity() { return capacity - currentPassengers; }

    // Board passengers onto the airplane
    public int boardPassengers(int passengersToBoard) {
        int boardedCount = Math.min(passengersToBoard, getAvailableCapacity());
        this.currentPassengers += boardedCount;
        System.out.println("[BOARDING] Airplane " + id + " boarded " + boardedCount + " passengers. Total on board: " + currentPassengers);
        return boardedCount;
    }

    // Simulate takeoff
    public void takeOff() {
        System.out.println("[TAKEOFF] Airplane " + id + " taking off from " + currentLocationAirportCode + " with " + currentPassengers + " passengers.");
    }

    // Simulate landing, empty passengers, and record flight
    public void land(String destinationAirportCode, Flight completedFlight) throws StackException { // Accepts the history Flight object
        this.currentLocationAirportCode = destinationAirportCode; // Update current location
        this.currentPassengers = 0; // Empty passengers

            this.flightHistory.push(completedFlight); // Record flight in the stack

        System.out.println("[LANDING] Airplane " + id + " landed at " + destinationAirportCode + ". Passengers emptied. Flight recorded.");
    }

    // Method to print flight history
    public void printFlightHistory() throws StackException {
        System.out.println("\n--- Flight History for Airplane " + id + " ---");

            if (flightHistory.isEmpty()) {
                System.out.println("  No flights recorded yet.");
                return;
            }
            LinkedStack tempStack = new LinkedStack(); // Use a temporary stack to print in correct order (FIFO style)
            while (!flightHistory.isEmpty()) {
                tempStack.push(flightHistory.pop());
            }
            while (!tempStack.isEmpty()) {
                Object flightObj = tempStack.pop();
                if (flightObj instanceof Flight) { // Ensure it's a Flight object
                    System.out.println("  " + flightObj);
                }
                flightHistory.push(flightObj); // Push back to original stack
            }

        System.out.println("-------------------------------------");
    }

    @Override
    public String toString() {
        return "Airplane [ID: " + id + ", Location: " + currentLocationAirportCode +
                ", Passengers: " + currentPassengers + "/" + capacity + "]";
    }
}