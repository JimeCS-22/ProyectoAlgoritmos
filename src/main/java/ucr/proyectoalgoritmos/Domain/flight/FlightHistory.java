package ucr.proyectoalgoritmos.Domain.flight; // Adjust package as needed

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FlightHistory { // This is the "history flight" object
    private String originAirportCode;
    private String destinationAirportCode;
    private int passengersCount;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String airplaneID;

    public FlightHistory(String originAirportCode, String destinationAirportCode, int passengersCount,
                         LocalDateTime departureTime, LocalDateTime arrivalTime, String airplaneID) {
        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationAirportCode;
        this.passengersCount = passengersCount;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.airplaneID = airplaneID;
    }

    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public int getPassengersCount() { return passengersCount; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public String getAirplaneID() { return airplaneID; }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "HistoryFlight [Plane: " + airplaneID +
                ", From: " + originAirportCode +
                ", To: " + destinationAirportCode +
                ", Paxs: " + passengersCount +
                ", Depart: " + departureTime.format(formatter) +
                ", Arrive: " + arrivalTime.format(formatter) + "]";
    }
}