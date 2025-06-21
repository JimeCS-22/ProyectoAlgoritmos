package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import java.util.Objects;

/**
 * Representa un **avión** dentro del sistema de gestión de vuelos.
 * Cada avión tiene un identificador único, una capacidad de pasajeros,
 * una ubicación actual (código de aeropuerto), un **tipo de ubicación** (en tierra o en vuelo),
 * un estado operativo y un historial de vuelos que ha realizado.
 */
public class Airplane {
    private String id;
    private int capacity;
    private String currentLocationAirportCode; // Código IATA del aeropuerto donde se encuentra actualmente (o el último conocido si está en vuelo)
    private AirplaneLocationType locationType; // NUEVO: Para distinguir si está en un aeropuerto o en vuelo
    private AirplaneStatus status;
    private LinkedStack flightHistory;

    /**
     * Enumeración que define los posibles **estados operativos** en los que puede encontrarse un avión.
     */
    public enum AirplaneStatus {
        /**
         * El avión está disponible y no asignado a un vuelo.
         */
        IDLE,
        /**
         * El avión está actualmente en vuelo.
         */
        IN_FLIGHT,
        /**
         * El avión está siendo sometido a mantenimiento.
         */
        MAINTENANCE,
        /**
         * El avión ha sido asignado a un vuelo próximo.
         */
        ASSIGNED,
        /**
         * El avión ha sido retirado de servicio.
         */
        RETIRED
    }

    /**
     * Enumeración que define el **tipo de ubicación** actual de un avión.
     * Permite diferenciar claramente si el avión está en un aeropuerto o en el aire.
     */
    public enum AirplaneLocationType {
        /**
         * El avión se encuentra actualmente en un aeropuerto.
         */
        AIRPORT,
        /**
         * El avión está actualmente en vuelo entre dos aeropuertos.
         */
        IN_FLIGHT,
        /**
         * La ubicación del avión es desconocida o no definida.
         */
        UNKNOWN
    }

    /**
     * Constructor para crear una nueva instancia de un avión.
     * Un avión recién creado se establece por defecto en estado **IDLE**,
     * su tipo de ubicación en **AIRPORT** y su historial de vuelos se inicializa como una pila vacía.
     *
     * @param id El identificador único del avión. No debe ser nulo ni vacío.
     * @param capacity La capacidad máxima de pasajeros del avión. Debe ser un número positivo.
     * @param initialLocationAirportCode El código del aeropuerto donde el avión se encuentra inicialmente.
     * No debe ser nulo ni vacío.
     * @throws IllegalArgumentException Si el ID, la capacidad o el código de aeropuerto son inválidos.
     */
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
        this.flightHistory = new LinkedStack(); // Inicializa la pila del historial de vuelos
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public int getCapacity() {
        return capacity;
    }

    /**
     * Obtiene el código del aeropuerto donde el avión se encuentra actualmente,
     * o el último aeropuerto conocido si el avión está en vuelo.
     * @return El código IATA del aeropuerto o el último conocido.
     */
    public String getCurrentLocationAirportCode() {
        return currentLocationAirportCode;
    }

    /**
     * Obtiene el tipo de ubicación actual del avión (AIRPORT, IN_FLIGHT, UNKNOWN).
     * @return El {@link AirplaneLocationType} actual del avión.
     */
    public AirplaneLocationType getLocationType() { // NUEVO GETTER
        return locationType;
    }

    public AirplaneStatus getStatus() {
        return status;
    }

    public LinkedStack getFlightHistory() {
        return flightHistory;
    }

    // --- Setters ---

