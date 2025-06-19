package ucr.proyectoalgoritmos.Domain.passenger;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.flight.Flight;
import ucr.proyectoalgoritmos.Domain.AVL;
import ucr.proyectoalgoritmos.Domain.TreeException;
import ucr.proyectoalgoritmos.Domain.queue.LinkedQueue;
import ucr.proyectoalgoritmos.Domain.queue.QueueException;
import ucr.proyectoalgoritmos.util.Utility; // Importar Utility para el generador de números aleatorios

/**
 * Gestiona la colección de pasajeros utilizando un árbol AVL para una búsqueda y gestión eficiente.
 * Proporciona métodos para registrar, buscar, y manejar el historial de vuelos de los pasajeros.
 */
public class PassengerManager {
    // CORRECCIÓN IMPORTANTE: Cambiado a private para encapsulamiento (ya lo tenías)
    private AVL passengers;

    /**
     * Constructor de PassengerManager. Inicializa el árbol AVL para almacenar pasajeros.
     */
    public PassengerManager() {
        this.passengers = new AVL();
    }

    /**
     * Registra un nuevo pasajero en el sistema.
     * Si un pasajero con el mismo ID ya existe, lanza una IllegalArgumentException.
     *
     * @param id El ID único del pasajero.
     * @param name El nombre del pasajero.
     * @param nationality La nacionalidad del pasajero.
     * @throws TreeException Si ocurre un error al interactuar con el árbol AVL (aunque el AVL debería manejar internamente las duplicidades).
     * @throws IllegalArgumentException Si un pasajero con el mismo ID ya está registrado.
     */
    public void registerPassenger(String id, String name, String nationality) throws TreeException, IllegalArgumentException {
        // Busca si el pasajero ya existe para evitar duplicados
        Passenger tempPassenger = new Passenger(id);
        if (passengers.search(tempPassenger) != null) {
            throw new IllegalArgumentException("El pasajero con ID " + id + " ya está registrado.");
        }
        // Crea e inserta el nuevo pasajero en el AVL
        Passenger newPassenger = new Passenger(id, name, nationality);
        passengers.insert(newPassenger);
    }

