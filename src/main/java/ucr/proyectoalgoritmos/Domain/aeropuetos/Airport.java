package ucr.proyectoalgoritmos.Domain.aeropuetos;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import java.util.Objects;

/**
 * Representa un aeropuerto en el sistema de gestión de vuelos
 */
public class Airport {
    private String code;
    private String name;
    private String country;
    private DoublyLinkedList passengerQueue;
    private AirportStatus status;
    private SinglyLinkedList departuresBoard;

    public enum AirportStatus {
        ACTIVE,
        CLOSED,
        UNDER_MAINTENANCE,
        INACTIVE
    }

    public Airport(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = AirportStatus.ACTIVE;
        this.departuresBoard = new SinglyLinkedList();
        this.passengerQueue = new DoublyLinkedList();
    }

    public Airport() {
        this.status = AirportStatus.ACTIVE;
        this.departuresBoard = new SinglyLinkedList();
        this.passengerQueue = new DoublyLinkedList();
    }

    public Airport(String code, String name, String country, AirportStatus status) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = status;
        this.departuresBoard = new SinglyLinkedList();
        this.passengerQueue = new DoublyLinkedList();
    }

    // Getters y Setters básicos
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public AirportStatus getStatus() { return status; }
    public SinglyLinkedList getDeparturesBoard() { return departuresBoard; }
    public DoublyLinkedList getPassengerQueue() { return passengerQueue; }

    public void setStatus(AirportStatus status) { this.status = status; }
    public void setDeparturesBoard(SinglyLinkedList departuresBoard) { this.departuresBoard = departuresBoard; }
    public void setPassengerQueue(DoublyLinkedList passengerQueue) { this.passengerQueue = passengerQueue; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }

    // Métodos utilitarios
    public int getPassengerQueueSize() throws ListException {
        return passengerQueue != null ? passengerQueue.size() : 0;
    }

    public int getDeparturesBoardSize() {
        return departuresBoard != null ? departuresBoard.size() : 0;
    }

    @Override
    public String toString() {
        return "Aeropuerto [Código: " + code + ", Nombre: " + name + ", País: " + country + ", Estado: " + status + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(code, airport.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}