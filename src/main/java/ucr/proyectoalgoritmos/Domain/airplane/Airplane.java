package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import java.util.Objects;


public class Airplane {
    private String id;
    private int capacity;
    private String currentLocationAirportCode;
    private AirplaneLocationType locationType;
    private AirplaneStatus status;
    private LinkedStack flightHistory;

    public void setLocationType(AirplaneLocationType airplaneLocationType) {
        if (airplaneLocationType == null) {
            throw new IllegalArgumentException("El tipo de ubicación del avión no puede ser nulo.");
        }
        this.locationType = airplaneLocationType;
    }



    public enum AirplaneStatus {

        IDLE,

        IN_FLIGHT,

        MAINTENANCE,

        ASSIGNED,

        RETIRED
    }

    public enum AirplaneLocationType {

        AIRPORT,

        IN_FLIGHT,

        UNKNOWN
    }

    public Airplane(String id, int capacity, String initialLocationAirportCode) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del avión no puede ser nulo o vacío.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad del avión debe ser un número positivo.");
        }
        // La ubicación inicial SIEMPRE debe ser un aeropuerto válido.
        if (initialLocationAirportCode == null || initialLocationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto de ubicación inicial no puede ser nulo o vacío.");
        }

        this.id = id.trim(); // Asegurarse de limpiar espacios
        this.capacity = capacity;
        this.currentLocationAirportCode = initialLocationAirportCode.trim(); // Establecer ubicación inicial
        this.locationType = AirplaneLocationType.AIRPORT; // Por defecto, el avión inicia en un aeropuerto
        this.status = AirplaneStatus.IDLE; // Estado por defecto
        this.flightHistory = new LinkedStack();
    }


    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getCurrentLocationAirportCode() {
        return currentLocationAirportCode;
    }


    public AirplaneLocationType getLocationType() { // NUEVO GETTER
        return locationType;
    }

    public AirplaneStatus getStatus() {
        return status;
    }

    public LinkedStack getFlightHistory() {
        return flightHistory;
    }


    public void setCurrentLocationAirportCode(String currentLocationAirportCode) {
        if (currentLocationAirportCode == null || currentLocationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto actual no puede ser nulo o vacío.");
        }
        this.currentLocationAirportCode = currentLocationAirportCode.trim();
        this.locationType = AirplaneLocationType.AIRPORT; // Al establecer un aeropuerto, el tipo de ubicación es AIRPORT
    }

    public void setLocationInFlight() { // NUEVO MÉTODO
        this.locationType = AirplaneLocationType.IN_FLIGHT;
        // No se cambia currentLocationAirportCode aquí; este representa el ÚLTIMO aeropuerto conocido.
    }

    public void setStatus(AirplaneStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del avión no puede ser nulo.");
        }
        this.status = status;
    }

    public void addFlightToHistory(Flight flight) throws StackException {
        if (flight == null) {
            throw new IllegalArgumentException("No se puede añadir un vuelo nulo al historial.");
        }
        this.flightHistory.push(flight);
    }

    @Override
    public String toString() {
        String locationDisplay;
        if (locationType == AirplaneLocationType.AIRPORT) {
            locationDisplay = "en aeropuerto: " + currentLocationAirportCode;
        } else if (locationType == AirplaneLocationType.IN_FLIGHT) {
            locationDisplay = "en vuelo (último aeropuerto: " + currentLocationAirportCode + ")";
        } else {
            locationDisplay = "ubicación desconocida";
        }
        return "Avión [ID: " + id + ", Capacidad: " + capacity + ", Estado: " + status + ", " + locationDisplay + "]";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airplane airplane = (Airplane) o;
        return Objects.equals(id, airplane.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}