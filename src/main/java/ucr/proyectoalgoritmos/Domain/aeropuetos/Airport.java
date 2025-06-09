package ucr.proyectoalgoritmos.Domain.aeropuetos; // Adjust package

import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // Your SinglyLinkedList for departures board
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue; // For passenger queue at this airport
import ucr.proyectoalgoritmos.Domain.list.ListException;


public class Airport implements Comparable<Airport> { // Implement Comparable for sorting/searching
    private String code; // e.g., SJO, JFK
    private String name;
    private String country;
    private AirportStatus status; // Enum for Active/Inactive
    private SinglyLinkedList departuresBoard; // A list of upcoming flights from this airport
    private LinkedQueue passengerQueue; // Queue for passengers waiting to board at this airport

    public Airport(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = AirportStatus.ACTIVE; // Default to active
        this.departuresBoard = new SinglyLinkedList(); // Initialize the departures board
        this.passengerQueue = new LinkedQueue(); // Initialize passenger queue
    }

    // --- Getters ---
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public AirportStatus getStatus() { return status; }
    public SinglyLinkedList getDeparturesBoard() { return departuresBoard; }
    public LinkedQueue getPassengerQueue() { return passengerQueue; }


    // --- Setters (for editable attributes) ---
    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }
    public void setStatus(AirportStatus status) { this.status = status; }

    // --- Operations ---
    public void activate() { this.status = AirportStatus.ACTIVE; }
    public void deactivate() { this.status = AirportStatus.INACTIVE; }

    // Add a flight to the departures board (for display)
    public void addFlightToBoard(Object flight) { // Change Object to Flight from FlightManagement when ready
        this.departuresBoard.add(flight);
    }

    public void removeFlightFromBoard(Object flight) throws ListException { // Remove after departure
        this.departuresBoard.remove(flight);
    }

    public void addPassengersToQueue(int count) throws ListException {
        for (int i = 0; i < count; i++) {
           passengerQueue.offer(new Object()); // Add a dummy passenger object
        }
        System.out.println("[QUEUE] " + count + " passengers added to " + name + " queue. Total: " + passengerQueue.size());
    }

    public int boardPassengers(int capacity) throws ListException {
        int boardedCount = 0;
        while (!passengerQueue.isEmpty() && boardedCount < capacity) {
            passengerQueue.poll(); // Remove a passenger from the queue
            boardedCount++;
        }
        return boardedCount;
    }


    @Override
    public String toString() {
        return "Airport{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", status=" + status +
                ", queueSize=" + (passengerQueue != null ? passengerQueue.size() : "N/A") + // Show queue size
                '}';
    }

    // Enum for airport status
    public enum AirportStatus {
        ACTIVE,
        INACTIVE
    }

    // For comparison in DoublyLinkedList or other structures
    @Override
    public int compareTo(Airport other) {
        return this.code.compareTo(other.code);
    }
}