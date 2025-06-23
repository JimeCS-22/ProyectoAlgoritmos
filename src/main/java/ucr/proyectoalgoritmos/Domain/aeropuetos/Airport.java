package ucr.proyectoalgoritmos.Domain.aeropuetos;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import java.util.Objects; // Import required for Objects.equals and Objects.hash

/**
 * Representa un **aeropuerto** dentro del sistema de gestión de aeropuertos y rutas de vuelo.
 * Esta clase almacena información fundamental sobre el aeropuerto, su estado operativo
 * y una colección de vuelos que tienen programada su salida desde este lugar.
 */
public class Airport {
    /**
     * El **código único** que identifica al aeropuerto (ej. "SJO", "LAX").
     * Es crucial para la búsqueda y gestión.
     */
    private String code;
    /**
     * El **nombre completo** del aeropuerto (ej. "Juan Santamaría International Airport").
     */
    private String name;
    /**
     * El **país** donde se localiza el aeropuerto.
     */
    private String country;
    private DoublyLinkedList passengerQueue;
    /**
     * El **estado operativo actual** del aeropuerto, definido por la enumeración {@link AirportStatus}.
     * Permite saber si el aeropuerto está activo, cerrado o en mantenimiento.
     */
    private AirportStatus status;
    /**
     * La **pizarra de salidas** (departures board) que contiene una lista de los vuelos
     * programados para despegar desde este aeropuerto.
     * Esta lista está implementada usando una **SinglyLinkedList**, como lo requieren las especificaciones del proyecto.
     * Deberá contener objetos de tipo Flight.
     */
    private SinglyLinkedList departuresBoard; // Descomenta esta línea una vez que tengas tu SinglyLinkedList lista.

    /**
     * Enumeración que define los posibles **estados operativos** en los que puede encontrarse un aeropuerto.
     */
    public enum AirportStatus {
        /**
         * Indica que el aeropuerto está **completamente operativo** y funcionando con normalidad.
         */
        ACTIVE,
        /**
         * Indica que el aeropuerto está **cerrado** temporal o permanentemente.
         */
        CLOSED,
        /**
         * Indica que el aeropuerto se encuentra en **mantenimiento**, lo que podría
         * afectar sus operaciones.
         */
        UNDER_MAINTENANCE, INACTIVE
    }

    /**
     * Constructor para crear una nueva instancia de un aeropuerto.
     * Al ser creado, el aeropuerto se establece por defecto en estado **ACTIVE**.
     *
     * @param code El **código único** del aeropuerto. No debe ser nulo ni vacío.
     * @param name El **nombre** completo del aeropuerto.
     * @param country El **país** donde se encuentra el aeropuerto.
     */
    public Airport(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = AirportStatus.ACTIVE; // Estado por defecto
        this.departuresBoard = new SinglyLinkedList(); // Inicializar siempre
        this.passengerQueue = new DoublyLinkedList();
    }

    public Airport() {
        // Inicializa cualquier cosa a valores por defecto si es necesario,
        // pero para la deserialización de Jackson, a menudo es suficiente con que exista.
        this.status = AirportStatus.ACTIVE; // Un buen valor por defecto
        this.departuresBoard = new SinglyLinkedList(); // ¡AÑADIR ESTO!
        this.passengerQueue = new DoublyLinkedList();
    }

