package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue; // Assuming your LinkedQueue is in this package
import ucr.proyectoalgoritmos.Domain.queue.QueueException; // Assuming your QueueException is here
import ucr.proyectoalgoritmos.Domain.list.ListException; // Keep if addFlightToHistory can still throw it, or remove

import java.util.Objects;

public class Passenger implements Comparable<Passenger> {
    private String id;
    private String name;
    private String nationality;
    private LinkedQueue flightHistory; // Historial de vuelos para este pasajero, ahora como LinkedQueue

    public Passenger(String id) {
        this.id = id;
        this.flightHistory = new LinkedQueue();
    }

    public Passenger(String id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new LinkedQueue(); // Initialize here
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; } // Corrected method name
    public LinkedQueue getFlightHistory() { return flightHistory; } // Return LinkedQueue



    /**
     * Adds a flight to the passenger's history using the queue's enqueue operation.
     * @param flight The flight to add to the history.
     * @throws QueueException If there's an issue with the queue.
     */
    public void addFlightToHistory(Flight flight) throws QueueException { // Changed ListException to QueueException
        if (flight != null) {
            this.flightHistory.enQueue(flight); // Use enqueue for LinkedQueue
        }
    }

    @Override
    public int compareTo(Passenger other) {
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

    @Override
    public String toString() {
        return "Pasajero [ID: " + id + ", Nombre: " + (name != null ? name : "N/A") +
                ", Nacionalidad: " + (nationality != null ? nationality : "N/A") + "]";
    }
}