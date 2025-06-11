// In Flight.java
package ucr.proyectoalgoritmos.Domain.flight;

import ucr.proyectoalgoritmos.Domain.airplane.Airplane; // Assuming you have an Airplane class
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // Your list implementation
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // Your Passenger class

import java.time.LocalDateTime;
import java.util.Objects; // For equals and hashCode

public class Flight {
    private String flightNumber;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private int capacity;
    private SinglyLinkedList passengers; // List of passengers on this flight
    private int occupancy; // Current number of passengers
    private FlightStatus status;
    private Airplane airplane; // The assigned airplane
    private int estimatedDurationMinutes; // Duration of the flight in minutes

    public enum FlightStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity) throws ListException {
        this.flightNumber = flightNumber;
        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationAirportCode;
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.passengers = new SinglyLinkedList(); // Initialize passenger list
        this.occupancy = 0;
        this.status = FlightStatus.SCHEDULED; // Initial status
    }

    // --- Getters ---
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getCapacity() { return capacity; }
    public SinglyLinkedList getPassengers() { return passengers; }
    public int getOccupancy() { return occupancy; }
    public FlightStatus getStatus() { return status; }
    public Airplane getAirplane() { return airplane; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }

    // --- Setters ---
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public void setStatus(FlightStatus status) { this.status = status; }
    public void setAirplane(Airplane airplane) { this.airplane = airplane; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; } // Potentially useful for internal management

    // --- Passenger Management ---
    public void addPassenger(Passenger passenger) throws ListException {
        if (occupancy < capacity) {
            this.passengers.add(passenger); // Add to the flight's internal list
            this.occupancy++; // Increment occupancy
            //System.out.println("DEBUG: Pasajero " + passenger.getId() + " añadido al vuelo " + this.flightNumber + ". Ocupación: " + this.occupancy + "/" + this.capacity);
        } else {
            // This should ideally be caught before calling addPassenger if capacity is a hard limit
            System.err.println("ADVERTENCIA: El vuelo " + this.flightNumber + " está lleno. No se pudo añadir al pasajero " + passenger.getId() + ".");
            throw new ListException("Vuelo lleno"); // Or handle as an exception
        }
    }

    public void removePassenger(Passenger passenger) throws ListException {
        if (this.passengers.contains(passenger)) { // Assuming 'contains' is implemented and 'equals' for Passenger works
            this.passengers.remove(passenger); // Remove from the flight's internal list
            this.occupancy--; // Decrement occupancy
            //System.out.println("DEBUG: Pasajero " + passenger.getId() + " removido del vuelo " + this.flightNumber + ". Ocupación: " + this.occupancy + "/" + this.capacity);
        } else {
            System.err.println("ADVERTENCIA: El pasajero " + passenger.getId() + " no está en el vuelo " + this.flightNumber + ".");
        }
    }

    public void clearPassengers() throws ListException {
        this.passengers.clear();
        this.occupancy = 0;
        //System.out.println("DEBUG: Pasajeros del vuelo " + this.flightNumber + " vaciados. Ocupación: " + this.occupancy);
    }

    // --- Important for list operations (e.g., removing from scheduledFlights) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber); // Flight number is the unique identifier
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }

    @Override
    public String toString() {
        return "Vuelo [Num: " + flightNumber + ", De: " + originAirportCode + ", A: " + destinationAirportCode +
                ", Salida: " + departureTime + ", Cap: " + capacity + ", Ocup: " + occupancy +
                ", Estado: " + status + "]";
    }
}