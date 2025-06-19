package ucr.proyectoalgoritmos.Domain.flight;

// Importa LinkedQueue en lugar de CircularDoublyLinkedList
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException; // Puede que necesites cambiar a QueueException para los errores específicos de la cola
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;
import ucr.proyectoalgoritmos.Domain.queue.QueueException; // Asegúrate de importar QueueException

import java.time.LocalDateTime;
import java.util.Objects;

public class Flight {
    private String flightNumber;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private int capacity; // Esta capacidad puede cambiar si se asigna un avión
    private LinkedQueue passengers; // AHORA ES LinkedQueue para la lista de pasajeros
    private int occupancy; // Número actual de pasajeros en el vuelo
    private FlightStatus status; // Estado actual del vuelo
    private Airplane airplane; // El avión asignado a este vuelo (puede ser nulo inicialmente)
    private int estimatedDurationMinutes; // Duración estimada del vuelo en minutos

    /**
     * Enumeración que define los posibles **estados operativos** en los que puede encontrarse un vuelo.
     */
    public enum FlightStatus {
        /**
         * El vuelo está planificado pero aún no ha despegado.
         */
        SCHEDULED,
        /**
         * El vuelo está en progreso, actualmente volando.
         */
        IN_PROGRESS,
        /**
         * El vuelo ha llegado a su destino.
         */
        COMPLETED,
        /**
         * El vuelo ha sido cancelado.
         */
        CANCELLED,
        /**
         * El vuelo ha sido asignado a un avión y está listo para operaciones.
         */
        ASSIGNED
    }