    /**
     * Establece la nueva ubicación (código de aeropuerto) actual del avión.
     * Cuando se establece un código de aeropuerto, el {@code locationType} del avión
     * se actualiza automáticamente a {@link AirplaneLocationType#AIRPORT}.
     * @param currentLocationAirportCode El nuevo código del aeropuerto. No puede ser nulo o vacío.
     * @throws IllegalArgumentException Si el código de aeropuerto proporcionado es nulo o vacío.
     */
    public void setCurrentLocationAirportCode(String currentLocationAirportCode) {
        // Los mensajes DEBUG son útiles, pero pueden ser ruidosos en producción.
        // Considera usar un logger para controlarlos mejor.
        // System.out.println("DEBUG (Airplane): Intentando establecer la ubicación actual a: '" + currentLocationAirportCode + "' para avión ID: " + this.id);

        if (currentLocationAirportCode == null || currentLocationAirportCode.trim().isEmpty()) {
            // System.err.println("ERROR DEBUG (Airplane): Valor nulo o vacío recibido para currentLocationAirportCode en avión ID: " + this.id);
            throw new IllegalArgumentException("El código del aeropuerto actual no puede ser nulo o vacío.");
        }
        this.currentLocationAirportCode = currentLocationAirportCode.trim();
        this.locationType = AirplaneLocationType.AIRPORT; // Al establecer un aeropuerto, el tipo de ubicación es AIRPORT
        // System.out.println("DEBUG (Airplane): Ubicación actual establecida a: '" + this.currentLocationAirportCode + "' y locationType a AIRPORT para avión ID: " + this.id);
    }

    /**
     * Establece el tipo de ubicación del avión a {@link AirplaneLocationType#IN_FLIGHT}.
     * Cuando un avión está en vuelo, su {@code currentLocationAirportCode} mantiene
     * el código del último aeropuerto del que despegó, hasta que aterrice en el siguiente.
     */
    public void setLocationInFlight() { // NUEVO MÉTODO
        this.locationType = AirplaneLocationType.IN_FLIGHT;
        // No se cambia currentLocationAirportCode aquí; este representa el ÚLTIMO aeropuerto conocido.
        // System.out.println("DEBUG (Airplane): LocationType establecido a IN_FLIGHT para avión ID: " + this.id);
    }

    /**
     * Establece el nuevo estado operativo del avión.
     * @param status El nuevo estado del avión. No puede ser nulo.
     * @throws IllegalArgumentException Si el estado proporcionado es nulo.
     */
    public void setStatus(AirplaneStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del avión no puede ser nulo.");
        }
        this.status = status;
    }

    /**
     * Añade un vuelo al historial de vuelos del avión. El vuelo se añade a la parte superior de la pila.
     * @param flight El objeto {@link Flight} a añadir al historial.
     * @throws StackException Si ocurre un error al añadir el vuelo a la pila.
     * @throws IllegalArgumentException Si el vuelo proporcionado es nulo.
     */
    public void addFlightToHistory(Flight flight) throws StackException {
        if (flight == null) {
            throw new IllegalArgumentException("No se puede añadir un vuelo nulo al historial.");
        }
        this.flightHistory.push(flight);
    }

    /**
     * Provee una representación en cadena de texto de la información del avión.
     * Útil para depuración y visualización en consola.
     * Muestra la ubicación de manera dinámica según el {@code locationType}.
     * @return Una cadena formateada con el ID, capacidad, ubicación y estado del avión.
     */
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

    /**
     * Compara este objeto Airplane con otro objeto para determinar si son iguales.
     * Dos aviones se consideran iguales si tienen el mismo {@code id}.
     *
     * @param o El objeto a comparar con este Airplane.
     * @return {@code true} si los objetos son iguales (mismo ID), {@code false} en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airplane airplane = (Airplane) o;
        return Objects.equals(id, airplane.id);
    }

    /**
     * Retorna un valor de código hash para este objeto Airplane.
     * Este método debe ser consistente con {@code equals()}: si dos objetos
     * son iguales según {@code equals()}, deben tener el mismo valor de {@code hashCode()}.
     * Se genera basándose en el {@code id} del avión.
     *
     * @return Un valor de código hash entero.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}