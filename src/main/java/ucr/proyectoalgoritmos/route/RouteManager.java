package ucr.proyectoalgoritmos.route;

import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map; // If you use Map for route definitions in JSON

public class RouteManager {
    private Graph graph; // Your graph implementation
    private AirportManager airportManager;

    public RouteManager(AirportManager airportManager) {
        this.graph = new Graph(20); // Initialize with a suitable capacity for airports
        this.airportManager = airportManager;
    }

    public Graph getGraph() {
        return graph;
    }

    public void loadRoutesFromJson(String filename) throws IOException, ListException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Map<String, Object>> routesData = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>() {}.getType());

            if (routesData != null) {
                for (Map<String, Object> route : routesData) {
                    String origin = (String) route.get("origin");
                    String destination = (String) route.get("destination");
                    Double distanceDouble = (Double) route.get("distance");
                    Double durationDouble = (Double) route.get("duration");

                    int distance = (distanceDouble != null) ? distanceDouble.intValue() : 0;
                    int duration = (durationDouble != null) ? durationDouble.intValue() : 0;

                    // Ensure airports exist before adding edges
                    if (airportManager.findAirport(origin) != null && airportManager.findAirport(destination) != null) {
                        graph.addVertex(origin); // Add vertices if they don't exist (graph handles duplicates)
                        graph.addVertex(destination);
                        graph.addEdge(origin, destination, duration); // Using duration as weight for shortest path
                        graph.addEdge(destination, origin, duration); // Assuming routes are bidirectional for simplicity
                    } else {
                        System.err.println("ADVERTENCIA RM: No se pudo añadir ruta " + origin + "-" + destination + ". Uno o ambos aeropuertos no existen.");
                    }
                }
            }
        }
    }

    // Assumes your Graph class has a method to calculate shortest path (e.g., Dijkstra)
    public int calculateShortestRoute(String originCode, String destinationCode) throws ListException {
        if (!graph.containsVertex(originCode) || !graph.containsVertex(destinationCode)) {
            // System.out.println("DEBUG RM: Ruta no encontrada entre " + originCode + " y " + destinationCode + ". Uno o ambos aeropuertos no están en el grafo.");
            return Integer.MAX_VALUE; // Indicate no route
        }
        return graph.shortestPath(originCode, destinationCode); // Returns path weight (duration)
    }
}