    /**
     * Constructor para crear una nueva instancia de un vuelo.
     * Un vuelo recién creado se establece por defecto en estado **SCHEDULED**.
     *
     * @param flightNumber El número único de identificación del vuelo.
     * @param originAirportCode El código IATA del aeropuerto de origen.
     * @param destinationAirportCode El código IATA del aeropuerto de destino.
     * @param departureTime La fecha y hora de salida programada del vuelo.
     * @param capacity La capacidad inicial de pasajeros del vuelo. Esta puede ser actualizada
     * si se asigna un avión con una capacidad diferente.
     * @throws IllegalArgumentException Si alguno de los parámetros obligatorios es nulo, vacío o inválido.
     * @throws QueueException Si ocurre un error al inicializar la cola de pasajeros. (Cambiado de ListException)
     */
    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity) throws QueueException { // Cambiado de ListException
        // Validaciones en el constructor para asegurar la integridad de los datos.
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de vuelo no puede ser nulo o vacío.");
        }
        if (originAirportCode == null || originAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto de origen no puede ser nulo o vacío.");
        }
        if (destinationAirportCode == null || destinationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto de destino no puede ser nulo o vacío.");
        }
        if (departureTime == null) {
            throw new IllegalArgumentException("La hora de salida no puede ser nula.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser un número positivo.");
        }

        this.flightNumber = flightNumber.trim();
        this.originAirportCode = originAirportCode.trim();
        this.destinationAirportCode = destinationAirportCode.trim();
        this.departureTime = departureTime;
        this.capacity = capacity;
        this.passengers = new LinkedQueue(); // AHORA SE INICIALIZA COMO LinkedQueue
        this.occupancy = 0; // Ocupación inicial en 0
        this.status = FlightStatus.SCHEDULED; // Estado inicial
        this.airplane = null; // Avión no asignado al inicio
        this.estimatedDurationMinutes = 0; // Duración por defecto, se puede setear después
    }

    // --- Getters ---
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getCapacity() { return capacity; }
    public LinkedQueue getPassengers() { return passengers; } // AHORA RETORNA LinkedQueue
    public int getOccupancy() { return occupancy; }
    public FlightStatus getStatus() { return status; }
    public Airplane getAirplane() { return airplane; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }

    // --- Setters ---
    /**
     * Establece una nueva hora de salida para el vuelo.
     * @param departureTime La nueva fecha y hora de salida.
     */
    public void setDepartureTime(LocalDateTime departureTime) {
        if (departureTime == null) {
            throw new IllegalArgumentException("La hora de salida no puede ser nula.");
        }
        this.departureTime = departureTime;
    }

    /**
     * Establece un nuevo estado para el vuelo.
     * @param status El nuevo estado del vuelo.
     */
    public void setStatus(FlightStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del vuelo no puede ser nulo.");
        }
        this.status = status;
    }

    /**
     * Asigna un avión a este vuelo. Al asignar un avión, la capacidad del vuelo
     * se actualiza para coincidir con la capacidad del avión.
     * Se valida que la ocupación actual del vuelo no exceda la capacidad del nuevo avión.
     * @param airplane El objeto {@link Airplane} a asignar al vuelo. Puede ser nulo para desasignar.
     * @throws IllegalArgumentException Si la ocupación actual excede la capacidad del avión
     * o si el avión proporcionado es nulo y el vuelo ya tiene pasajeros.
     */
    public void setAirplane(Airplane airplane) {
        if (airplane == null) {
            this.airplane = null;
            return;
        }

        // Validación clave: La ocupación actual no debe exceder la capacidad del avión que se asigna.
        if (this.occupancy > airplane.getCapacity()) {
            throw new IllegalArgumentException(
                    "No se puede asignar el avión '" + airplane.getId() +
                            "' al vuelo '" + this.flightNumber +
                            "' porque la ocupación actual (" + this.occupancy +
                            ") excede la capacidad del avión (" + airplane.getCapacity() + ")."
            );
        }
        this.airplane = airplane;
        this.capacity = airplane.getCapacity(); // Actualiza la capacidad del vuelo a la del avión asignado
    }

    /**
     * Establece la duración estimada del vuelo en minutos.
     * @param estimatedDurationMinutes La duración en minutos.
     */
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) {
        if (estimatedDurationMinutes < 0) {
            throw new IllegalArgumentException("La duración estimada no puede ser negativa.");
        }
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    // El setter para 'occupancy' es privado porque debe ser gestionado internamente
    // por los métodos addPassenger y removePassenger.
    private void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }

    /**
     * Añade un pasajero al vuelo.
     * @param passenger El objeto {@link Passenger} a añadir.
     * @throws IllegalArgumentException Si el pasajero es nulo.
     * @throws QueueException Si el vuelo está lleno. (Cambiado de ListException)
     */
    public void addPassenger(Passenger passenger) throws QueueException { // Cambiado a QueueException
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        if (occupancy >= capacity) {
            throw new QueueException("El vuelo " + this.flightNumber + " está lleno. No se pudo añadir al pasajero " + passenger.getId() + ".");
        }
        // Nota: Las colas no suelen verificar si un elemento ya existe antes de añadirlo (no tienen 'contains' eficiente).
        // Si la unicidad del pasajero en el vuelo es un requisito estricto, la lógica de validación
        // debería manejarse antes de llamar a este método, o añadir un conjunto auxiliar para rastrear la unicidad.
        this.passengers.enQueue(passenger); // AHORA USA enQueue
        this.occupancy++;
    }

    /**
     * Remueve el primer pasajero de la cola del vuelo (FIFO).
     * @return El objeto {@link Passenger} que fue removido.
     * @throws QueueException Si el vuelo no tiene pasajeros.
     */
    public Passenger deQueuePassenger() throws QueueException { // Renombrado y modificado para usar deQueue
        if (this.passengers.isEmpty()) {
            throw new QueueException("El vuelo " + this.flightNumber + " no tiene pasajeros para remover.");
        }
        this.occupancy--;
        return (Passenger) this.passengers.deQueue(); // Usa deQueue
    }

    /**
     * Vacía la lista de pasajeros del vuelo y restablece la ocupación a cero.
     * @throws QueueException Si ocurre un error al limpiar la lista de pasajeros. (Cambiado de ListException)
     */
    public void clearPassengers() throws QueueException { // Cambiado a QueueException
        this.passengers.clear();
        this.occupancy = 0;
    }

    /**
     * Verifica si el vuelo ha alcanzado su capacidad máxima.
     * @return {@code true} si la ocupación es igual o mayor a la capacidad, {@code false} en caso contrario.
     */
    public boolean isFull() {
        return occupancy >= capacity;
    }

    /**
     * Compara este objeto Flight con otro objeto para determinar si son iguales.
     * Dos vuelos se consideran iguales si tienen el mismo {@code flightNumber}.
     *
     * @param o El objeto a comparar con este Flight.
     * @return {@code true} si los objetos son iguales (mismo número de vuelo), {@code false} en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber);
    }

    /**
     * Retorna un valor de código hash para este objeto Flight.
     * Este método debe ser consistente con {@code equals()}: si dos objetos
     * son iguales según {@code equals()}, deben tener el mismo valor de {@code hashCode()}.
     * Se genera basándose en el {@code flightNumber} del vuelo.
     *
     * @return Un valor de código hash entero.
     */
    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }

    /**
     * Provee una representación en cadena de texto de la información del vuelo.
     * Útil para depuración y visualización en consola.
     * @return Una cadena formateada con los detalles del vuelo.
     */
    @Override
    public String toString() {
        return "Vuelo [Num: " + flightNumber + ", De: " + originAirportCode + ", A: " + destinationAirportCode +
                ", Salida: " + departureTime + ", Cap: " + capacity +
                ", Estado: " + status + (airplane != null ? ", Avión: " + airplane.getId() : "") +
                ", Ocupación: " + occupancy + "]"; // Añadido el campo de ocupación para mayor claridad
    }
}