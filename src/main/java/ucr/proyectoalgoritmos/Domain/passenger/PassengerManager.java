package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;

import java.util.HashMap;
import java.util.Map;

public class PassengerManager {
    private Map<String, Passenger> passengers; // Mapea ID a objeto Passenger

    public PassengerManager() throws ListException {
        this.passengers = new HashMap<>();
    }

    public void registerPassenger(String id, String name, String nationality) throws ListException {
        if (passengers.containsKey(id)) {
            return; // Ya existe, no lo registramos de nuevo
        }
        Passenger newPassenger = new Passenger(id, name, nationality);
        passengers.put(id, newPassenger);
    }

    public Passenger searchPassenger(String id) {
        return passengers.get(id);
    }

    public int getPassengerCount() {
        return passengers.size();
    }

    public SinglyLinkedList getAllPassengerIds() throws ListException {
        SinglyLinkedList ids = new SinglyLinkedList();
        for (String id : passengers.keySet()) {
            ids.add(id);
        }
        return ids;
    }

    public DoublyLinkedList getAllPassengers() throws ListException {
        DoublyLinkedList all = new DoublyLinkedList();
        for (Passenger p : passengers.values()) {
            all.add(p);
        }
        return all;
    }

    // Simplemente añade el vuelo al historial personal del pasajero.
    public void processTicketPurchase(Passenger passenger, Flight flight) throws ListException {
        if (passenger != null && flight != null) {
            passenger.addFlightToHistory(flight);
        } else {
            System.err.println("ERROR PM: No se pudo añadir vuelo al historial del pasajero. Objeto nulo.");
        }
    }

    // Este método podría ser llamado por FlightSimulator para mostrar el historial
    public void addFlightToPassengerHistory(String passengerId, Flight flight) throws ListException {
        Passenger p = searchPassenger(passengerId);
        if (p != null) {
            p.addFlightToHistory(flight);
        } else {
            System.err.println("ERROR PM: Pasajero " + passengerId + " no encontrado para actualizar historial.");
        }
    }

    public void showFlightHistory(String passengerId) throws ListException {
        Passenger p = searchPassenger(passengerId);
        if (p != null) {
            System.out.println("Historial de Vuelos para Pasajero " + p.getName() + " (ID: " + p.getId() + "):");
            SinglyLinkedList history = p.getFlightHistory();
            if (history != null && !history.isEmpty()) {
                for (int i = 0; i < history.size(); i++) {
                    Flight f = (Flight) history.get(i);
                    System.out.println("  - Vuelo " + f.getFlightNumber() + ": " + f.getOriginAirportCode() + " -> " + f.getDestinationAirportCode() + " (Estado: " + f.getStatus() + ")");
                }
            } else {
                System.out.println("  (No tiene vuelos registrados)");
            }
        } else {
            System.err.println("ERROR: Pasajero con ID " + passengerId + " no encontrado para mostrar historial.");
        }
    }
}