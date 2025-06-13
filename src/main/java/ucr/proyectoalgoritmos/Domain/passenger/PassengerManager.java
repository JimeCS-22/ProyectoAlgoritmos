package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.AVL; // Asegúrate de que tu clase AVL esté en Domain
import ucr.proyectoalgoritmos.Domain.TreeException; // Asegúrate de que tu clase TreeException esté en Domain
import ucr.proyectoalgoritmos.Domain.queue.QueueException; // Necesario por el historial de vuelos de Passenger

public class PassengerManager {
    public AVL passengers; // AVL tree para almacenar pasajeros

    public PassengerManager() {
        this.passengers = new AVL();
    }

    /**
     * Registra un nuevo pasajero en el sistema.
     * @param id El ID único del pasajero.
     * @param name El nombre del pasajero.
     * @param nationality La nacionalidad del pasajero.
     * @throws TreeException Si hay un error al insertar en el árbol AVL.
     */
    public void registerPassenger(String id, String name, String nationality) throws TreeException {
        Passenger tempPassenger = new Passenger(id);
        if (passengers.search(tempPassenger) != null) {
            //System.out.println("ERROR: El pasajero con ID " + id + " ya está registrado.");
            return; // Ya existe, no lo registramos de nuevo
        }
        Passenger newPassenger = new Passenger(id, name, nationality);
        passengers.insert(newPassenger); // Insertamos en el AVL
        //System.out.println("Pasajero " + id + " (" + name + ") registrado con éxito.");
    }

    /**
     * Busca un pasajero por su ID.
     * @param id El ID del pasajero a buscar.
     * @return El objeto Passenger si se encuentra, null en caso contrario.
     * @throws TreeException Si hay un error durante la operación de búsqueda en el árbol AVL.
     */
    public Passenger searchPassenger(String id) throws TreeException {
        return (Passenger) passengers.search(new Passenger(id));
    }

    /**
     * Obtiene el número total de pasajeros registrados.
     * @return El número de pasajeros.
     */
    public int getPassengerCount() {
        return passengers.size();
    }

    /**
     * Recupera una SinglyLinkedList de todos los IDs de pasajeros registrados.
     * @return Una SinglyLinkedList que contiene los IDs de los pasajeros.
     * @throws ListException Si hay un problema con la SinglyLinkedList.
     * @throws TreeException Si hay un problema al recorrer el árbol AVL.
     */
    public SinglyLinkedList getAllPassengerIds() throws ListException, TreeException {
        SinglyLinkedList ids = new SinglyLinkedList();
        DoublyLinkedList allPassengersList = passengers.inOrderList(); // Asumiendo que inOrderList devuelve DoublyLinkedList
        if (allPassengersList != null && !allPassengersList.isEmpty()) {
            for (int i = 0; i < allPassengersList.size(); i++) {
                Passenger p = (Passenger) allPassengersList.get(i);
                if (p != null) {
                    ids.add(p.getId());
                }
            }
        }
        return ids;
    }

    /**
     * Recupera una DoublyLinkedList de todos los objetos Passenger registrados.
     * @return Una DoublyLinkedList que contiene todos los objetos Passenger.
     * @throws ListException Si hay un problema con la DoublyLinkedList.
     * @throws TreeException Si hay un problema al recorrer el árbol AVL.
     */
    public DoublyLinkedList getAllPassengers() throws ListException, TreeException {
        return passengers.inOrderList(); // Asumiendo que inOrderList devuelve DoublyLinkedList
    }

    /**
     * Procesa la compra de un billete añadiendo el vuelo al historial del pasajero.
     * @param passenger El pasajero.
     * @param flight El vuelo que se está comprando.
     * @throws QueueException Si hay un problema al añadir el vuelo al historial de vuelos del pasajero (LinkedQueue).
     * @throws IllegalArgumentException Si el pasajero o el vuelo son nulos.
     */
    public void processTicketPurchase(Passenger passenger, Flight flight) throws QueueException, IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo para procesar la compra del billete.");
        }
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo para procesar la compra del billete.");
        }
        // Esto añade implícitamente el vuelo al historial de vuelos (LinkedQueue) del pasajero
        passenger.addFlightToHistory(flight);
        //System.out.println("Vuelo " + flight.getFlightNumber() + " añadido al historial del pasajero " + passenger.getId() + ".");
    }

    /**
     * Añade un vuelo al historial de un pasajero específico por su ID.
     * @param passengerId El ID del pasajero.
     * @param flight El vuelo a añadir.
     * @throws TreeException Si no se encuentra el pasajero en el árbol AVL.
     * @throws QueueException Si hay un problema al añadir el vuelo al historial de vuelos del pasajero (LinkedQueue).
     */
    public void addFlightToPassengerHistory(String passengerId, Flight flight) throws TreeException, QueueException {
        Passenger p = searchPassenger(passengerId);
        if (p != null) {
            p.addFlightToHistory(flight);
            //System.out.println("Vuelo " + flight.getFlightNumber() + " añadido al historial de " + p.getName() + " (" + passengerId + ").");
        } else {
            System.err.println("ERROR: Pasajero con ID " + passengerId + " no encontrado para actualizar historial.");
        }
    }

    /**
     * Muestra el historial de vuelos de un pasajero específico.
     * @param passengerId El ID del pasajero cuyo historial se va a mostrar.
     * @throws TreeException Si no se encuentra el pasajero en el árbol AVL.
     * @throws QueueException Si hay un problema al acceder a los elementos del historial de vuelos del pasajero (LinkedQueue).
     */
    public void showFlightHistory(String passengerId) throws TreeException, QueueException {
        Passenger p = searchPassenger(passengerId);
        if (p != null) {
            System.out.println("\n--- Historial de Vuelos para Pasajero " + p.getName() + " (ID: " + p.getId() + ") ---");
            ucr.proyectoalgoritmos.Domain.queue.LinkedQueue historyQueue = p.getFlightHistory();

            if (historyQueue != null && !historyQueue.isEmpty()) {
                // Se utiliza el método toString() de LinkedQueue para mostrar el contenido
                System.out.print(historyQueue.toString()); // El toString de LinkedQueue ya tiene un salto de línea inicial
            } else {
                System.out.println("  (No tiene vuelos registrados)");
            }
        } else {
            System.err.println("ERROR: Pasajero con ID " + passengerId + " no encontrado para mostrar historial.");
        }
    }
}