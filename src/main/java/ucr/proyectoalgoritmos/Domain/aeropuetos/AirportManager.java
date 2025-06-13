package ucr.proyectoalgoritmos.Domain.aeropuetos;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

public class AirportManager {
    private DoublyLinkedList airports;

    public AirportManager() {
        this.airports = new DoublyLinkedList();
    }

    /**
     * Creates and adds a new airport to the system.
     * @param code The unique code of the airport (e.g., "SJO").
     * @param name The name of the airport.
     * @param country The country where the airport is located.
     * @throws ListException If there's an issue with the underlying DoublyLinkedList.
     */
    public void createAirport(String code, String name, String country) throws ListException {
        if (findAirport(code) != null) {
            System.out.println("ADVERTENCIA: El aeropuerto con código " + code + " ya existe. No se añadió de nuevo.");
            return;
        }
        Airport newAirport = new Airport(code, name, country);
        airports.add(newAirport);
        // System.out.println("[INFO] Aeropuerto creado: " + newAirport.getName() + " (" + newAirport.getCode() + ")");
    }

    /**
     * Finds an airport by its unique code.
     * @param code The code of the airport to find.
     * @return The Airport object if found, null otherwise.
     * @throws ListException If there's an issue accessing the list elements.
     */
    public Airport findAirport(String code) throws ListException {
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = (Airport) airports.get(i);
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }
        return null;
    }

    /**
     * Retrieves the name of an airport given its code.
     * @param code The code of the airport.
     * @return The name of the airport, or a "Desconocido" string if not found or an error occurs.
     */
    public String getAirportName(String code) {
        try {
            Airport airport = findAirport(code);
            return airport != null ? airport.getName() : "Desconocido (" + code + ")";
        } catch (ListException e) {
            // Log or handle the exception more robustly if needed
            return "ERROR al obtener nombre: " + e.getMessage();
        }
    }

    /**
     * Returns the underlying DoublyLinkedList containing all airports.
     * @return The DoublyLinkedList of Airport objects.
     */
    public DoublyLinkedList getAllAirports() {
        return airports;
    }

    /**
     * Gets the total count of airports in the system.
     * @return The number of airports.
     * @throws ListException If there's an issue with the list's size method.
     */
    public int getAirportCount() throws ListException {
        return airports.size();
    }

    /**
     * Deletes an airport from the system by its code.
     * @param code The code of the airport to delete.
     * @return true if the airport was found and removed, false otherwise.
     * @throws ListException If there's an issue with list operations.
     */
    public boolean deleteAirport(String code) throws ListException {
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = (Airport) airports.get(i);
            if (airport.getCode().equals(code)) {
                airports.remove(airport); // Assuming remove(Object) exists in DoublyLinkedList
                System.out.println("[INFO] Aeropuerto " + code + " eliminado.");
                return true;
            }
        }
        System.out.println("ADVERTENCIA: Aeropuerto con código " + code + " no encontrado para eliminar.");
        return false;
    }

    /**
     * Changes the status of an airport (active, closed, under_maintenance).
     * @param code The code of the airport to update.
     * @param newStatus The new status to set.
     * @return true if the airport was found and its status updated, false otherwise.
     * @throws ListException If there's an issue accessing the list elements.
     */
    public boolean activateOrDeactivateAirport(String code, Airport.AirportStatus newStatus) throws ListException {
        Airport airport = findAirport(code);
        if (airport != null) {
            airport.setStatus(newStatus);
            System.out.println("[INFO] Estado del aeropuerto " + code + " actualizado a: " + newStatus);
            return true;
        }
        System.out.println("ADVERTENCIA: Aeropuerto con código " + code + " no encontrado para actualizar estado.");
        return false;
    }

    /**
     * Lists airports based on their status.
     * @param showActive If true, active airports will be listed.
     * @param showClosed If true, closed airports will be listed.
     * @param showMaintenance If true, airports under maintenance will be listed.
     * @throws ListException If there's an issue accessing list elements.
     */
    public void listAirports(boolean showActive, boolean showClosed, boolean showMaintenance) throws ListException {
        System.out.println("\n--- Listado de Aeropuertos ---");
        boolean foundAny = false;
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = (Airport) airports.get(i);
            boolean shouldPrint = false;
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
                foundAny = true;
            }
        }
        if (!foundAny) {
            System.out.println("No se encontraron aeropuertos con los criterios de filtro especificados.");
        }
        System.out.println("--------------------------");
    }
}