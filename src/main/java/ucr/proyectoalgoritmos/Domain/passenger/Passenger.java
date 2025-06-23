package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import java.util.Objects;

public class Passenger implements Comparable<Passenger> {
    private String id;
    private String name;
    private String nationality;
    private LinkedQueue flightHistory;

    public Passenger(String id) {
        this.id = id;
        this.flightHistory = new LinkedQueue();
    }

    public Passenger(String id, String name, String nationality) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.flightHistory = new LinkedQueue();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getNationality() { return nationality; }
    public LinkedQueue getFlightHistory() { return flightHistory; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name;}
    public void setNationality(String nationality){ this.nationality = nationality; }
    public void setFlightHistory(LinkedQueue flightHistory) { this.flightHistory = flightHistory; }

    public void addFlightToHistory(Flight flight) throws QueueException {
        if (flight != null) {
            this.flightHistory.enQueue(flight);
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