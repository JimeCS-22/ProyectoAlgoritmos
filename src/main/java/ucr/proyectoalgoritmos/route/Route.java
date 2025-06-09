package ucr.proyectoalgoritmos.route; // Adjust package

public class Route { // Represents a single direct connection (edge)
    private String originAirportCode;
    private String destinationAirportCode;
    private int distance; // Weight in kilometers or minutes

    public Route(String originAirportCode, String destinationAirportCode, int distance) {
        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationAirportCode;
        this.distance = distance;
    }

    // --- Getters ---
    public String getOriginAirportCode() { return originAirportCode; }
    public String getDestinationAirportCode() { return destinationAirportCode; }
    public int getDistance() { return distance; }

    // --- Setter for distance (for modifying routes) ---
    public void setDistance(int distance) { this.distance = distance; }

    @Override
    public String toString() {
        return "Route{" +
                "from='" + originAirportCode + '\'' +
                ", to='" + destinationAirportCode + '\'' +
                ", distance=" + distance +
                '}';
    }

    // You might need equals() and hashCode() if you store Route objects directly in your graph,
    // especially for checking duplicates or removal.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return originAirportCode.equals(route.originAirportCode) &&
                destinationAirportCode.equals(route.destinationAirportCode);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(originAirportCode, destinationAirportCode);
    }
}