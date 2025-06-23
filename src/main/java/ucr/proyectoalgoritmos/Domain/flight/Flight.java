package ucr.proyectoalgoritmos.Domain.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;

import java.time.LocalDateTime;
import java.util.Objects;

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

    /**
     * Enumeration that defines the possible **operational states** a flight can be in.
     */
    public enum FlightStatus {
        /**
         * The flight is planned but has not yet departed.
         */
        SCHEDULED,
        /**
         * The flight is in progress, currently flying.
         */
        IN_PROGRESS,
        /**
         * The flight has arrived at its destination.
         */
        COMPLETED,
        /**
         * The flight has been canceled.
         */
        CANCELLED,
        /**
         * The flight has been assigned to an aircraft and is ready for operations.
         */
        ASSIGNED
    }

    /**
     * Default constructor for deserialization purposes (e.g., by Jackson).
     * Initializes lists and default values to prevent NullPointerExceptions.
     */
    public Flight() {
        this.passengers = new CircularDoublyLinkedList();
        this.status = FlightStatus.SCHEDULED;
        this.occupancy = 0;
        this.estimatedDurationMinutes = 0;
        // Other fields like flightNumber, departureTime, etc., will be set by the deserializer.
    }

    /**
     * Constructor to create a new flight instance.
     * A newly created flight is set to **SCHEDULED** status by default.
     *
     * @param flightNumber The unique identification number of the flight.
     * @param originAirportCode The IATA code of the origin airport.
     * @param destinationAirportCode The IATA code of the destination airport.
     * @param departureTime The scheduled departure date and time of the flight.
     * @param capacity The initial passenger capacity of the flight. This can be updated
     * if an airplane with a different capacity is assigned.
     * @throws IllegalArgumentException If any of the required parameters are null, empty, or invalid.
     * @throws ListException If an error occurs while initializing the passenger list.
     */
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
        this.passengers = new CircularDoublyLinkedList(); // Initialize the passenger list
        this.occupancy = 0; // Initial occupancy is 0
        this.status = FlightStatus.SCHEDULED; // Initial status
        this.airplane = null; // No airplane assigned initially
        this.estimatedDurationMinutes = 0; // Default duration, can be set later
        this.actualDepartureTime = null; // Not set until flight takes off
        this.actualArrivalTime = null; // Not set until flight lands
    }

    /**
     * Constructor for loading flight data, potentially with existing occupancy and status.
     *
     * @param flightNumber The unique identification number of the flight.
     * @param originAirportCode The IATA code of the origin airport.
     * @param destinationAirportCode The IATA code of the destination airport.
     * @param departureTime The scheduled departure date and time of the flight.
     * @param capacity The total passenger capacity of the flight.
     * @param occupancy The current number of passengers on the flight.
     * @param status The current status of the flight.
     * @throws IllegalArgumentException If any parameter is null, empty, or invalid.
     */
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
        this.passengers = new CircularDoublyLinkedList(); // Initialize the passenger list
        this.airplane = null; // Airplane not assigned initially
        this.estimatedDurationMinutes = 0; // Default duration, can be set later
        this.actualDepartureTime = null; // Not set until flight takes off
        this.actualArrivalTime = null; // Not set until flight lands
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
    /**
     * Sets a new scheduled departure time for the flight.
     * @param departureTime The new scheduled departure date and time.
     */
    public void setDepartureTime(LocalDateTime departureTime) {
        if (departureTime == null) {
            throw new IllegalArgumentException("Departure time cannot be null.");
        }
        this.departureTime = departureTime;
    }

    /**
     * Sets the actual time the flight took off.
     * @param actualDepartureTime The actual departure time.
     */
    public void setActualDepartureTime(LocalDateTime actualDepartureTime) {
        this.actualDepartureTime = actualDepartureTime;
    }

    /**
     * Sets the actual time the flight landed.
     * @param actualArrivalTime The actual arrival time.
     */
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }

    /**
     * Sets a new status for the flight.
     * @param status The new flight status.
     */
    public void setStatus(FlightStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Flight status cannot be null.");
        }
        this.status = status;
    }

    /**
     * Assigns an airplane to this flight. When an airplane is assigned, the flight's capacity
     * is updated to match the airplane's capacity.
     * It validates that the current flight occupancy does not exceed the new airplane's capacity.
     * @param airplane The {@link Airplane} object to assign to the flight. Can be null to unassign.
     * @throws IllegalArgumentException If current occupancy exceeds the airplane's capacity
     * or if the provided airplane is null and the flight already has passengers.
     */
    public void setAirplane(Airplane airplane) {
        if (airplane == null) {
            // Decide how to handle capacity when no airplane is assigned.
            // For now, it maintains the last set capacity.
            this.airplane = null;
            // Optionally: if passengers exist, consider throwing an error or clearing them.
            // if (this.occupancy > 0) {
            //     throw new IllegalArgumentException("Cannot unassign airplane from flight " + this.flightNumber + " while passengers are boarded.");
            // }
            return;
        }

        // Key validation: Current occupancy must not exceed the capacity of the assigned airplane.
        if (this.occupancy > airplane.getCapacity()) {
            throw new IllegalArgumentException(
                    "Cannot assign airplane '" + airplane.getId() +
                            "' to flight '" + this.flightNumber +
                            "' because current occupancy (" + this.occupancy +
                            ") exceeds the airplane's capacity (" + airplane.getCapacity() + ")."
            );
        }
        this.airplane = airplane;
        this.capacity = airplane.getCapacity(); // Update the flight's capacity to that of the assigned airplane
    }

    /**
     * Sets the estimated duration of the flight in minutes.
     * @param estimatedDurationMinutes The duration in minutes.
     */
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        if (estimatedDurationMinutes < 0) {
            throw new IllegalArgumentException("Estimated duration cannot be negative.");
        }
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    /**
     * Sets the current occupancy of the flight. This method should be used cautiously;
     * `addPassenger` and `removePassenger` are preferred for managing occupancy directly.
     * @param occupancy The new occupancy value.
     */
    public void setOccupancy(int occupancy) {
        if (occupancy < 0) {
            throw new IllegalArgumentException("Occupancy cannot be negative.");
        }
        if (occupancy > this.capacity) {
            throw new IllegalArgumentException("Occupancy (" + occupancy + ") cannot exceed flight capacity (" + this.capacity + ").");
        }
        this.occupancy = occupancy;
    }

    /**
     * Adds a passenger to the flight.
     * @param passenger The {@link Passenger} object to add.
     * @throws IllegalArgumentException If the passenger is null.
     * @throws ListException If the flight is full or the passenger is already on the flight.
     */
    public void addPassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null.");
        }
        if (occupancy >= capacity) {
            throw new ListException("Flight " + this.flightNumber + " is full. Could not add passenger " + passenger.getId() + ".");
        }
        // Assumes Passenger.equals() is correctly implemented (by passenger ID)
        if (this.passengers.contains(passenger)) {
            throw new ListException("Passenger " + passenger.getId() + " is already on flight " + this.flightNumber + ".");
        }

        this.passengers.add(passenger);
        this.occupancy++;
    }

    /**
     * Removes a passenger from the flight.
     * @param passenger The {@link Passenger} object to remove.
     * @throws IllegalArgumentException If the passenger is null.
     * @throws ListException If the flight has no passengers or the passenger is not on the flight.
     */
    public void removePassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("Passenger cannot be null.");
        }
        if (this.passengers.isEmpty()) {
            throw new ListException("Flight " + this.flightNumber + " has no passengers to remove.");
        }
        // Assumes Passenger.equals() is correctly implemented (by passenger ID)
        if (this.passengers.contains(passenger)) {
            this.passengers.remove(passenger);
            this.occupancy--;
        } else {
            throw new ListException("Passenger " + passenger.getId() + " is not on flight " + this.flightNumber + ".");
        }
    }

    /**
     * Empties the flight's passenger list and resets occupancy to zero.
     * @throws ListException If an error occurs while clearing the passenger list.
     */
    public void clearPassengers() throws ListException {
        this.passengers.clear();
        this.occupancy = 0;
    }

    /**
     * Checks if the flight has reached its maximum capacity.
     * @return {@code true} if occupancy is equal to or greater than capacity, {@code false} otherwise.
     */
    public boolean isFull() {
        return occupancy >= capacity;
    }

    /**
     * Compares this Flight object with another object to determine if they are equal.
     * Two flights are considered equal if they have the same {@code flightNumber}.
     *
     * @param o The object to compare with this Flight.
     * @return {@code true} if the objects are equal (same flight number), {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber);
    }

    /**
     * Returns a hash code value for this Flight object.
     * This method must be consistent with {@code equals()}: if two objects
     * are equal according to {@code equals()}, they must have the same {@code hashCode()} value.
     * It is generated based on the flight's {@code flightNumber}.
     *
     * @return An integer hash code value.
     */
    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }

    /**
     * Provides a string representation of the flight's information.
     * Useful for debugging and console display.
     * @return A formatted string with flight details.
     */
    @Override
    public String toString() {
        return "Flight [Num: " + flightNumber + ", From: " + originAirportCode + ", To: " + destinationAirportCode +
                ", Scheduled: " + (departureTime != null ? departureTime.withNano(0) : "N/A") +
                (actualDepartureTime != null ? ", Actual Depart: " + actualDepartureTime.withNano(0) : "") +
                (actualArrivalTime != null ? ", Actual Arrive: " + actualArrivalTime.withNano(0) : "") +
                ", Cap: " + capacity + ", Occupancy: " + occupancy +
                ", Status: " + status + (airplane != null ? ", Plane: " + airplane.getId() : "") +
                ", Est. Duration: " + estimatedDurationMinutes + " min]";
    }

    /**
     * Returns a string representing the current passenger occupancy in the format "occupancy/capacity".
     * @return A string showing current passengers versus total capacity.
     */
    public String getPassengersDisplay() {
        return this.occupancy + "/" + this.capacity;
    }

    // Setters for direct field assignment, mainly for deserialization or specific logic where direct assignment is needed.
    // Use with caution, as they bypass some validation logic present in the primary constructors or other methods.
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
            this.occupancy = passengers.size(); // Update occupancy when setting the passenger list directly
        } else {
            this.occupancy = 0;
        }
    }
}