package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

public class Passenger {
    private String id;
    private String name;
    private String nationality;
    private SinglyLinkedList flightHistory; // Historial de vuelos para este pasajero

    public Passenger(String id, String name, String nationality) throws ListException {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new SinglyLinkedList();
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public SinglyLinkedList getFlightHistory() { return flightHistory; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public void addFlightToHistory(Flight flight) throws ListException {
        if (flight != null && !flightHistory.contains(flight)) { // Avoid adding duplicates
            flightHistory.add(flight);
        }
    }

    // Important for list operations like 'contains' or 'remove'
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return id.equals(passenger.id); // Assuming ID is unique
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Pasajero [ID: " + id + ", Nombre: " + name + ", Nacionalidad: " + nationality + "]";
    }
}