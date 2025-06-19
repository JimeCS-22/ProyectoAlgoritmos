package ucr.proyectoalgoritmos.Domain.route;

/**
 * Representa una única ruta de vuelo con sus códigos de aeropuerto de origen y destino, y su distancia.
 * Esta clase está diseñada para ser utilizada por GSON para la deserialización del JSON de rutas.
 */
public class Route {
    // Estos nombres de campo DEBEN coincidir con las claves en tu JSON (case-sensitive)
    private String origin_airport_code;
    private String destination_airport_code;
    private int distance;

    // Constructor por defecto, necesario para GSON
    public Route() {
    }

    // Constructor con todos los campos (útil para la creación programática)
    public Route(String origin_airport_code, String destination_airport_code, int distance) {
        this.origin_airport_code = origin_airport_code;
        this.destination_airport_code = destination_airport_code;
        this.distance = distance;
    }

    // --- Getters ---
    public String getOrigin_airport_code() {
        return origin_airport_code;
    }

    public String getDestination_airport_code() {
        return destination_airport_code;
    }

    public int getDistance() {
        return distance;
    }

    // --- Setters (opcionales para deserialización si Gson puede acceder directamente a los campos) ---
    public void setOrigin_airport_code(String origin_airport_code) {
        this.origin_airport_code = origin_airport_code;
    }

    public void setDestination_airport_code(String destination_airport_code) {
        this.destination_airport_code = destination_airport_code;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Route{" +
                "origin_airport_code='" + origin_airport_code + '\'' +
                ", destination_airport_code='" + destination_airport_code + '\'' +
                ", distance=" + distance +
                '}';
    }
}