package ucr.proyectoalgoritmos.Domain.route; // <--- This package was previously 'ucr.proyectoalgoritmos.Domain.route'. Make sure it's consistent.

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Objects;

public class RouteManager {

    private RouteGraphService routeService;

    private AirportManager airportManager;

    private Gson gson;
    private static RouteManager instance;


    public RouteManager(AirportManager airportManager) {
        this.airportManager = airportManager;

        this.routeService = new RouteGraphService(0);
        // Inicializa Gson con formato bonito para depuración.
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Carga las rutas desde un archivo JSON y las añade al grafo.
     * @param filePath La ruta del archivo JSON que contiene las rutas.
     * @throws IOException Si ocurre un error al leer el archivo.
     */
    public void loadRoutesFromJson(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath)) {
            RouteListWrapper wrapper = gson.fromJson(reader, RouteListWrapper.class);

            if (wrapper == null || wrapper.getRoutes() == null) {
                System.err.println("Advertencia: No se encontraron rutas en el archivo JSON o el formato es incorrecto.");
                return;
            }

            List<Route> loadedRoutes = wrapper.getRoutes();

            for (Route route : loadedRoutes) {
                try {

                    routeService.addDualWeightRoute(
                            route.getOrigin_airport_code(),
                            route.getDestination_airport_code(),
                            route.getDistance(),
                            route.getDuration()
                    );
                    routeService.addVertex(route.getOrigin_airport_code());
                    routeService.addVertex(route.getDestination_airport_code());

                } catch (ListException | IllegalArgumentException e) {
                    System.err.println("Error al procesar ruta: " + route.getOrigin_airport_code() + "->" + route.getDestination_airport_code() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error al cargar el archivo de rutas desde '" + filePath + "'. Asegúrese de que la ruta es correcta y el archivo existe.");
            throw e;
        }
    }


    public void addAirportToGraph(String airportCode) throws ListException {
        routeService.addVertex(airportCode);
    }


    public DirectedSinglyLinkedListGraph getGraph() {

        return routeService.getInternalGraph();
    }


    public int calculateShortestRoute(String startCode, String endCode) throws ListException {
        return routeService.shortestPath(startCode, endCode);
    }


    public boolean checkRouteExists(String originCode, String destinationCode) {

        return routeService.hasDirectRoute(originCode, destinationCode);
    }


    public static synchronized RouteManager getInstance(AirportManager airportManager) {
        if (instance == null) {
            instance = new RouteManager(airportManager);
        }
        return instance;
    }

    private RouteManager() {
        throw new UnsupportedOperationException("Use getInstance() instead");
    }

    public static class ShortestPathResult {
        public SinglyLinkedList path;
        public double totalDistance;
        public double totalDuration;

        public ShortestPathResult(SinglyLinkedList path, double totalDistance, double totalDuration) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.totalDuration = totalDuration;
        }

        @Override
        public String toString() {
            String pathStr = "Path: ";
            if (path != null && !path.isEmpty()) {
                try {
                    for (int i = 0; i < path.size(); i++) {
                        pathStr += path.get(i);
                        if (i < path.size() - 1) {
                            pathStr += " -> ";
                        }
                    }
                } catch (ListException e) {
                    pathStr += "[Error al iterar el path]";
                }
            } else {
                pathStr += "No path found.";
            }
            String distFormatted = String.format("%.2f", totalDistance);
            String durFormatted = String.format("%.0f", totalDuration);

            return pathStr + ", Distance: " + distFormatted + " km" + ", Duration: " + durFormatted + " min";
        }
    }

    public ShortestPathResult findShortestRouteDetails(String originCode, String destinationCode, String criteria) throws ListException { // <-- AQUÍ ESTÁ EL MÉTODO
        if (originCode == null || originCode.isEmpty() || destinationCode == null || destinationCode.isEmpty()) {
            throw new IllegalArgumentException("Los códigos de aeropuerto no pueden ser nulos o vacíos.");
        }

        if (!routeService.containsVertex(originCode)) {
            throw new IllegalArgumentException("El aeropuerto de origen '" + originCode + "' no existe en el grafo.");
        }
        if (!routeService.containsVertex(destinationCode)) {
            throw new IllegalArgumentException("El aeropuerto de destino '" + destinationCode + "' no existe en el grafo.");
        }

        double[] allWeights = routeService.getShortestPathDualWeights(originCode, destinationCode, criteria);

        if (allWeights == null) {
            return null;
        }

        SinglyLinkedList path = routeService.getPathDualWeight(originCode, destinationCode, criteria);

        double totalCalculatedDistance;
        double totalCalculatedDuration;

        if (criteria.equalsIgnoreCase("distance")) {
            totalCalculatedDistance = allWeights[0];
            totalCalculatedDuration = allWeights[1];
        } else if (criteria.equalsIgnoreCase("duration")) {
            totalCalculatedDuration = allWeights[0];
            totalCalculatedDistance = allWeights[1];
        } else {
            throw new IllegalArgumentException("Criterio inválido. Use 'distance' o 'duration'.");
        }

        return new ShortestPathResult(path, totalCalculatedDistance, totalCalculatedDuration);
    }



}