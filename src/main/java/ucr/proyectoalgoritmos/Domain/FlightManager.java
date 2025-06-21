package ucr.proyectoalgoritmos.Domain;

import ucr.proyectoalgoritmos.Domain.Circular.CircularDoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.list.ListException;

public class FlightManager {

    private static FlightManager instance;
    // La lista de vuelos que será compartida
    private CircularDoublyLinkedList flightList;

    private FlightManager() {
        flightList = new CircularDoublyLinkedList();
    }

    public static synchronized FlightManager getInstance() {
        if (instance == null) {
            instance = new FlightManager();
        }
        return instance;
    }

    public CircularDoublyLinkedList getFlightList() {
        return flightList;
    }

    public void addFlight(Flight flight) {
        flightList.add(flight);
    }

    public void removeFlight(Flight flight) throws  ListException {
        flightList.remove(flight);
    }
}
