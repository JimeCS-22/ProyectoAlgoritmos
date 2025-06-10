package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.flight.FlightHistory;
import ucr.proyectoalgoritmos.Domain.flight.FlightHistoryList;
import ucr.proyectoalgoritmos.Domain.list.ListException; // Assuming this is your common ListException

public class Passenger implements Comparable<Passenger> {
    private String id;
    private String name;
    private String nationality;
    private FlightHistoryList flightHistory; // Changed to a custom FlightHistoryList for a list of histories

    public Passenger(String id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new FlightHistoryList(); // Initialize custom history list
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNationality() {
        return nationality;
    }

    // This method now adds a single FlightHistory object to the passenger's history list
    public void addFlightToHistory(FlightHistory completedFlight) {
        if (completedFlight != null) {
            try {
                this.flightHistory.add(completedFlight);
            } catch (ListException e) {
                System.err.println("[ERROR] Failed to add flight history for passenger " + id + ": " + e.getMessage());
            }
        }
    }

    public FlightHistoryList getFlightHistory() {
        return flightHistory;
    }

    @Override
    public int compareTo(Passenger other) {
        // This is the CRITICAL part for AVL: comparison MUST be based on the unique key (ID)
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        // Equality should also be based on ID for consistency with compareTo
        return id.equals(passenger.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode(); // Consistent with equals
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nationality='" + nationality + '\'' +
                '}';
    }
}