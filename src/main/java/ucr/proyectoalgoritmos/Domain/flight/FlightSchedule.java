package ucr.proyectoalgoritmos.Domain.flight; // Adjust package

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // For passengers assigned to this flight
import ucr.proyectoalgoritmos.Domain.passanger.Passenger; // For storing actual passengers

public class FlightSchedule implements Comparable<FlightSchedule> { // Implement Comparable for sorting/searching
    private String flightNumber; // e.g., AA123
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private int capacity;
    private int occupancy; // Current number of passengers assigned/boarded
    private FlightStatus status; // Enum for Scheduled, Active, Completed, Cancelled
    private SinglyLinkedList assignedPassengers; // List of Passenger objects assigned to this flight

    public FlightSchedule(String flightNumber, String originAirportCode, String destinationAirportCode,
                          LocalDateTime departureTime, int capacity) {
        this.flightNumber = flightNumber;
        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationAirportCode;
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.occupancy = 0; // Starts empty
        this.status = FlightStatus.SCHEDULED; // Initial status
        this.assignedPassengers = new SinglyLinkedList(); // Initialize list of passengers
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
    public SinglyLinkedList getAssignedPassengers() { return assignedPassengers; }

    // --- Setters (for updatable attributes or status changes) ---
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public void setStatus(FlightStatus status) { this.status = status; }

    // b. Assign passengers to the flight
    public boolean assignPassenger(Passenger passenger) { // Assign one passenger
        if (occupancy < capacity) {
            this.assignedPassengers.add(passenger); // Add passenger object to list
            occupancy++;
            System.out.println("[FLIGHT " + flightNumber + "] Passenger " + passenger.getId() + " assigned. Occupancy: " + occupancy);
            return true;
        } else {
            System.out.println("[FLIGHT " + flightNumber + "] Cannot assign passenger " + passenger.getId() + ". Flight is full.");
            return false;
        }
    }

    // Empty passengers upon flight completion
    public void emptyPassengers() {
        this.occupancy = 0;
        try {
            this.assignedPassengers.clear(); // Clear the list of assigned passengers
        } catch (Exception e) { /* should not happen for clear */ }
        System.out.println("[FLIGHT " + flightNumber + "] Passengers emptied.");
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

    // For comparison in CircularDoublyLinkedList or other structures (e.g., by flight number)
    @Override
    public int compareTo(FlightSchedule other) {
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