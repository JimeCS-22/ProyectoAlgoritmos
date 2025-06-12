package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

public class Airplane {
    private String id;
    private int capacity;
    private String currentLocationAirportCode;
    private AirplaneStatus status;
    private SinglyLinkedList flightHistory; // Historial de vuelos para este avión

    public enum AirplaneStatus {
        IDLE, IN_FLIGHT, MAINTENANCE, RETIRED
    }

    public Airplane(String id, int capacity, String currentLocationAirportCode) {
        this.id = id;
        this.capacity = capacity;
        this.currentLocationAirportCode = currentLocationAirportCode;
        this.status = AirplaneStatus.IDLE; // Por defecto
        this.flightHistory = new SinglyLinkedList();
    }

    // Getters
    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public String getCurrentLocationAirportCode() { return currentLocationAirportCode; }
    public AirplaneStatus getStatus() { return status; }
    public SinglyLinkedList getFlightHistory() { return flightHistory; }

    // Setters
    public void setCurrentLocationAirportCode(String currentLocationAirportCode) {
        this.currentLocationAirportCode = currentLocationAirportCode;
    }
    public void setStatus(AirplaneStatus status) {
        this.status = status;
    }

    public void addFlightToHistory(Flight flight) throws ListException {
        if (flight != null) {
            this.flightHistory.add(flight);
        }
    }

    public void printFlightHistory() {
        System.out.println("Historial del Avión " + id + " (Capacidad: " + capacity + ", Ubicación actual: " + currentLocationAirportCode + ", Estado: " + status + "):");
        try {
            if (flightHistory != null && !flightHistory.isEmpty()) {
                for (int i = 0; i < flightHistory.size(); i++) {
                    Flight f = (Flight) flightHistory.get(i);
                    System.out.println("  - Vuelo " + f.getFlightNumber() + ": " + f.getOriginAirportCode() + " a " + f.getDestinationAirportCode() + " (Estado: " + f.getStatus() + ")");
                }
            } else {
                System.out.println("  (No tiene vuelos registrados)");
            }
        } catch (ListException e) {
            System.err.println("ERROR: No se pudo mostrar el historial de vuelos del avión " + id + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Avión [ID: " + id + ", Capacidad: " + capacity + ", Ubicación: " + currentLocationAirportCode + ", Estado: " + status + "]";
    }
}