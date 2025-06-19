package ucr.proyectoalgoritmos.Domain.airplane;

import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.stack.LinkedStack;
import ucr.proyectoalgoritmos.Domain.stack.StackException;
import java.util.Objects; // Importar java.util.Objects para los métodos equals y hashCode

/**
 * Representa un **avión** dentro del sistema de gestión de vuelos.
 * Cada avión tiene un identificador único, una capacidad de pasajeros,
 * una ubicación actual (código de aeropuerto), un estado operativo
 * y un historial de vuelos que ha realizado.
 */
public class Airplane {
    private String id; // Identificador único del avión (ej. "N123AA")
    private int capacity; // Capacidad máxima de pasajeros
    private String currentLocationAirportCode; // Código IATA del aeropuerto donde se encuentra actualmente
    private AirplaneStatus status; // Estado operativo actual del avión
    private LinkedStack flightHistory; // Historial de vuelos realizados por este avión

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
     * Constructor para crear una nueva instancia de un avión.
     * Un avión recién creado se establece por defecto en estado **IDLE**
     * y su historial de vuelos se inicializa como una pila vacía.
     *
     * @param id El identificador único del avión. No debe ser nulo ni vacío.
     * @param capacity La capacidad máxima de pasajeros del avión. Debe ser un número positivo.
     * @param currentLocationAirportCode El código del aeropuerto donde el avión se encuentra inicialmente.
     * No debe ser nulo ni vacío.
     * @throws IllegalArgumentException Si el ID, la capacidad o el código de aeropuerto son inválidos.
     */
    public Airplane(String id, int capacity, String currentLocationAirportCode) {
        // Validaciones básicas para asegurar la integridad de los datos
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del avión no puede ser nulo o vacío.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad del avión debe ser un número positivo.");
        }
        // This check is good for the constructor
        if (currentLocationAirportCode == null || currentLocationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto actual no puede ser nulo o vacío.");
        }

        this.id = id;
        this.capacity = capacity;
        this.currentLocationAirportCode = currentLocationAirportCode;
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

    public String getCurrentLocationAirportCode() {
        return currentLocationAirportCode;
    }

    public AirplaneStatus getStatus() {
        return status;
    }

    public LinkedStack getFlightHistory() {
        return flightHistory;
    }

    // --- Setters ---
    // Nota: 'id' y 'capacity' no tienen setters porque suelen ser inmutables después de la creación.
    // Si necesitas cambiarlos, puedes añadir los setters correspondientes.

    /**
     * Establece la nueva ubicación (código de aeropuerto) actual del avión.
     * @param currentLocationAirportCode El nuevo código del aeropuerto.
     */
    public void setCurrentLocationAirportCode(String currentLocationAirportCode) {
        // --- DEBUG START ---
        System.out.println("DEBUG (Airplane): Intentando establecer la ubicación actual a: '" + currentLocationAirportCode + "' para avión ID: " + this.id);
        // --- DEBUG END ---

        if (currentLocationAirportCode == null || currentLocationAirportCode.trim().isEmpty()) {
            // --- DEBUG START ---
            System.err.println("ERROR DEBUG (Airplane): Valor nulo o vacío recibido para currentLocationAirportCode en avión ID: " + this.id);
            // --- DEBUG END ---
            throw new IllegalArgumentException("El código del aeropuerto actual no puede ser nulo o vacío.");
        }
        this.currentLocationAirportCode = currentLocationAirportCode;
        // --- DEBUG START ---
        System.out.println("DEBUG (Airplane): Ubicación actual establecida a: '" + this.currentLocationAirportCode + "' para avión ID: " + this.id);
        // --- DEBUG END ---
    }

    /**
     * Establece el nuevo estado operativo del avión.
     * @param status El nuevo estado del avión.
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
     * @throws StackException Si ocurre un error al añadir el vuelo a la pila (ej. si la pila está llena, aunque
     * LinkedStack no debería tener este problema a menos que se defina).
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
     * @return Una cadena formateada con el ID, capacidad, ubicación y estado del avión.
     */
    @Override
    public String toString() {
        return "Avión [ID: " + id + ", Capacidad: " + capacity + ", Ubicación: " + currentLocationAirportCode + ", Estado: " + status + "]";
    }

    /**
     * Compara este objeto Airplane con otro objeto para determinar si son iguales.
     * Dos aviones se consideran iguales si tienen el mismo {@code id}.
     *
     * @param o El objeto a comparar con este Airplane.
     * @return {@code true} si los objetos son iguales (mismo ID), {@code false} en caso contrario.
     */
    @Override // Asegurarse de que se sobrescribe correctamente
    public boolean equals(Object o) {
        // Optimización: Si es la misma referencia de objeto, son iguales.
        if (this == o) return true;
        // Si el objeto es nulo o no es una instancia de Airplane, no son iguales.
        if (o == null || getClass() != o.getClass()) return false;
        // Realiza un 'cast' seguro al tipo Airplane.
        Airplane airplane = (Airplane) o;
        // La igualdad se basa únicamente en el 'id' del avión, que es el identificador único.
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
    @Override // Asegurarse de que se sobrescribe correctamente
    public int hashCode() {
        // Genera el hash code basado en el ID del avión, que es el identificador único.
        return Objects.hash(id);
    }
}