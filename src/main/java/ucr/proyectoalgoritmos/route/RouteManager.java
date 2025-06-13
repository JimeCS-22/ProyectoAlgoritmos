package ucr.proyectoalgoritmos.route;

import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph; // Importación correcta del grafo

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class RouteManager {
    private DirectedSinglyLinkedListGraph graph; // Usar el nombre del grafo directamente
    private AirportManager airportManager;

    public RouteManager(AirportManager airportManager) {
        // Se asume que el grafo no necesita un maxVertices en su constructor,
        // ya que maneja su tamaño dinámicamente.
        this.graph = new DirectedSinglyLinkedListGraph();
        this.airportManager = airportManager;
    }

    public DirectedSinglyLinkedListGraph getGraph() { // Retornar el tipo de grafo correcto
        return graph;
    }

    /**
     * Carga rutas desde un archivo JSON y las añade al grafo.
     * Verifica que los aeropuertos existan en el AirportManager.
     * @param filename Nombre del archivo JSON.
     * @throws IOException Si hay un error de lectura del archivo.
     * @throws ListException Si hay un error con las listas internas del grafo.
     */
    public void loadRoutesFromJson(String filename) throws IOException, ListException {
        try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
            Gson gson = new Gson();
            List<Map<String, Object>> routesData = gson.fromJson(reader, new TypeToken<List<Map<String, Object>>>() {}.getType());

            if (routesData != null) {
                for (Map<String, Object> route : routesData) {
                    String origin = (String) route.get("origin");
                    String destination = (String) route.get("destination");
                    Double distanceDouble = (Double) route.get("distance"); // Puedes usarlo o ignorarlo
                    Double durationDouble = (Double) route.get("duration");

                    // Asegurarse de que los valores no sean nulos antes de convertirlos
                    int duration = (durationDouble != null) ? durationDouble.intValue() : 0;

                    // Solo añadir la ruta si ambos aeropuertos existen en el AirportManager
                    if (airportManager.findAirport(origin) != null && airportManager.findAirport(destination) != null) {
                        // Asegurar que los vértices (aeropuertos) existan en el grafo antes de añadir la arista
                        // addVertex retorna el índice, pero aquí solo nos interesa que existan.
                        graph.addVertex(origin);
                        graph.addVertex(destination);

                        graph.addEdge(graph.getIndexForAirportCode(origin), graph.getIndexForAirportCode(destination), duration);
                    } else {
                        System.err.println("ADVERTENCIA RM: No se pudo añadir ruta " + origin + "-" + destination + ". Uno o ambos aeropuertos no existen en AirportManager.");
                    }
                }
            }
        }
    }

    /**
     * Calcula la ruta más corta (duración) entre dos códigos de aeropuerto.
     * @param originCode Código del aeropuerto de origen.
     * @param destinationCode Código del aeropuerto de destino.
     * @return La duración más corta en minutos, o Integer.MAX_VALUE si no hay ruta o los aeropuertos no existen.
     * @throws ListException Si hay un error al acceder a las listas internas del grafo.
     */
    public int calculateShortestRoute(String originCode, String destinationCode) throws ListException {
        // Verificar si los vértices existen en el grafo antes de intentar calcular la ruta
        if (!graph.containsVertex(originCode) || !graph.containsVertex(destinationCode)) {
            System.err.println("ADVERTENCIA RM: Ruta no encontrada. Uno o ambos aeropuertos no están en el grafo de rutas: " + originCode + " o " + destinationCode);
            return Integer.MAX_VALUE;
        }
        return graph.shortestPath(originCode, destinationCode);
    }
}