    /**
     * Busca un pasajero registrado por su ID.
     *
     * @param id El ID del pasajero a buscar.
     * @return El objeto Passenger si se encuentra, o null si no existe.
     * @throws TreeException Si ocurre un error al buscar en el árbol AVL.
     */
    public Passenger searchPassenger(String id) throws TreeException {
        // En una implementación robusta, podrías considerar lanzar una excepción si el ID es nulo o vacío
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para la búsqueda.");
        }
        return (Passenger) passengers.search(new Passenger(id));
    }

    /**
     * Obtiene el número total de pasajeros registrados en el sistema.
     *
     * @return El conteo de pasajeros.
     */
    public int getPassengerCount() {
        return passengers.size();
    }

    /**
     * Obtiene una lista simple de todos los IDs de los pasajeros registrados.
     *
     * @return Una SinglyLinkedList que contiene los IDs de los pasajeros.
     * @throws ListException Si ocurre un error al manejar las listas (e.g., si DoublyLinkedList lanza ListException).
     * @throws TreeException Si ocurre un error al obtener la lista del árbol AVL.
     */
    public SinglyLinkedList getAllPassengerIds() throws ListException, TreeException {
        SinglyLinkedList ids = new SinglyLinkedList();
        DoublyLinkedList allPassengersList = passengers.inOrderList(); // Obtiene todos los pasajeros en orden
        if (allPassengersList != null && !allPassengersList.isEmpty()) {
            for (int i = 0; i < allPassengersList.size(); i++) {
                Object element = allPassengersList.get(i);
                // Asegurarse de que el elemento es una instancia de Passenger y no es nulo.
                if (element instanceof Passenger p && p != null) {
                    ids.add(p.getId());
                } else {
                    // Manejar el caso donde un elemento no es un Passenger o es nulo (inesperado si el AVL funciona correctamente)
                    // Podrías loggear un warning o lanzar una excepción si esto es un estado de error grave.
                }
            }
        }
        return ids;
    }

    /**
     * Obtiene una lista doblemente enlazada de todos los objetos Passenger registrados.
     *
     * @return Una DoublyLinkedList que contiene todos los objetos Passenger.
     * @throws ListException Si ocurre un error al manejar las listas (e.g., en el método inOrderList del AVL).
     * @throws TreeException Si ocurre un error al obtener la lista del árbol AVL.
     */
    public DoublyLinkedList getAllPassengers() throws ListException, TreeException {
        return passengers.inOrderList();
    }

    /**
     * Procesa la compra de un billete añadiendo el vuelo al historial del pasajero.
     * Este método debería ser llamado después de que el vuelo haya sido exitosamente reservado para el pasajero.
     *
     * @param passenger El objeto Passenger al que se le añade el vuelo.
     * @param flight El objeto Flight que se añade al historial.
     * @throws QueueException Si ocurre un error al añadir el vuelo a la cola del historial.
     * @throws IllegalArgumentException Si el pasajero o el vuelo son nulos.
     */
    public void processTicketPurchase(Passenger passenger, Flight flight) throws QueueException, IllegalArgumentException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo para procesar la compra del billete.");
        }
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo para procesar la compra del billete.");
        }
        // Este método se encarga de añadir el vuelo al historial (LinkedQueue) del pasajero.
        passenger.addFlightToHistory(flight);
    }

    /**
     * Añade un vuelo al historial de un pasajero específico, buscándolo por su ID.
     *
     * @param passengerId El ID del pasajero.
     * @param flight El vuelo a añadir al historial.
     * @throws TreeException Si ocurre un error al buscar el pasajero en el árbol AVL.
     * @throws QueueException Si ocurre un error al añadir el vuelo a la cola del historial.
     * @throws IllegalArgumentException Si el ID del pasajero es nulo/vacío, el vuelo es nulo o el pasajero no se encuentra.
     */
    public void addFlightToPassengerHistory(String passengerId, Flight flight) throws TreeException, QueueException, IllegalArgumentException {
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para añadir al historial.");
        }
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo no puede ser nulo para añadir al historial.");
        }
        Passenger p = searchPassenger(passengerId); // searchPassenger ya tiene una validación para ID nulo/vacío.
        if (p != null) {
            p.addFlightToHistory(flight);
        } else {
            throw new IllegalArgumentException("Pasajero con ID " + passengerId + " no encontrado para actualizar historial.");
        }
    }

    /**
     * Obtiene y retorna el historial de vuelos de un pasajero específico.
     * Este método no imprime en consola, sino que retorna la cola del historial.
     *
     * @param passengerId El ID del pasajero.
     * @return La LinkedQueue que representa el historial de vuelos del pasajero (puede estar vacía, pero no null).
     * @throws TreeException Si ocurre un error al buscar el pasajero en el árbol AVL.
     * @throws IllegalArgumentException Si el ID del pasajero es nulo/vacío o el pasajero no se encuentra.
     */
    public LinkedQueue getPassengerFlightHistory(String passengerId) throws TreeException, IllegalArgumentException {
        if (passengerId == null || passengerId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para obtener el historial.");
        }
        Passenger p = searchPassenger(passengerId); // searchPassenger ya tiene una validación para ID nulo/vacío.
        if (p == null) {
            throw new IllegalArgumentException("Pasajero con ID " + passengerId + " no encontrado para obtener historial.");
        }
        return p.getFlightHistory();
    }

    /**
     * Obtiene un pasajero aleatorio de la lista de pasajeros registrados.
     * Útil para la simulación cuando se necesita seleccionar un pasajero al azar.
     *
     * @return Un objeto Passenger aleatorio, o null si no hay pasajeros registrados.
     * @throws TreeException Si ocurre un error al obtener la lista de pasajeros del árbol AVL.
     * @throws ListException Si ocurre un error al acceder a la lista de pasajeros.
     */
    public Passenger getRandomPassenger() throws TreeException, ListException {
        DoublyLinkedList allPassengers = passengers.inOrderList();
        if (allPassengers == null || allPassengers.isEmpty()) {
            return null; // No hay pasajeros para seleccionar
        }
        // CORRECCIÓN: Asumiendo que Utility.random(int bound) devuelve un entero aleatorio
        // entre 0 (inclusive) y bound (exclusive). Esto es una convención común.
        int randomIndex = Utility.random(allPassengers.size());
        return (Passenger) allPassengers.get(randomIndex);
    }

    /**
     * Retorna el árbol AVL de pasajeros directamente.
     * Este método puede ser útil para propósitos de prueba o para permitir
     * operaciones de bajo nivel si es estrictamente necesario, aunque
     * generalmente se prefiere interactuar a través de los métodos de PassengerManager.
     *
     * @return El árbol AVL que contiene los pasajeros.
     */
    public AVL getPassengersAVL() {
        return passengers;
    }

    /**
     * Remueve un pasajero del sistema por su ID.
     *
     * @param id El ID del pasajero a remover.
     * @throws TreeException Si ocurre un error al remover del árbol AVL.
     * @throws IllegalArgumentException Si el ID del pasajero es nulo/vacío o el pasajero con el ID especificado no se encuentra.
     */
    public void removePassenger(String id) throws TreeException, IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para remover.");
        }
        Passenger passengerToRemove = new Passenger(id);
        if (passengers.search(passengerToRemove) == null) {
            throw new IllegalArgumentException("El pasajero con ID " + id + " no se encontró para remover.");
        }
        passengers.remove(passengerToRemove);
    }

    /**
     * Procesa la finalización de un vuelo para un pasajero específico.
     * Esto implica añadir el vuelo al historial del pasajero.
     * Puedes añadir lógica adicional aquí si, por ejemplo, el pasajero tuviera un
     * estado de "en vuelo" que necesite ser actualizado.
     *
     * @param passenger El objeto Passenger cuyo vuelo ha finalizado.
     * @param completedFlight El objeto Flight que ha sido completado.
     * @throws QueueException Si ocurre un error al añadir el vuelo al historial del pasajero.
     * @throws IllegalArgumentException Si el pasajero o el vuelo son nulos.
     * @throws TreeException Si ocurre un error al buscar el pasajero (si se busca por ID en lugar de pasar el objeto).
     */
    public void processFlightCompletion(Passenger passenger, Flight completedFlight) throws QueueException, IllegalArgumentException, TreeException {
        if (passenger == null) {
            throw new IllegalArgumentException("El pasajero no puede ser nulo para procesar la finalización del vuelo.");
        }
        if (completedFlight == null) {
            throw new IllegalArgumentException("El vuelo completado no puede ser nulo.");
        }

        // Aquí simplemente delegamos a la función que añade el vuelo al historial del pasajero.
        // Si el PassengerManager necesitara hacer más cosas con el pasajero después de un vuelo (ej.
        // actualizar un contador de millas, cambiar un estado "en vuelo" a "en tierra", etc.),
        // esa lógica se añadiría aquí.
        passenger.addFlightToHistory(completedFlight);

        // Ejemplo de lógica adicional si Passenger tuviera un campo 'currentActiveFlight':
        // if (passenger.getCurrentActiveFlight() != null && passenger.getCurrentActiveFlight().equals(completedFlight)) {
        //     passenger.setCurrentActiveFlight(null); // Marcar que ya no está en ese vuelo
        // }
        // También podrías actualizar la ubicación del pasajero si la manejas a ese nivel.
    }
}