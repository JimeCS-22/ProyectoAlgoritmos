package ucr.proyectoalgoritmos.Domain.passanger; // Adjust package

import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // To store flight history
import ucr.proyectoalgoritmos.Domain.flight.Flight; // Using the history Flight object

public class Passenger implements Comparable<Passenger> { // Implement Comparable for AVL Tree
    private String id; // Cédula (ID number)
    private String name;
    private String nationality;
    private SinglyLinkedList flightHistory; // Stores Flight (history) objects

    public Passenger(String id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new SinglyLinkedList(); // Initialize flight history
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public SinglyLinkedList getFlightHistory() { return flightHistory; }

    // --- Setters (if attributes are editable) ---
    public void setName(String name) { this.name = name; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    // Add a flight to the passenger's history
    public void addFlightToHistory(Flight flight) { // Strong type checking for Flight object
        this.flightHistory.add(flight);
    }

    // For comparison in AVL (based on ID)
    @Override
    public int compareTo(Passenger other) {
        return this.id.compareTo(other.id);
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