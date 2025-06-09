package ucr.proyectoalgoritmos.Domain.passanger; // Adjust package

import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.list.ListException; // For list errors in flight history
import ucr.proyectoalgoritmos.Domain.flight.Flight; // For adding to history

public class PassengerManager {
    private AVL passengerTree; // Stores Passenger objects

    public PassengerManager() {
        this.passengerTree = new AVL(); // Initialize your AVL tree
    }

    // a. Register new passenger
    public void registerPassenger(String id, String name, String nationality) {
        Passenger newPassenger = new Passenger(id, name, nationality);
        try {
            // AVL insert operation. Assumes AVL handles duplicate Passenger objects
            // by comparing their 'id' (via Passenger's compareTo method)
            passengerTree.insert(newPassenger);
            System.out.println("[INFO] Passenger registered: " + newPassenger.getName() + " (ID: " + newPassenger.getId() + ")");
        } catch (Exception e) { // Catch potential exceptions from AVL (e.g., duplicate ID)
            System.err.println("[ERROR] Registering passenger " + id + ": " + e.getMessage());
        }
    }

    // b. Search passenger by ID
    public Passenger searchPassenger(String id) {
        // AVL search method needs an object for comparison. Create a dummy Passenger.
        try {
            // Ensure your Passenger class correctly implements equals() and compareTo()
            // based on the 'id' field, and that your AVL.search() method
            // uses these comparison methods.
            return (Passenger) passengerTree.search(new Passenger(id, "", ""));
        } catch (Exception e) {
            // System.err.println("Error searching passenger " + id + ": " + e.getMessage()); // Avoid noise if not found is normal
            return null; // Not found or error
        }
    }

    // c. Show flight history
    public void showFlightHistory(String passengerId) {
        Passenger passenger = searchPassenger(passengerId);
        if (passenger == null) {
            System.out.println("[INFO] Passenger with ID " + passengerId + " not found.");
            return;
        }
        System.out.println("\n--- Flight History for " + passenger.getName() + " (ID: " + passenger.getId() + ") ---");
        try {
            if (passenger.getFlightHistory().isEmpty()) {
                System.out.println("  No flight history recorded.");
            } else {
                for (int i = 0; i < passenger.getFlightHistory().size(); i++) {
                    System.out.println("  " + passenger.getFlightHistory().get(i)); // Assumes Flight has a good toString()
                }
            }
        } catch (ListException e) {
            System.err.println("[ERROR] Accessing flight history for " + passengerId + ": " + e.getMessage());
        }
        System.out.println("----------------------------------------------");
    }

    // Add a flight to a passenger's history (called after flight completion)
    public void addFlightToPassengerHistory(String passengerId, Flight completedFlight) {
        Passenger passenger = searchPassenger(passengerId);
        if (passenger != null) {
            passenger.addFlightToHistory(completedFlight);
        } else {
            System.err.println("[ERROR] Passenger " + passengerId + " not found to update flight history.");
        }
    }

    /**
     * Returns the total number of passengers currently registered in the system.
     * This count is maintained by the underlying AVL tree.
     * @return The number of passengers.
     */
    public int getPassengerCount() {
        return passengerTree.size(); // Assumes your AVL class has a size() method
    }
}