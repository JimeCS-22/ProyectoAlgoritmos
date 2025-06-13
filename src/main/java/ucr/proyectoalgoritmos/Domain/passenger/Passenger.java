package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;

import java.util.Objects; // For Objects.hash and Objects.equals

public class Passenger implements Comparable<Passenger> {
    private String id;
    private String name;
    private String nationality;
    private SinglyLinkedList flightHistory; // As per your design

    public Passenger(String id) { // Constructor for search
        this.id = id;
        this.flightHistory = new SinglyLinkedList();
    }

    public Passenger(String id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new SinglyLinkedList(); // Initialize here
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String StringgetNationality() { return nationality; }
    public SinglyLinkedList getFlightHistory() { return flightHistory; }

    // Method to add flight to history
    public void addFlightToHistory(Flight flight) throws ucr.proyectoalgoritmos.Domain.list.ListException {
        if (flightHistory == null) { // Defensive check
            flightHistory = new SinglyLinkedList();
        }
        flightHistory.add(flight);
    }

    @Override
    public int compareTo(Passenger other) {
        // Important: Compare by the ID, as that's your unique key
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return Objects.equals(id, passenger.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}