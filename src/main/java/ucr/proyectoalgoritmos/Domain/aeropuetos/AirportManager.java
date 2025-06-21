package ucr.proyectoalgoritmos.Domain.aeropuetos;

import ucr.proyectoalgoritmos.Domain.FlightManager;
import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException; // ¡Asegúrate de que esta excepción esté definida en tu proyecto!

/**
 * La clase `AirportManager` es responsable de **gestionar la colección de aeropuertos**
 * dentro del sistema. Utiliza una {@link ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList}
 * para almacenar los objetos {@link Airport}.
 *
 * Provee operaciones para crear, buscar, eliminar, y actualizar el estado de los aeropuertos,
 * además de funcionalidades para listarlos.
 */
public class AirportManager {
    /**
     * La **lista doblemente enlazada** que contiene todos los objetos {@link Airport}
     * que están siendo gestionados por este manager. Es el TDA principal para aeropuertos.
     */
    private DoublyLinkedList airports;
    private static AirportManager instance;

    /**
     * Constructor para inicializar una nueva instancia de `AirportManager`.
     * Crea una **nueva lista doblemente enlazada vacía** donde se almacenarán los aeropuertos.
     */
    public AirportManager() {
        this.airports = new DoublyLinkedList();
    }

    /**
     * Crea un **nuevo aeropuerto** y lo añade a la lista de aeropuertos gestionados.
     * Antes de añadir, verifica si ya existe un aeropuerto con el mismo código.
     * Si ya existe, imprime una advertencia y no lo añade.
     *
     * @param code El **código único** (String) del nuevo aeropuerto.
     * @param name El **nombre** (String) del nuevo aeropuerto.
     * @param country El **país** (String) donde se ubica el nuevo aeropuerto.
     * @throws ListException Si ocurre un problema interno al añadir el aeropuerto a la {@link DoublyLinkedList}.
     */
    public void createAirport(String code, String name, String country) throws ListException {
        // Validación para evitar aeropuertos duplicados por código.
        if (findAirport(code) != null) {
            // En una aplicación real con UI, esta advertencia se manejaría con una excepción
            // (ej. AirportAlreadyExistsException) para notificar a la interfaz de usuario.
            System.out.println("ADVERTENCIA: El aeropuerto con código " + code + " ya existe. No se añadió de nuevo.");
            return;
        }
        Airport newAirport = new Airport(code, name, country);
        this.airports.add(newAirport); // Añade el nuevo aeropuerto a la lista.
        // System.out.println("[INFO] Aeropuerto creado: " + newAirport.getName() + " (" + newAirport.getCode() + ")");
    }

    /**
     * Busca un aeropuerto en la lista por su **código único**.
     *
     * @param code El **código** (String) del aeropuerto a buscar.
     * @return El objeto {@link Airport} si se encuentra un aeropuerto con el código especificado;
     * retorna `null` si no se encuentra ningún aeropuerto.
     * @throws ListException Si ocurre un error al iterar o acceder a elementos de la lista.
     */
    public Airport findAirport(String code) throws ListException {
        // Itera sobre la lista doblemente enlazada para encontrar el aeropuerto.
        for (int i = 0; i < this.airports.size(); i++) {
            Airport airport = (Airport) this.airports.get(i); // Se asume que los elementos son de tipo Airport.
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }
        return null; // El aeropuerto no fue encontrado.
    }

    /**
     * Obtiene el **nombre** de un aeropuerto dado su código.
     * Es un método de conveniencia para la interfaz de usuario o para otras partes del sistema.
     *
     * @param code El **código** (String) del aeropuerto cuyo nombre se desea obtener.
     * @return El nombre del aeropuerto si se encuentra; de lo contrario, devuelve
     * "Desconocido (código)" o un mensaje de error si se produce una `ListException`.
     */
    public String getAirportName(String code) {
        try {
            Airport airport = findAirport(code);
            // Uso de operador ternario para un retorno conciso.
            return airport != null ? airport.getName() : "Desconocido (" + code + ")";
        } catch (ListException e) {
            // Manejo básico de la excepción; en un entorno de producción, se debería registrar
            // el error o propagarlo de manera más robusta.
            return "ERROR al obtener nombre: " + e.getMessage();
        }
    }

    /**
     * Retorna la **lista completa** de aeropuertos gestionados.
     *
     * @return La {@link DoublyLinkedList} que contiene todos los objetos {@link Airport}.
     * Esta lista es la misma que usa el manager internamente.
     */
    public DoublyLinkedList getAllAirports() {
        return this.airports;
    }

