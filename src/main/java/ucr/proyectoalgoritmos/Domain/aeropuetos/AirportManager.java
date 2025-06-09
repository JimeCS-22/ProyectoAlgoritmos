package ucr.proyectoalgoritmos.Domain.aeropuetos; // Adjust package

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.util.Utility; // Your Utility class for compare method

public class AirportManager {
    private DoublyLinkedList airportList; // This will store Airport objects

    public AirportManager() {
        this.airportList = new DoublyLinkedList(); // Initialize your DoublyLinkedList
    }

    // a. Create Airport
    public void createAirport(String code, String name, String country) throws ListException {
        Airport newAirport = new Airport(code, name, country);
        // Check for existing airport code before adding
        if (findAirport(code) != null) {
            throw new ListException("Airport with code " + code + " already exists.");
        }
        airportList.add(newAirport); // Add to the DoublyLinkedList
        System.out.println("[INFO] Airport created: " + newAirport.getName() + " (" + newAirport.getCode() + ")");
    }

    // a. Edit Airport
    public void editAirport(String code, String newName, String newCountry, Airport.AirportStatus newStatus) throws ListException {
        Airport airportToEdit = findAirport(code);
        if (airportToEdit == null) {
            throw new ListException("Airport with code " + code + " not found.");
        }
        airportToEdit.setName(newName);
        airportToEdit.setCountry(newCountry);
        airportToEdit.setStatus(newStatus);
        System.out.println("[INFO] Airport " + code + " updated.");
    }

    // a. Delete Airport
    public void deleteAirport(String code) throws ListException {
        Airport airportToDelete = findAirport(code);
        if (airportToDelete == null) {
            throw new ListException("Airport with code " + code + " not found for deletion.");
        }
        airportList.remove(airportToDelete); // Remove from the DoublyLinkedList
        System.out.println("[INFO] Airport " + code + " deleted.");
    }

    // b. Activate or Deactivate Airports
    public void setAirportStatus(String code, Airport.AirportStatus status) throws ListException {
        Airport airport = findAirport(code);
        if (airport == null) {
            throw new ListException("Airport with code " + code + " not found.");
        }
        airport.setStatus(status);
        System.out.println("[INFO] Airport " + code + " status set to " + status);
    }

    // c. List Airports
    public void listAirports(boolean includeActive, boolean includeInactive) throws ListException {
        if (airportList.isEmpty()) {
            System.out.println("No airports to list.");
            return;
        }
        System.out.println("\n--- Airport List ---");
        for (int i = 0; i < airportList.size(); i++) {
            Airport airport = (Airport) airportList.get(i);
            boolean print = false;
            if (includeActive && airport.getStatus() == Airport.AirportStatus.ACTIVE) {
                print = true;
            }
            if (includeInactive && airport.getStatus() == Airport.AirportStatus.INACTIVE) {
                print = true;
            }
            if (print) {
                System.out.println(airport);
            }
        }
        System.out.println("--------------------");
    }

    // Helper method to find an airport by code
    public Airport findAirport(String code) throws ListException {
        if (airportList.isEmpty()) {
            return null;
        }
        // Iterate and compare using the Airport's code
        for (int i = 0; i < airportList.size(); i++) {
            Airport airport = (Airport) airportList.get(i);
            if (Utility.compare(airport.getCode(), code) == 0) { // Using Utility.compare for consistency
                return airport;
            }
        }
        return null; // Not found
    }

    public DoublyLinkedList getAllAirports() {
        return airportList;
    }

    public int getAirportCount() throws ListException {
        return airportList.size();
    }
}