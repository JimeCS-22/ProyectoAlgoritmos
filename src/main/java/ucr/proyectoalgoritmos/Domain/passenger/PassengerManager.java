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

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Inicializa el árbol AVL para almacenar pasajeros.
     */
    public PassengerManager() {
        passengers = new AVL();
    }

    /**
     * Obtiene la instancia única del PassengerManager (Singleton).
     * @return La instancia única de PassengerManager
     */
    public static synchronized PassengerManager getInstance() {
        if (instance == null) {
            instance = new PassengerManager();
            instance.setPassengers(PassengerJson.loadPassengersFromJson());
            System.out.println("Pasajeros cargados: " + instance.getPassengerCount()); // Debug
        }
        return instance;
    }

    /**
     * Carga los pasajeros desde el archivo JSON.
     */
    private void loadPassengers() {
        setPassengers(PassengerJson.loadPassengersFromJson());
    }

    /**
     * Guarda los pasajeros en el archivo JSON.
     */
    public void savePassengers() {
        PassengerJson.savePassengersToJson(passengers);
    }

    /**
     * Registra un nuevo pasajero en el sistema.
     * @param id El ID único del pasajero.
     * @param name El nombre del pasajero.
     * @param nationality La nacionalidad del pasajero.
     * @throws TreeException Si ocurre un error al interactuar con el árbol AVL.
     * @throws IllegalArgumentException Si un pasajero con el mismo ID ya está registrado o si los parámetros son inválidos.
     */
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

    /**
     * Busca un pasajero registrado por su ID.
     * @param id El ID del pasajero a buscar.
     * @return El objeto Passenger si se encuentra, o null si no existe.
     * @throws TreeException Si ocurre un error al buscar en el árbol AVL.
     * @throws IllegalArgumentException Si el ID del pasajero es nulo o vacío.
     */
    public Passenger searchPassenger(String id) throws TreeException, IllegalArgumentException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del pasajero no puede ser nulo o vacío para la búsqueda.");
        }
        return (Passenger) passengers.search(new Passenger(id));
    }

    /**
     * Obtiene el número total de pasajeros registrados en el sistema.
     * @return El conteo de pasajeros.
     */
    public int getPassengerCount() {
        return passengers.size();
    }

    /**
     * Obtiene una lista simple de todos los IDs de los pasajeros registrados.
     * @return Una SinglyLinkedList que contiene los IDs de los pasajeros.
     * @throws ListException Si ocurre un error al manejar las listas.
     * @throws TreeException Si ocurre un error al obtener la lista del árbol AVL.
     */
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

    /**
     * Obtiene una lista doblemente enlazada de todos los objetos Passenger registrados.
     * @return Una DoublyLinkedList que contiene todos los objetos Passenger.
     * @throws ListException Si ocurre un error al manejar las listas.
     * @throws TreeException Si ocurre un error al obtener la lista del árbol AVL.
     */
    public DoublyLinkedList getAllPassengers() throws ListException, TreeException {
        return passengers.inOrderList();
    }

    /**
     * Establece la lista de pasajeros.
     * @param passengers El AVL con los pasajeros.
     */
    public void setPassengers(AVL passengers) {
        if (passengers == null) {
            this.passengers = new AVL();
        } else {
            this.passengers = passengers;
        }
    }

    /**
     * Procesa la compra de un billete añadiendo el vuelo al historial del pasajero.
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
        passenger.addFlightToHistory(flight);
    }

    /**
     * Añade un vuelo al historial de un pasajero específico, buscándolo por su ID.
     * @param passengerId El ID del pasajero.
     * @param flight El vuelo a añadir al historial.
     * @throws TreeException Si ocurre un error al buscar el pasajero en el árbol AVL.
     * @throws QueueException Si ocurre un error al añadir el vuelo a la cola del historial.
     * @throws IllegalArgumentException Si los parámetros son inválidos o el pasajero no se encuentra.
     */
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

    /**
     * Obtiene el historial de vuelos de un pasajero específico.
     * @param passengerId El ID del pasajero.
     * @return La LinkedQueue con el historial de vuelos.
     * @throws TreeException Si ocurre un error al buscar el pasajero.
     * @throws IllegalArgumentException Si el ID es inválido o el pasajero no se encuentra.
     */
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

    /**
     * Obtiene un pasajero aleatorio de la lista de pasajeros registrados.
     * @return Un objeto Passenger aleatorio, o null si no hay pasajeros.
     * @throws TreeException Si ocurre un error al obtener la lista de pasajeros.
     * @throws ListException Si ocurre un error al acceder a la lista.
     */
    public Passenger getRandomPassenger() throws TreeException, ListException {
        if (passengers.isEmpty()) {
            return null;
        }

        DoublyLinkedList allPassengers = passengers.inOrderList();
        int randomIndex = Utility.random(allPassengers.size());
        return (Passenger) allPassengers.get(randomIndex);
    }

    /**
     * Obtiene el árbol AVL de pasajeros.
     * @return El árbol AVL que contiene los pasajeros.
     */
    public AVL getPassengersAVL() {
        return passengers;
    }

    /**
     * Remueve un pasajero del sistema por su ID.
     * @param id El ID del pasajero a remover.
     * @throws TreeException Si ocurre un error al remover del árbol AVL.
     * @throws IllegalArgumentException Si el ID es inválido o el pasajero no se encuentra.
     */
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

    /**
     * Procesa la finalización de un vuelo para un pasajero específico.
     * @param passenger El objeto Passenger cuyo vuelo ha finalizado.
     * @param completedFlight El objeto Flight completado.
     * @throws QueueException Si ocurre un error al añadir el vuelo al historial.
     * @throws IllegalArgumentException Si los parámetros son nulos.
     */
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
        // 1. Eliminar el pasajero
        this.removePassenger(idToRemove);

        // 2. Obtener todos los pasajeros ordenados
        DoublyLinkedList allPassengers = passengers.inOrderList();

        // 3. Limpiar y reconstruir el AVL con nuevos IDs
        passengers.clear();

        int newIdNumber = 1;
        for (int i = 0; i < allPassengers.size(); i++) {
            Passenger p = (Passenger) allPassengers.get(i);
            // Crear nuevo pasajero con ID reordenado
            Passenger renumberedPassenger = new Passenger(
                    "P" + String.format("%03d", newIdNumber++),
                    p.getName(),
                    p.getNationality()
            );

            passengers.insert(renumberedPassenger);
        }

        // 4. Guardar cambios
        PassengerJson.savePassengersToJson(passengers);
    }

    /**
     * Actualiza la información de un pasajero existente en el sistema
     * @param updatedPassenger El objeto Passenger con la información actualizada
     * @return true si la actualización fue exitosa, false si no se encontró el pasajero
     * @throws Exception Si ocurre algún error durante el proceso de actualización
     */
    public boolean updatePassenger(Passenger updatedPassenger) throws Exception {
        if (updatedPassenger == null || updatedPassenger.getId() == null || updatedPassenger.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El pasajero o su ID no pueden ser nulos o vacíos");
        }

        try {
            // Buscar el pasajero existente
            Passenger existingPassenger = searchPassenger(updatedPassenger.getId());

            if (existingPassenger == null) {
                return false; // Pasajero no encontrado
            }

            // Validar los nuevos datos
            if (updatedPassenger.getName() == null || updatedPassenger.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del pasajero no puede estar vacío");
            }

            if (updatedPassenger.getNationality() == null || updatedPassenger.getNationality().trim().isEmpty()) {
                throw new IllegalArgumentException("La nacionalidad del pasajero no puede estar vacía");
            }

            // Actualizar los datos del pasajero existente
            existingPassenger.setName(updatedPassenger.getName());
            existingPassenger.setNationality(updatedPassenger.getNationality());

            // Guardar cambios
            savePassengers();

            return true;
        } catch (TreeException e) {
            throw new Exception("Error al acceder a la estructura de datos de pasajeros: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error inesperado al actualizar pasajero: " + e.getMessage());
        }
    }
}