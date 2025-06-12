package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.AVL; // Importamos tu clase AVL
import ucr.proyectoalgoritmos.Domain.TreeException; // Asumiendo que has creado TreeException

public class PassengerManager {
    public AVL passengers; // Ahora es público para acceso desde fuera

    public PassengerManager() {
        this.passengers = new AVL();
    }

    /**
     * Registra un nuevo pasajero en el sistema.
     *
     * @param id El ID único del pasajero.
     * @param name El nombre del pasajero.
     * @param nationality La nacionalidad del pasajero.
     * @throws ListException Si ocurre un error al manipular listas internas (ej. en el historial).
     * @throws TreeException Si ocurre un error con el árbol AVL (ej. elemento no Comparable).
     */
    public void registerPassenger(String id, String name, String nationality) throws ListException, TreeException {
        // Creamos un pasajero temporal para la búsqueda por ID
        Passenger tempPassenger = new Passenger(id);
        if (passengers.search(tempPassenger) != null) {
            System.out.println("ERROR: El pasajero con ID " + id + " ya está registrado.");
            return; // Ya existe, no lo registramos de nuevo
        }
        Passenger newPassenger = new Passenger(id, name, nationality);
        passengers.insert(newPassenger); // Insertamos en el AVL
        System.out.println("Pasajero " + id + " (" + name + ") registrado con éxito.");
    }

    /**
     * Busca un pasajero por su ID.
     *
     * @param id El ID del pasajero a buscar.
     * @return El objeto Passenger si se encuentra, o null si no.
     * @throws TreeException Si ocurre un error con el árbol AVL (ej. elemento no Comparable).
     */
    public Passenger searchPassenger(String id) throws TreeException {
        // Usamos search del AVL, creando un pasajero 'fantasma' para la búsqueda por ID
        return (Passenger) passengers.search(new Passenger(id));
    }

    /**
     * Obtiene el número total de pasajeros registrados.
     *
     * @return El conteo de pasajeros.
     */
    public int getPassengerCount() {
        // Asumiendo que tu clase AVL tiene un método size() que devuelve el conteo de nodos.
        // Si no lo tiene, podrías implementarlo o contar los nodos en inOrderList().
        // Si tu AVL no tiene size, esta línea causará un error.
        // Alternativa si no tienes size(): return passengers.inOrderList().size(); (menos eficiente)
        return passengers.size();
    }

    /**
     * Obtiene una lista con todos los IDs de los pasajeros registrados.
     * Recorre el AVL en orden para obtener los IDs.
     *
     * @return Una SinglyLinkedList con los IDs de los pasajeros.
     * @throws ListException Si ocurre un error al manipular la lista.
     * @throws TreeException Si ocurre un error al recorrer el árbol.
     */
    public SinglyLinkedList getAllPassengerIds() throws ListException, TreeException {
        SinglyLinkedList ids = new SinglyLinkedList();
        DoublyLinkedList allPassengers = passengers.inOrderList(); // Obtenemos todos los pasajeros en orden
        if (allPassengers != null && !allPassengers.isEmpty()) {
            for (int i = 0; i < allPassengers.size(); i++) {
                Passenger p = (Passenger) allPassengers.get(i);
                if (p != null) {
                    ids.add(p.getId()); // Añadimos solo el ID a la nueva lista
                }
            }
        }
        return ids;
    }

    /**
     * Obtiene una lista con todos los objetos Passenger registrados.
     * Recorre el AVL en orden para obtener los pasajeros.
     *
     * @return Una DoublyLinkedList con todos los objetos Passenger.
     * @throws ListException Si ocurre un error al manipular la lista.
     * @throws TreeException Si ocurre un error al recorrer el árbol.
     */
    public DoublyLinkedList getAllPassengers() throws ListException, TreeException {
        // Simplemente devolvemos la lista que ya genera el AVL en orden
        return passengers.inOrderList();
    }

    /**
     * Procesa la compra de un billete para un pasajero, añadiendo el vuelo a su historial.
     *
     * @param passenger El objeto Passenger.
     * @param flight El objeto Flight a añadir al historial.
     * @throws ListException Si ocurre un error al añadir el vuelo al historial del pasajero.
     */
    public void processTicketPurchase(Passenger passenger, Flight flight) throws ListException {
        if (passenger != null && flight != null) {
            passenger.addFlightToHistory(flight);
            System.out.println("DEBUG PM: Vuelo " + flight.getFlightNumber() + " añadido al historial de " + passenger.getName());
        } else {
            System.err.println("ERROR PM: No se pudo añadir vuelo al historial del pasajero. Objeto pasajero o vuelo nulo.");
        }
    }

    /**
     * Añade un vuelo al historial de un pasajero dado su ID.
     *
     * @param passengerId El ID del pasajero.
     * @param flight El vuelo a añadir.
     * @throws ListException Si ocurre un error en la lista de historial.
     * @throws TreeException Si ocurre un error al buscar el pasajero en el árbol.
     */
    public void addFlightToPassengerHistory(String passengerId, Flight flight) throws ListException, TreeException {
        Passenger p = searchPassenger(passengerId);
        if (p != null) {
            p.addFlightToHistory(flight);
        } else {
            System.err.println("ERROR PM: Pasajero " + passengerId + " no encontrado para actualizar historial.");
        }
    }

    /**
     * Muestra el historial de vuelos de un pasajero dado su ID.
     *
     * @param passengerId El ID del pasajero.
     * @throws ListException Si ocurre un error en la lista de historial.
     * @throws TreeException Si ocurre un error al buscar el pasajero en el árbol.
     */
    public void showFlightHistory(String passengerId) throws ListException, TreeException {
        Passenger p = searchPassenger(passengerId);
        if (p != null) {
            System.out.println("\n--- Historial de Vuelos para Pasajero " + p.getName() + " (ID: " + p.getId() + ") ---");
            SinglyLinkedList history = p.getFlightHistory();
            if (history != null && !history.isEmpty()) {
                for (int i = 0; i < history.size(); i++) {
                    Flight f = (Flight) history.get(i);
                    System.out.println("  - Vuelo " + f.getFlightNumber() + ": " + f.getOriginAirportCode() + " -> " + f.getDestinationAirportCode() + " (Estado: " + f.getStatus() + ")");
                }
            } else {
                System.out.println("  (No tiene vuelos registrados en su historial)");
            }
        } else {
            System.err.println("ERROR: Pasajero con ID " + passengerId + " no encontrado para mostrar historial.");
        }
    }
}