    /**
     * Obtiene el **número total de aeropuertos** que están siendo gestionados actualmente.
     *
     * @return Un entero que representa la cantidad de aeropuertos en la lista.
     * @throws ListException Si ocurre un error al intentar obtener el tamaño de la lista.
     */
    public int getAirportCount() throws ListException {
        return this.airports.size();
    }

    /**
     * Elimina un aeropuerto de la lista basándose en su **código único**.
     *
     * @param code El **código** (String) del aeropuerto a eliminar.
     * @return `true` si el aeropuerto fue encontrado y eliminado exitosamente;
     * `false` si el aeropuerto no fue encontrado en la lista.
     * @throws ListException Si ocurre un error al intentar remover el elemento de la {@link DoublyLinkedList}.
     */
    public boolean deleteAirport(String code) throws ListException {
        // Se itera para encontrar el objeto Airport y luego se utiliza el método remove de la lista.
        for (int i = 0; i < this.airports.size(); i++) {
            Airport airport = (Airport) this.airports.get(i);
            if (airport.getCode().equals(code)) {
                this.airports.remove(airport); // Asume que DoublyLinkedList.remove(Object) funciona.
                System.out.println("[INFO] Aeropuerto " + code + " eliminado.");
                return true;
            }
        }
        // Similar a createAirport, esta advertencia podría ser una excepción en la UI.
        System.out.println("ADVERTENCIA: Aeropuerto con código " + code + " no encontrado para eliminar.");
        return false;
    }

    /**
     * Activa o desactiva un aeropuerto, o cambia su estado a mantenimiento.
     *
     * @param code El **código** (String) del aeropuerto cuyo estado se desea actualizar.
     * @param newStatus El **nuevo estado** ({@link Airport.AirportStatus}) a asignar al aeropuerto.
     * @return `true` si el aeropuerto fue encontrado y su estado fue actualizado;
     * `false` si el aeropuerto no fue encontrado.
     * @throws ListException Si ocurre un error al buscar el aeropuerto en la lista.
     */
    public boolean activateOrDeactivateAirport(String code, Airport.AirportStatus newStatus) throws ListException {
        Airport airport = findAirport(code);
        if (airport != null) {
            airport.setStatus(newStatus);
            System.out.println("[INFO] Estado del aeropuerto " + code + " actualizado a: " + newStatus);
            return true;
        }
        // Mensaje de advertencia si el aeropuerto no existe.
        System.out.println("ADVERTENCIA: Aeropuerto con código " + code + " no encontrado para actualizar estado.");
        return false;
    }

    /**
     * Imprime en la consola un **listado de los aeropuertos**, permitiendo filtrar por su estado.
     *
     * @param showActive Indica si se deben incluir los aeropuertos con estado {@code ACTIVE}.
     * @param showClosed Indica si se deben incluir los aeropuertos con estado {@code CLOSED}.
     * @param showMaintenance Indica si se deben incluir los aeropuertos con estado {@code UNDER_MAINTENANCE}.
     * @throws ListException Si ocurre un error al acceder a los elementos de la lista de aeropuertos.
     */
    public void listAirports(boolean showActive, boolean showClosed, boolean showMaintenance) throws ListException {
        System.out.println("\n--- Listado de Aeropuertos ---");
        boolean foundAny = false; // Bandera para saber si se imprimió al menos un aeropuerto.

        // Itera sobre cada aeropuerto en la lista para aplicar los filtros.
        for (int i = 0; i < this.airports.size(); i++) {
            Airport airport = (Airport) this.airports.get(i);
            boolean shouldPrint = false; // Determina si el aeropuerto actual cumple los criterios de filtro.

            // Lógica para aplicar los filtros de estado.
            if (showActive && airport.getStatus() == Airport.AirportStatus.ACTIVE) {
                shouldPrint = true;
            }
            if (showClosed && airport.getStatus() == Airport.AirportStatus.CLOSED) {
                shouldPrint = true;
            }
            if (showMaintenance && airport.getStatus() == Airport.AirportStatus.UNDER_MAINTENANCE) {
                shouldPrint = true;
            }

            if (shouldPrint) {
                System.out.println(airport);
                foundAny = true; // Se encontró al menos un aeropuerto que cumple el filtro.
            }
        }
        if (!foundAny) {
            System.out.println("No se encontraron aeropuertos con los criterios de filtro especificados.");
        }
        System.out.println("--------------------------");
    }

    public static synchronized AirportManager getInstance() {
        if (instance == null) {
            instance = new AirportManager();
        }
        return instance;
    }

    public DoublyLinkedList getAirportList() {
        return airports;
    }

    public void addAirport(Airport airport) throws ListException {
        if (airport == null) {
            throw new ListException("El aeropuerto no puede ser nulo.");
        }
        this.airports.add(airport);
    }
}