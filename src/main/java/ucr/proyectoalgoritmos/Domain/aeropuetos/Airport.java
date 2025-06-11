package ucr.proyectoalgoritmos.Domain.aeropuetos;

public class Airport {
    private String code;
    private String name;
    private String country;
    private AirportStatus status;

    public enum AirportStatus {
        ACTIVE, CLOSED, UNDER_MAINTENANCE
    }

    public Airport(String code, String name, String country) {
        this.code = code;
        this.name = name;
        this.country = country;
        this.status = AirportStatus.ACTIVE; // Default status
    }

    // Getters
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCountry() { return country; }
    public AirportStatus getStatus() { return status; }

    // Setters
    public void setStatus(AirportStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Aeropuerto [Código: " + code + ", Nombre: " + name + ", País: " + country + ", Estado: " + status + "]";
    }
}