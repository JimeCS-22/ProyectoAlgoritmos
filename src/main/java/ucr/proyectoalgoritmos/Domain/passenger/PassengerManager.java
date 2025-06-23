package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import ucr.proyectoalgoritmos.UtilJson.PassengerJson;
import ucr.proyectoalgoritmos.util.Utility;

/**
 * Gestiona la colección de pasajeros utilizando un árbol AVL para una búsqueda y gestión eficiente.
 * Proporciona métodos para registrar, buscar, y manejar el historial de vuelos de los pasajeros.
 */
public class PassengerManager {
    private static AVL passengers;
    private static PassengerManager instance;

    public PassengerManager() {
        passengers = new AVL();
    }

    public static synchronized PassengerManager getInstance() {
        if (instance == null) {
            instance = new PassengerManager();
            instance.setPassengers(PassengerJson.loadPassengersFromJson());
        }
        return instance;
    }

    private void loadPassengers() {
        setPassengers(PassengerJson.loadPassengersFromJson());
    }

    public void savePassengers() {
        PassengerJson.savePassengersToJson(passengers);
    }

    public void registerPassenger(String id, String name, String nationality) throws TreeException, IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del pasajero no puede ser nulo o vacío.");
        }
        if (nationality == null || nationality.trim().isEmpty()) {
            throw new IllegalArgumentException("La nacionalidad del pasajero no puede ser nula o vacía.");
        }

        Passenger tempPassenger = new Passenger(id);
        if (passengers.search(tempPassenger) != null) {
            throw new IllegalArgumentException("El pasajero con ID " + id + " ya está registrado.");
        }

        Passenger newPassenger = new Passenger(id, name, nationality);
        passengers.insert(newPassenger);
    }

    public Passenger searchPassenger(String id) throws TreeException, IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para la búsqueda.");
        }
        return (Passenger) passengers.search(new Passenger(id));
    }

    public int getPassengerCount() {
        return passengers.size();
    }

    public SinglyLinkedList getAllPassengerIds() throws ListException, TreeException {
        SinglyLinkedList ids = new SinglyLinkedList();
        DoublyLinkedList allPassengersList = passengers.inOrderList();

        if (allPassengersList != null && !allPassengersList.isEmpty()) {
            for (int i = 0; i < allPassengersList.size(); i++) {
                Object element = allPassengersList.get(i);
                if (element instanceof Passenger passenger && passenger != null) {
                    ids.add(passenger.getId());
                }
            }
        }
        return ids;
    }

    public DoublyLinkedList getAllPassengers() throws ListException, TreeException {
        return passengers.inOrderList();
    }

    public void setPassengers(AVL passengers) {
        if (passengers == null) {
            this.passengers = new AVL();
        } else {
            this.passengers = passengers;
        }
    }

    public void processTicketPurchase(Passenger passenger, Flight flight) throws QueueException, IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo para procesar la compra del billete.");
        }
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo para procesar la compra del billete.");
        }
        passenger.addFlightToHistory(flight);
    }

    public void addFlightToPassengerHistory(String passengerId, Flight flight) throws TreeException, QueueException, IllegalArgumentException {
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para añadir al historial.");
        }
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo para añadir al historial.");
        }

        Passenger passenger = searchPassenger(passengerId);
        if (passenger != null) {
            passenger.addFlightToHistory(flight);
        } else {
            throw new IllegalArgumentException("Pasajero con ID " + passengerId + " no encontrado para actualizar historial.");
        }
    }

    public LinkedQueue getPassengerFlightHistory(String passengerId) throws TreeException, IllegalArgumentException {
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para obtener el historial.");
        }

        Passenger passenger = searchPassenger(passengerId);
        if (passenger == null) {
            throw new IllegalArgumentException("Pasajero con ID " + passengerId + " no encontrado para obtener historial.");
        }
        return passenger.getFlightHistory();
    }

    public Passenger getRandomPassenger() throws TreeException, ListException {
        if (passengers.isEmpty()) {
            return null;
        }

        DoublyLinkedList allPassengers = passengers.inOrderList();
        int randomIndex = Utility.random(allPassengers.size());
        return (Passenger) allPassengers.get(randomIndex);
    }

    public AVL getPassengersAVL() {
        return passengers;
    }

    public void removePassenger(String id) throws TreeException, IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para remover.");
        }

        Passenger passengerToRemove = new Passenger(id);
        Passenger found = (Passenger) passengers.search(passengerToRemove);

        if (found == null) {
            throw new IllegalArgumentException("El pasajero con ID " + id + " no se encontró para remover.");
        }

        passengers.remove(found);
    }

    public void processFlightCompletion(Passenger passenger, Flight completedFlight) throws QueueException, IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo para procesar la finalización del vuelo.");
        }
        if (completedFlight == null) {
            throw new IllegalArgumentException("El vuelo completado no puede ser nulo.");
        }

        passenger.addFlightToHistory(completedFlight);
    }

    public void removeAndRenumberPassengers(String idToRemove) throws TreeException, ListException {
        this.removePassenger(idToRemove);

        DoublyLinkedList allPassengers = passengers.inOrderList();
        passengers.clear();

        int newIdNumber = 1;
        for (int i = 0; i < allPassengers.size(); i++) {
            Passenger p = (Passenger) allPassengers.get(i);
            Passenger renumberedPassenger = new Passenger(
                    "P" + String.format("%03d", newIdNumber++),
                    p.getName(),
                    p.getNationality()
            );

            passengers.insert(renumberedPassenger);
        }

        PassengerJson.savePassengersToJson(passengers);
    }

    public boolean updatePassenger(Passenger updatedPassenger) throws Exception {
        if (updatedPassenger == null || updatedPassenger.getId() == null || updatedPassenger.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El pasajero o su ID no pueden ser nulos o vacíos");
        }

        try {
            Passenger existingPassenger = searchPassenger(updatedPassenger.getId());

            if (existingPassenger == null) {
                return false;
            }

            if (updatedPassenger.getName() == null || updatedPassenger.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del pasajero no puede estar vacío");
            }

            if (updatedPassenger.getNationality() == null || updatedPassenger.getNationality().trim().isEmpty()) {
                throw new IllegalArgumentException("La nacionalidad del pasajero no puede estar vacía");
            }

            existingPassenger.setName(updatedPassenger.getName());
            existingPassenger.setNationality(updatedPassenger.getNationality());

            savePassengers();

            return true;
        } catch (TreeException e) {
            throw new Exception("Error al acceder a la estructura de datos de pasajeros: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error inesperado al actualizar pasajero: " + e.getMessage());
        }
    }
}