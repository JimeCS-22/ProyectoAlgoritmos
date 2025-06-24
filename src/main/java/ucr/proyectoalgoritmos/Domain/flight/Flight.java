package ucr.proyectoalgoritmos.Domain.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {
    private String flightNumber;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime; // Scheduled departure time
    private LocalDateTime actualDepartureTime; // Actual time flight took off
    private LocalDateTime actualArrivalTime; // Actual time flight landed
    private int capacity; // This capacity can change if an airplane is assigned
    private CircularDoublyLinkedList passengers; // List of passengers on the flight
    private int occupancy; // Current number of passengers on the flight
    private FlightStatus status; // Current status of the flight
    private Airplane airplane; // The airplane assigned to this flight (can be null initially)
    private int estimatedDurationMinutes; // Estimated flight duration in minutes
    private String gate;

    // Constantes para las puertas disponibles
    private static final String[] AVAILABLE_GATES = {
            "A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3", "D1", "D2", "D3"
    };
    private static final Random RANDOM = new Random();

    public enum FlightStatus {

        SCHEDULED,

        IN_PROGRESS,

        COMPLETED,

        CANCELLED,

        ASSIGNED
    }

    public Flight() {
        this.passengers = new CircularDoublyLinkedList();
        this.status = FlightStatus.SCHEDULED;
        this.occupancy = 0;
        this.estimatedDurationMinutes = 0;
        this.gate = assignRandomGate(); // Asigna puerta al crear el vuelo
    }

    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity) throws ListException {
        // Validations in the constructor to ensure data integrity.
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight number cannot be null or empty.");
        }
        if (originAirportCode == null || originAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin airport code cannot be null or empty.");
        }
        if (destinationAirportCode == null || destinationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination airport code cannot be null or empty.");
        }
        if (departureTime == null) {
            throw new IllegalArgumentException("Departure time cannot be null.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }

        this.flightNumber = flightNumber.trim();
        this.originAirportCode = originAirportCode.trim();
        this.destinationAirportCode = destinationAirportCode.trim();
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.passengers = new CircularDoublyLinkedList();
        this.occupancy = 0;
        this.status = FlightStatus.SCHEDULED;
        this.airplane = null;
        this.estimatedDurationMinutes = 0;
        this.actualDepartureTime = null;
        this.actualArrivalTime = null;
        this.gate = null;
    }

    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity, int occupancy, FlightStatus status) {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Flight number cannot be null or empty.");
        }
        if (originAirportCode == null || originAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Origin airport code cannot be null or empty.");
        }
        if (destinationAirportCode == null || destinationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination airport code cannot be null or empty.");
        }
        if (departureTime == null) {
            throw new IllegalArgumentException("Departure time cannot be null.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be a positive number.");
        }
        if (occupancy < 0 || occupancy > capacity) {
            throw new IllegalArgumentException("Occupancy must be non-negative and not exceed capacity.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Flight status cannot be null.");
        }

        this.flightNumber = flightNumber.trim();
        this.originAirportCode = originAirportCode.trim();
        this.destinationAirportCode = destinationAirportCode.trim();
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.occupancy = occupancy;
        this.status = status;
        this.passengers = new CircularDoublyLinkedList();
        this.airplane = null;
        this.estimatedDurationMinutes = 0;
        this.actualDepartureTime = null;
        this.actualArrivalTime = null;
        this.gate = null;
    }

    // --- Getters ---
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getScheduledDepartureTime() { return departureTime; } // Consistent with departureTime
    public LocalDateTime getActualDepartureTime() { return actualDepartureTime; }
    public LocalDateTime getActualArrivalTime() { return actualArrivalTime; }
    public int getCapacity() { return capacity; }
    public CircularDoublyLinkedList getPassengers() { return passengers; }
    public int getOccupancy() { return occupancy; }
    public FlightStatus getStatus() { return status; }
    public Airplane getAirplane() { return airplane; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }

    // --- Setters ---
       public void setDepartureTime(LocalDateTime departureTime) {
        if (departureTime == null) {
            throw new IllegalArgumentException("Departure time cannot be null.");
        }
        this.departureTime = departureTime;
    }

    public void setActualDepartureTime(LocalDateTime actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    public void setActualArrivalTime(LocalDateTime actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    public void setStatus(FlightStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Flight status cannot be null.");
        }
        this.status = status;
    }

    public void setAirplane(Airplane airplane) {
        if (airplane == null) {

            this.airplane = null;

            return;
        }


        if (this.occupancy > airplane.getCapacity()) {
            throw new IllegalArgumentException(
                    "Cannot assign airplane '" + airplane.getId() +
                            "' to flight '" + this.flightNumber +
                            "' because current occupancy (" + this.occupancy +
                            ") exceeds the airplane's capacity (" + airplane.getCapacity() + ")."
            );
        }
        this.airplane = airplane;
        this.capacity = airplane.getCapacity();
    }

    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        if (estimatedDurationMinutes < 0) {
            throw new IllegalArgumentException("Estimated duration cannot be negative.");
        }
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public void setOccupancy(int occupancy) {
        if (occupancy < 0) {
            throw new IllegalArgumentException("Occupancy cannot be negative.");
        }
        if (occupancy > this.capacity) {
            throw new IllegalArgumentException("Occupancy (" + occupancy + ") cannot exceed flight capacity (" + this.capacity + ").");
        }
        this.occupancy = occupancy;
    }

    public void addPassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null.");
        }
        if (occupancy >= capacity) {
            throw new ListException("Flight " + this.flightNumber + " is full. Could not add passenger " + passenger.getId() + ".");
        }

        if (this.passengers.contains(passenger)) {
            throw new ListException("Passenger " + passenger.getId() + " is already on flight " + this.flightNumber + ".");
        }

        this.passengers.add(passenger);
        this.occupancy++;
    }


    public void removePassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null.");
        }
        if (this.passengers.isEmpty()) {
            throw new ListException("Flight " + this.flightNumber + " has no passengers to remove.");
        }

        if (this.passengers.contains(passenger)) {
            this.passengers.remove(passenger);
            this.occupancy--;
        } else {
            throw new ListException("Passenger " + passenger.getId() + " is not on flight " + this.flightNumber + ".");
        }
    }

    public void clearPassengers() throws ListException {
        this.passengers.clear();
        this.occupancy = 0;
    }

    public boolean isFull() {
        return occupancy >= capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber);
    }


    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }


    @Override
    public String toString() {
        return "Flight [Num: " + flightNumber + ", From: " + originAirportCode + ", To: " + destinationAirportCode +
                ", Gate: " + (gate != null ? gate : "Not assigned") +
                ", Scheduled: " + (departureTime != null ? departureTime.withNano(0) : "N/A") +
                (actualDepartureTime != null ? ", Actual Depart: " + actualDepartureTime.withNano(0) : "") +
                (actualArrivalTime != null ? ", Actual Arrive: " + actualArrivalTime.withNano(0) : "") +
                ", Cap: " + capacity + ", Occupancy: " + occupancy +
                ", Status: " + status + (airplane != null ? ", Plane: " + airplane.getId() : "") +
                ", Est. Duration: " + estimatedDurationMinutes + " min]";
    }

    public String getPassengersDisplay() {
        return this.occupancy + "/" + this.capacity;
    }



    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setOriginAirportCode(String originAirportCode) {
        this.originAirportCode = originAirportCode;
    }

    public void setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = destinationAirportCode;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setPassengers(CircularDoublyLinkedList passengers) {
        this.passengers = passengers;
        if (passengers != null) {
            this.occupancy = passengers.size();
        } else {
            this.occupancy = 0;
        }
    }

    // --- Métodos para puertas de abordaje ---

    /**
     * Obtiene la puerta de abordaje asignada al vuelo.
     * Si no hay una puerta asignada, asigna una aleatoria.
     */
    public String getGate() {
        if (gate == null || gate.isEmpty() || gate.equals("N/A")) {
            this.gate = assignRandomGate(); // Reasigna si no es válida
        }
        return gate;
    }

    /**
     * Asigna una puerta específica al vuelo.
     */
    public void setGate(String gate) {
        this.gate = (gate == null || gate.trim().isEmpty()) ? assignRandomGate() : gate.trim();
    }

    /**
     * Asigna una puerta de abordaje aleatoria al vuelo.
     */
    private String assignRandomGate() {
        return AVAILABLE_GATES[RANDOM.nextInt(AVAILABLE_GATES.length)];
    }

    /**
     * Versión alternativa que asigna una puerta consistentemente basada en el número de vuelo
     */
    private void assignConsistentGate() {
        int hash = Math.abs(this.flightNumber.hashCode());
        this.gate = AVAILABLE_GATES[hash % AVAILABLE_GATES.length];
    }
}