    /**
     * Constructor para crear una nueva instancia de un aeropuerto con un estado inicial específico.
     * Utilizado si necesitas cargar aeropuertos con un estado predefinido (ej. desde un archivo).
     * @param code El código único del aeropuerto.
     * @param name El nombre completo del aeropuerto.
     * @param country El país donde se encuentra el aeropuerto.
     * @param status El estado operativo inicial del aeropuerto.
     */
    public Airport(String code, String name, String country, AirportStatus status) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = status;
        this.departuresBoard = new SinglyLinkedList();
        this.passengerQueue = new DoublyLinkedList();
    }


    /**
     * Obtiene el **código** de identificación del aeropuerto.
     * @return Una cadena de texto con el código del aeropuerto.
     */
    public String getCode() {
        return code;
    }

    /**
     * Obtiene el **nombre** completo del aeropuerto.
     * @return Una cadena de texto con el nombre del aeropuerto.
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene el **país** donde está localizado el aeropuerto.
     * @return Una cadena de texto con el nombre del país.
     */
    public String getCountry() {
        return country;
    }

    /**
     * Obtiene el **estado operativo actual** del aeropuerto.
     * @return Un valor de la enumeración {@link AirportStatus} (ACTIVE, CLOSED, UNDER_MAINTENANCE).
     */
    public AirportStatus getStatus() {
        return status;
    }

    /**
     * Obtiene la **pizarra de salidas** del aeropuerto, que es una SinglyLinkedList
     * conteniendo los vuelos programados.
     * @return La SinglyLinkedList que representa la pizarra de salidas.
     */
    public SinglyLinkedList getDeparturesBoard() { return departuresBoard; }




    /**
     * Establece un **nuevo estado operativo** para el aeropuerto.
     * @param status El nuevo estado del aeropuerto, de tipo {@link AirportStatus}.
     */
    public void setStatus(AirportStatus status) { this.status = status; }


    /**
     * Establece una nueva **pizarra de salidas** para el aeropuerto.
     * @param departuresBoard La nueva SinglyLinkedList que representará la pizarra de salidas.
     */
    public void setDeparturesBoard(SinglyLinkedList departuresBoard) { this.departuresBoard = departuresBoard; }

    /**
     * Provee una **representación en cadena de texto** de la información del aeropuerto.
     * Útil para depuración y visualización en consola.
     * @return Una cadena formateada con el código, nombre, país y estado del aeropuerto.
     */
    @Override
    public String toString() {
        return "Aeropuerto [Código: " + code + ", Nombre: " + name + ", País: " + country + ", Estado: " + status + "]";
    }


    /**
     * Compara este objeto Airport con otro objeto para determinar si son iguales.
     * Dos aeropuertos se consideran iguales si tienen el mismo {@code code}.
     *
     * @param o El objeto a comparar con este Airport.
     * @return {@code true} si los objetos son iguales (mismo código), {@code false} en caso contrario.
     */

    public boolean equals(Object o) {
        // Optimización: Si es la misma referencia de objeto, son iguales.
        if (this == o) return true;
        // Si el objeto es nulo o no es de la misma clase, no son iguales.
        if (o == null || getClass() != o.getClass()) return false;
        // Realiza un 'cast' seguro al tipo Airport.
        Airport airport = (Airport) o;
        // La igualdad se basa únicamente en el código del aeropuerto, que es el identificador único.
        return Objects.equals(code, airport.code);
    }

    /**
     * Retorna un valor de código hash para este objeto Airport.
     * Este método debe ser consistente con {@code equals()}: si dos objetos
     * son iguales según {@code equals()}, deben tener el mismo valor de {@code hashCode()}.
     * Se genera basándose en el {@code code} del aeropuerto.
     *
     * @return Un valor de código hash entero.
     */
    @Override
    public int hashCode() {
        // Genera el hash code basado en el código del aeropuerto, que es el identificador único.
        return Objects.hash(code);
    }

    public DoublyLinkedList getPassengerQueue() { // Debe coincidir con el tipo de tu campo
        return passengerQueue;
    }

    // Setter para la cola de pasajeros (Jackson lo necesita para deserializar si está en el JSON)
    public void setPassengerQueue(DoublyLinkedList passengerQueue) {
        this.passengerQueue = passengerQueue;
    }

    public int getPassengerQueueSize() throws ListException { // ¡QUITAR throws ListException!
        return passengerQueue != null ? passengerQueue.size() : 0;
    }

    public int getDeparturesBoardSize() {
        return departuresBoard != null ? departuresBoard.size() : 0;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }


}