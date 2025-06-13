package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack; // Now we know the exact LinkedStack
import ucr.proyectoalgoritmos.Domain.stack.StackException; // And the StackException

public class Airplane {
    private String id;
    private int capacity;
    private String currentLocationAirportCode;
    private AirplaneStatus status;
    private LinkedStack flightHistory; // Historial de vuelos para este avión, ahora como LinkedStack

    public enum AirplaneStatus {
        IDLE, IN_FLIGHT, MAINTENANCE, ASSIGNED, RETIRED
    }

    public Airplane(String id, int capacity, String currentLocationAirportCode) {
        this.id = id;
        this.capacity = capacity;
        this.currentLocationAirportCode = currentLocationAirportCode;
        this.status = AirplaneStatus.IDLE; // Por defecto
        this.flightHistory = new LinkedStack(); // Initialize as LinkedStack
    }

    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public String getCurrentLocationAirportCode() { return currentLocationAirportCode; }
    public AirplaneStatus getStatus() { return status; }

    public void setCurrentLocationAirportCode(String currentLocationAirportCode) {
        this.currentLocationAirportCode = currentLocationAirportCode;
    }
    public void setStatus(AirplaneStatus status) {
        this.status = status;
    }

    /**
     * Adds a flight to the airplane's history using the stack's push operation.
     * @param flight The flight to add to the history.
     * @throws StackException If there's an issue with the stack.
     */
    public void addFlightToHistory(Flight flight) throws StackException {
        if (flight != null) {
            this.flightHistory.push(flight); // Use push for LinkedStack
        }
    }

    /**
     * Prints the flight history of the airplane using the LinkedStack's toString method.
     */
    public void printFlightHistory() {
        System.out.println("Historial del Avión " + id + " (Capacidad: " + capacity + ", Ubicación actual: " + currentLocationAirportCode + ", Estado: " + status + "):");
        try {
            if (flightHistory != null && !flightHistory.isEmpty()) {
                // Relying on LinkedStack's well-implemented toString() method
                // It will print the elements from top to bottom (most recent to oldest flight pushed)
                // in the format defined by LinkedStack.toString().
                System.out.println(flightHistory.toString());
            } else {
                System.out.println("  (No tiene vuelos registrados)");
            }
        } catch (Exception e) { // Catch any unexpected exceptions from stack operations
            System.err.println("ERROR inesperado al mostrar historial de vuelos del avión " + id + ": " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Avión [ID: " + id + ", Capacidad: " + capacity + ", Ubicación: " + currentLocationAirportCode + ", Estado: " + status + "]";
    }
}