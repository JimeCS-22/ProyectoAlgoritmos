package ucr.proyectoalgoritmos.Domain.aeropuetos;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.UtilJson.AirportJson;
import java.io.IOException;

public class AirportManager {
    private DoublyLinkedList airports;
    private static AirportManager instance;

    public AirportManager() {
        this.airports = new DoublyLinkedList();
    }

    public static synchronized AirportManager getInstance() {
        if (instance == null) {
            instance = new AirportManager();
            instance.setAirports(AirportJson.loadAirportsFromJson());
        }
        return instance;
    }

    public void createAirport(String code, String name, String country) throws ListException {
        if (findAirport(code) != null) {
            throw new ListException("El aeropuerto con código " + code + " ya existe");
        }
        Airport newAirport = new Airport(code, name, country);
        this.airports.add(newAirport);
    }

    public Airport findAirport(String code) throws ListException {
        for (int i = 0; i < this.airports.size(); i++) {
            Airport airport = (Airport) this.airports.get(i);
            if (airport.getCode().equals(code)) {
                return airport;
            }
        }
        return null;
    }

    public String getAirportName(String code) {
        try {
            Airport airport = findAirport(code);
            return airport != null ? airport.getName() : "Desconocido (" + code + ")";
        } catch (ListException e) {
            return "Error al obtener nombre";
        }
    }

    public DoublyLinkedList getAllAirports() {
        return this.airports;
    }

    public int getAirportCount() throws ListException {
        return this.airports.size();
    }

    public boolean deleteAirport(String code) throws ListException {
        for (int i = 0; i < this.airports.size(); i++) {
            Airport airport = (Airport) this.airports.get(i);
            if (airport.getCode().equals(code)) {
                this.airports.remove(airport);
                return true;
            }
        }
        return false;
    }

    public boolean updateAirportStatus(String code, Airport.AirportStatus newStatus) throws ListException {
        Airport airport = findAirport(code);
        if (airport != null) {
            airport.setStatus(newStatus);
            return true;
        }
        return false;
    }

    public void setAirports(DoublyLinkedList airports) {
        this.airports = airports != null ? airports : new DoublyLinkedList();
    }

    public void addAirport(Airport airport) throws ListException {
        if (airport == null) {
            throw new ListException("El aeropuerto no puede ser nulo");
        }
        this.airports.add(airport);
    }

    public Airport getAirportByName(String name) throws ListException {
        for (int i = 0; i < this.airports.size(); i++) {
            Airport airport = (Airport) this.airports.get(i);
            if (airport.getName().equalsIgnoreCase(name)) {
                return airport;
            }
        }
        return null;
    }

    public void updateAirport(Airport updatedAirport) throws ListException {
        if (updatedAirport == null) {
            throw new ListException("El aeropuerto a actualizar no puede ser nulo");
        }

        for (int i = 0; i < this.airports.size(); i++) {
            Airport existingAirport = (Airport) this.airports.get(i);
            if (existingAirport.getCode().equals(updatedAirport.getCode())) {
                existingAirport.setName(updatedAirport.getName());
                existingAirport.setCountry(updatedAirport.getCountry());
                existingAirport.setStatus(updatedAirport.getStatus());
                return;
            }
        }
        throw new ListException("No se encontró el aeropuerto para actualizar");
    }

    public void loadAirportsFromJson(String airportsJsonPath) throws IOException, ListException {
        this.airports = AirportJson.loadAirportsFromJson();
    }

    public Airport findAirportByCode(String airportCode) throws ListException {
        if (airportCode == null || airportCode.isEmpty()) {
            return null;
        }

        for (int i = 0; i < airports.size(); i++) {
            Airport currentAirport = (Airport) airports.get(i);
            if (currentAirport.getCode().equalsIgnoreCase(airportCode)) {
                return currentAirport;
            }
        }
        return null;
    }
}