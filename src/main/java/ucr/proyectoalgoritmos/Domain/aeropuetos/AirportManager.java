package ucr.proyectoalgoritmos.Domain.aeropuetos;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;

public class AirportManager {
    private DoublyLinkedList airports;

    public AirportManager() {
        this.airports = new DoublyLinkedList();
    }

    public void createAirport(String code, String name, String country) throws ListException {
        // Simple check to prevent duplicate airport codes
        if (findAirport(code) != null) {
            System.out.println("ADVERTENCIA: El aeropuerto con código " + code + " ya existe. No se añadió de nuevo.");
            return;
        }
        Airport newAirport = new Airport(code, name, country);
        airports.add(newAirport);
        // System.out.println("[INFO] Aeropuerto creado: " + newAirport.getName() + " (" + newAirport.getCode() + ")");
    }

    public Airport findAirport(String code) throws ListException {
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = (Airport) airports.get(i);
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }
        return null; // Not found
    }

    public String getAirportName(String code) {
        try {
            Airport airport = findAirport(code);
            return airport != null ? airport.getName() : "Desconocido (" + code + ")";
        } catch (ListException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public DoublyLinkedList getAllAirports() {
        return airports;
    }

    public int getAirportCount() throws ListException {
        return airports.size();
    }


}