package ucr.proyectoalgoritmos.Domain.flight; // Adjust package

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // For passengers assigned to this flight
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // For storing actual passengers
import ucr.proyectoalgoritmos.Domain.list.ListException; // Import ListException

public class FlightSchedule implements Comparable<FlightSchedule> { // Implement Comparable for sorting/searching
    private String flightNumber; // e.g., AA123
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private int capacity;
    private int occupancy; // Current number of passengers assigned/boarded
    private FlightStatus status; // Enum for Scheduled, Active, Completed, Cancelled
    private SinglyLinkedList assignedPassengers; // List of Passenger objects assigned to this flight

    public FlightSchedule(String flightNumber, String originAirportCode, String destinationCode,
                          LocalDateTime departureTime, int capacity) {
        this.flightNumber = flightNumber;
        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationCode; // Corrected parameter name
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.occupancy = 0; // Starts empty
        this.status = FlightStatus.SCHEDULED; // Initial status
        this.assignedPassengers = new SinglyLinkedList(); // Initialize list of passengers
        System.out.println("FS DEBUG: Flight " + flightNumber + " created with capacity " + capacity);
    }

    // --- Getters ---
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getCapacity() { return capacity; }
    public int getOccupancy() { return occupancy; }
    public FlightStatus getStatus() { return status; }
    public int getAvailableSeats() { return capacity - occupancy; }
    public SinglyLinkedList getAssignedPassengers() { return assignedPassengers; } // Returns the actual list

    // --- Setters (for updatable attributes or status changes) ---
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public void setStatus(FlightStatus status) {
        System.out.println("FS DEBUG: Flight " + flightNumber + " status changed from " + this.status + " to " + status);
        this.status = status;
    }

    /**
     * Assigns a passenger to this flight if there are available seats.
     * @param passenger The Passenger object to assign.
     * @return true if the passenger was successfully assigned, false otherwise (e.g., flight is full).
     */
    public boolean assignPassenger(Passenger passenger) { // Assign one passenger
        if (passenger == null) {
            System.err.println("[FLIGHT " + flightNumber + "] Cannot assign a null passenger.");
            return false;
        }
        if (occupancy < capacity) {
            this.assignedPassengers.add(passenger); // Add passenger object to list
            occupancy++;
            System.out.println("[FLIGHT " + flightNumber + "] Passenger " + passenger.getId() + " assigned. Occupancy: " + occupancy + "/" + capacity);
            return true;
        } else {
            System.out.println("[FLIGHT " + flightNumber + "] Cannot assign passenger " + passenger.getId() + ". Flight is full (Occupancy: " + occupancy + "/" + capacity + ").");
            return false;
        }
    }

    /**
     * Clears all assigned passengers and resets occupancy to zero.
     * This method is typically called upon flight completion.
     */
    public void emptyPassengers() {
        System.out.println("FS DEBUG: Emptying passengers for flight " + flightNumber + ". Before: " + occupancy + " passengers.");
        this.occupancy = 0;
        this.assignedPassengers.clear(); // Clear the list of assigned passengers
        System.out.println("[FLIGHT " + flightNumber + "] Passengers emptied. After: " + occupancy + " passengers.");
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "Flight [Num: " + flightNumber +
                ", From: " + originAirportCode +
                ", To: " + destinationAirportCode +
                ", Depart: " + departureTime.format(formatter) +
                ", Cap: " + capacity +
                ", Occ: " + occupancy +
                ", Status: " + status +
                "]";
    }

    /**
     * Compares this FlightSchedule object with another based on their flight numbers.
     * @param other The other FlightSchedule object to compare to.
     * @return A negative integer, zero, or a positive integer as this flight number
     * is less than, equal to, or greater than the specified flight number.
     */
    @Override
    public int compareTo(FlightSchedule other) {
        // Null checks for robustness
        if (other == null) return 1; // This object is greater than null
        if (this.flightNumber == null && other.flightNumber == null) return 0;
        if (this.flightNumber == null) return -1; // Null flight number is "less" than a non-null one
        if (other.flightNumber == null) return 1;

        return this.flightNumber.compareTo(other.flightNumber);
    }

    // Enum for flight status
    public enum FlightStatus {
        SCHEDULED,
        ACTIVE,    // In transit
        COMPLETED,
        CANCELLED
    }
}