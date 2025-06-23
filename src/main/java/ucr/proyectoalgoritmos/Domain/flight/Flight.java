package ucr.proyectoalgoritmos.Domain.flight;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight {
    private String flightNumber;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private int capacity; // Esta capacidad puede cambiar si se asigna un avión
    private CircularDoublyLinkedList passengers; // Lista de pasajeros en el vuelo
    private int occupancy; // Número actual de pasajeros en el vuelo
    private FlightStatus status; // Estado actual del vuelo
    private Airplane airplane; // El avión asignado a este vuelo (puede ser nulo inicialmente)
    private int estimatedDurationMinutes; // Duración estimada del vuelo en minutos
    private String gate; // Atributo para la puerta de embarque

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
     * @throws ListException Si ocurre un error al inicializar la lista de pasajeros.
     */
    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity) throws ListException {
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
        this.passengers = new CircularDoublyLinkedList(); // Inicializa la lista de pasajeros
        this.occupancy = 0; // Ocupación inicial en 0
        this.status = FlightStatus.SCHEDULED; // Estado inicial
        this.airplane = null; // Avión no asignado al inicio
        this.estimatedDurationMinutes = 0; // Duración por defecto, se puede setear después
        this.gate = "N/A"; // Valor por defecto
    }

    // --- Getters ---
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getCapacity() { return capacity; }
    public CircularDoublyLinkedList getPassengers() { return passengers; }
    public int getOccupancy() { return occupancy; }
    public FlightStatus getStatus() { return status; }
    public Airplane getAirplane() { return airplane; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public String getGate() { return gate; }


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
            // Decide how to handle capacity when no airplane is assigned.
            // For now, if no plane is assigned, the flight's capacity might revert to its
            // initial constructor capacity or become effectively 0 depending on design.
            // This implementation maintains the last set capacity for flexibility.
            // Consider if a null airplane means the flight is no longer viable (e.g., set status to CANCELLED).
            this.airplane = null;
            // Optionally: set this.capacity = 0; or revert to initial capacity if applicable
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
    public void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }

    /**
     * Añade un pasajero al vuelo.
     * @param passenger El objeto {@link Passenger} a añadir.
     * @throws IllegalArgumentException Si el pasajero es nulo.
     * @throws ListException Si el vuelo está lleno o el pasajero ya está en el vuelo.
     */
    public void addPassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        if (occupancy >= capacity) {
            throw new ListException("El vuelo " + this.flightNumber + " está lleno. No se pudo añadir al pasajero " + passenger.getId() + ".");
        }
        // Asume que Passenger.equals() está correctamente implementado (por ID del pasajero)
        if (this.passengers.contains(passenger)) {
            throw new ListException("El pasajero " + passenger.getId() + " ya está en el vuelo " + this.flightNumber + ".");
        }

        this.passengers.add(passenger);
        this.occupancy++;
    }

    /**
     * Remueve un pasajero del vuelo.
     * @param passenger El objeto {@link Passenger} a remover.
     * @throws IllegalArgumentException Si el pasajero es nulo.
     * @throws ListException Si el vuelo no tiene pasajeros o el pasajero no está en el vuelo.
     */
    public void removePassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        if (this.passengers.isEmpty()) {
            throw new ListException("El vuelo " + this.flightNumber + " no tiene pasajeros para remover.");
        }
        // Asume que Passenger.equals() está correctamente implementado (por ID del pasajero)
        if (this.passengers.contains(passenger)) {
            this.passengers.remove(passenger);
            this.occupancy--;
        } else {
            throw new ListException("El pasajero " + passenger.getId() + " no está en el vuelo " + this.flightNumber + ".");
        }
    }

    /**
     * Vacía la lista de pasajeros del vuelo y restablece la ocupación a cero.
     * @throws ListException Si ocurre un error al limpiar la lista de pasajeros.
     */
    public void clearPassengers() throws ListException {
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

    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity, int occupancy, FlightStatus status) { // departureTime es LocalTime aquí
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de vuelo no puede ser nulo o vacío.");
        }
        if (originAirportCode == null || originAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto de origen no puede ser nulo o vacío.");
        }
        if (destinationAirportCode == null || destinationAirportCode.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del aeropuerto de destino no puede ser nulo o vacío.");
        }
        if (departureTime == null) { // Aquí usas LocalTime
            throw new IllegalArgumentException("La hora de salida no puede ser nula.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser un número positivo.");
        }
        if (occupancy < 0 || occupancy > capacity) {
            throw new IllegalArgumentException("La ocupación debe ser no negativa y no exceder la capacidad.");
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado del vuelo no puede ser nulo.");
        }

        this.flightNumber = flightNumber.trim();
        this.originAirportCode = originAirportCode.trim();
        this.destinationAirportCode = destinationAirportCode.trim();
        this.departureTime = departureTime; // Asignamos directamente el LocalTime
        this.capacity = capacity;
        this.occupancy = occupancy;
        this.status = status;
        this.passengers = new CircularDoublyLinkedList(); // Inicializa la lista de pasajeros
        this.airplane = null; // Avión no asignado al inicio
        this.estimatedDurationMinutes = 0; // Duración por defecto, se puede setear después
        this.gate = gate != null ? gate.trim() : "N/A";
    }

    public Flight() {
        // Inicializa tus listas y valores por defecto para que no sean nulos
        this.passengers = new CircularDoublyLinkedList();
        this.status = FlightStatus.SCHEDULED; // O el valor que desees por defecto
        this.occupancy = 0;
        this.estimatedDurationMinutes = 0;
        // Los Strings y LocalDateTime pueden ser null inicialmente y Jackson los rellenará
        this.gate = "N/A"; // Valor por defecto
    }

    public String getPassengersDisplay() {
        // Asegúrate de que 'passengers' no sea nulo antes de intentar acceder a él
        if (this.passengers == null || this.passengers.isEmpty()) {
            return "0/" + this.capacity; // Asumiendo que 'capacity' es accesible
        }
        return this.passengers.size() + "/" + this.capacity; // Asumiendo que 'capacity' es accesible
    }


    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public void setOriginAirportCode(String originAirportCode) {
        this.originAirportCode = originAirportCode;
    }

    public void setDestinationAirportCode(String destinationAirportCode) {
        this.destinationAirportCode = destinationAirportCode;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setPassengers(CircularDoublyLinkedList passengers) {
        this.passengers = passengers;
    }

    public void setGate(String gate) { // Setter para 'gate'
        this.gate = gate;
    }
}