package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.AVL; // Assuming your AVL is in Domain
import ucr.proyectoalgoritmos.Domain.flight.FlightHistory;
import ucr.proyectoalgoritmos.Domain.list.ListException; // Your ListException

public class PassengerManager {
    public AVL passengers; // AVL tree to store Passenger objects

    public PassengerManager() {
        this.passengers = new AVL();
    }

    public void registerPassenger(String id, String name, String nationality) throws ListException {
        Passenger newPassenger = new Passenger(id, name, nationality);
        try {
            passengers.insert(newPassenger);
            System.out.println("[INFO] Passenger registered: " + newPassenger.getName() + " (ID: " + newPassenger.getId() + ")");
        } catch (ListException e) {
            System.err.println("[ERROR] Failed to register passenger " + id + ": " + e.getMessage());
            // Optionally, rethrow if registration must be unique and fail
        }
    }

    public Passenger searchPassenger(String id) throws ListException {
        // Create a "dummy" passenger object with just the ID for searching
        // This relies on Passenger's compareTo and equals methods
        Passenger searchKey = new Passenger(id, "", "");
        try {
            return (Passenger) passengers.search(searchKey);
        } catch (ListException e) {
            System.err.println("[ERROR] Error searching for passenger " + id + ": " + e.getMessage());
            return null;
        }
    }

    // Method to add flight history to a specific passenger
    public void addFlightToPassengerHistory(String passengerId, FlightHistory flightRecord) throws ListException {
        Passenger passenger = searchPassenger(passengerId); // Find the passenger
        if (passenger != null) {
            passenger.addFlightToHistory(flightRecord);
            System.out.println("[HISTORY] Flight record added for passenger " + passengerId + ".");
        } else {
            System.err.println("[ERROR] Cannot add flight history: Passenger " + passengerId + " not found.");
        }
    }

    public void showFlightHistory(String passengerId) {
        try {
            Passenger passenger = searchPassenger(passengerId);
            if (passenger != null) {
                System.out.println("\n--- Flight History for " + passenger.getName() + " (ID: " + passenger.getId() + ") ---");
                System.out.println(passenger.getFlightHistory()); // Uses FlightHistoryList's toString
            } else {
                System.out.println("\n--- Flight History for " + passengerId + " ---");
                System.out.println("  Passenger not found or no flight history recorded.");
            }
        } catch (ListException e) {
            System.err.println("[ERROR] Error showing flight history for " + passengerId + ": " + e.getMessage());
        }
    }

    public int getPassengerCount() {
        return passengers.size();
    }
}