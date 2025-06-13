package ucr.proyectoalgoritmos.Domain.flight;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList; // Importación correcta para tu CDLL
import ucr.proyectoalgoritmos.Domain.airplane.Airplane;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;

import java.time.LocalDateTime;
import java.util.Objects; // Para equals y hashCode

public class Flight { // Aquí NO está la palabra 'abstract'
    private String flightNumber;
    private String originAirportCode;
    private String destinationAirportCode;
    private LocalDateTime departureTime;
    private int capacity;
    private CircularDoublyLinkedList passengers; // Lista para guardar pasajeros en este vuelo
    private int occupancy; // Número actual de pasajeros a bordo
    private FlightStatus status;
    private Airplane airplane; // El avión asignado a este vuelo
    private int estimatedDurationMinutes; // Tiempo de vuelo estimado en minutos

    public enum FlightStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, ASSIGNED
    }

    /**
     * Construye un nuevo objeto Flight.
     *
     * @param flightNumber         Identificador único del vuelo.
     * @param originAirportCode    Código del aeropuerto de origen.
     * @param destinationAirportCode Código del aeropuerto de destino.
     * @param departureTime        Fecha y hora de salida programada.
     * @param capacity             Capacidad máxima de pasajeros del vuelo.
     * @throws ListException       Si hay un problema al inicializar la lista de pasajeros.
     * @throws IllegalArgumentException Si algún detalle del vuelo es inválido (nulo/cadena vacía, capacidad no positiva).
     */
    public Flight(String flightNumber, String originAirportCode, String destinationAirportCode,
                  LocalDateTime departureTime, int capacity) throws ListException {
        // Validación de entrada para los parámetros del constructor
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
        this.passengers = new CircularDoublyLinkedList(); // ¡Esta es la corrección clave!
        this.occupancy = 0; // El vuelo inicia sin pasajeros
        this.status = FlightStatus.SCHEDULED; // El estado inicial es SCHEDULED
    }

    // --- Getters ---
    public String getFlightNumber() { return flightNumber; }
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getCapacity() { return capacity; }
    public CircularDoublyLinkedList getPassengers() { return passengers; } // Retorna CircularDoublyLinkedList
    public int getOccupancy() { return occupancy; }
    public FlightStatus getStatus() { return status; }
    public Airplane getAirplane() { return airplane; }
    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }

    // --- Setters ---
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public void setStatus(FlightStatus status) { this.status = status; }
    public void setAirplane(Airplane airplane) {
        this.airplane = airplane;
        if (airplane != null) {
            this.capacity = airplane.getCapacity(); // Actualiza la capacidad del vuelo según el avión
        }
    }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    // El método setOccupancy se mantiene privado para asegurar la consistencia interna.
    // La ocupación solo debe cambiar a través de addPassenger y removePassenger.
    private void setOccupancy(int occupancy) { this.occupancy = occupancy; }


    // --- Gestión de Pasajeros ---

    /**
     * Añade un pasajero al vuelo si hay capacidad disponible.
     *
     * @param passenger El objeto Passenger a añadir.
     * @throws ListException Si el vuelo está lleno o ocurre un error con la lista subyacente.
     * @throws IllegalArgumentException Si el pasajero es nulo.
     */
    public void addPassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        if (occupancy >= capacity) {
            throw new ListException("El vuelo " + this.flightNumber + " está lleno. No se pudo añadir al pasajero " + passenger.getId() + ".");
        }
        if (this.passengers.contains(passenger)) {
            // Opcional: Si quieres evitar añadir el mismo pasajero dos veces
            throw new ListException("El pasajero " + passenger.getId() + " ya está en el vuelo " + this.flightNumber + ".");
        }

        this.passengers.add(passenger);
        this.occupancy++;
    }

    /**
     * Elimina un pasajero del vuelo.
     *
     * @param passenger El objeto Passenger a eliminar.
     * @throws ListException Si el pasajero no se encuentra en el vuelo o ocurre un error con la lista subyacente.
     * @throws IllegalArgumentException Si el pasajero es nulo.
     */
    public void removePassenger(Passenger passenger) throws ListException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo.");
        }
        if (this.passengers.isEmpty()) {
            throw new ListException("El vuelo " + this.flightNumber + " no tiene pasajeros para remover.");
        }
        // Asumiendo que el método remove de tu CircularDoublyLinkedList toma un Object y lo encuentra/elimina correctamente.
        if (this.passengers.contains(passenger)) {
            this.passengers.remove(passenger);
            this.occupancy--;
        } else {
            throw new ListException("El pasajero " + passenger.getId() + " no está en el vuelo " + this.flightNumber + ".");
        }
    }

    /**
     * Elimina a todos los pasajeros del vuelo y reinicia la ocupación a cero.
     *
     * @throws ListException Si ocurre un error con la lista subyacente durante la operación de limpieza.
     */
    public void clearPassengers() throws ListException {
        this.passengers.clear();
        this.occupancy = 0;
    }

    /**
     * Verifica si el vuelo está lleno.
     * @return true si la ocupación actual del vuelo es igual o excede su capacidad, false en caso contrario.
     */
    public boolean isFull() {
        return occupancy >= capacity;
    }

    /**
     * Compara este objeto Flight con otro objeto para verificar si son iguales.
     * Los vuelos se consideran iguales si sus números de vuelo son idénticos.
     *
     * @param o El objeto con el que comparar.
     * @return true si los objetos son iguales, false en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber);
    }

    /**
     * Devuelve un valor de código hash para el objeto.
     * El código hash se basa en el número de vuelo.
     *
     * @return Un valor de código hash para este objeto.
     */
    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }

    /**
     * Devuelve una representación de cadena del objeto Flight.
     *
     * @return Una cadena formateada que muestra los detalles del vuelo.
     */
    @Override
    public String toString() {
        return "Vuelo [Num: " + flightNumber + ", De: " + originAirportCode + ", A: " + destinationAirportCode +
                ", Salida: " + departureTime + ", Cap: " + capacity + ", Ocup: " + occupancy +
                ", Estado: " + status + (airplane != null ? ", Avión: " + airplane.getId() : "") + "]";
    }
}