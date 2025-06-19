package ucr.proyectoalgoritmos.Domain.route; // <-- OJO: Este paquete es 'Domain.route', no 'route'

import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

/**
 * Renamed from Route to RouteGraphService to avoid conflict with JSON data class.
 * Representa y gestiona rutas aéreas, actuando como una fachada para un grafo dirigido.
 * Permite la manipulación de rutas utilizando códigos de aeropuerto en lugar de índices internos.
 */
public class RouteGraphService {
    private DirectedSinglyLinkedListGraph internalGraph;

    public RouteGraphService(int maxVertices) {
        this.internalGraph = new DirectedSinglyLinkedListGraph();
    }


    /**
     * @return Retorna la instancia del grafo interno.
     */
    public DirectedSinglyLinkedListGraph getInternalGraph() { // <-- ¡MÉTODO AÑADIDO!
        return internalGraph;
    }

    public void addVertex(String airportCode) throws ListException {
        internalGraph.addVertex(airportCode);
    }

    public boolean containsVertex(String airportCode) {
        return internalGraph.getIndexForAirportCode(airportCode) != -1;
    }

    public void addEdge(String originCode, String destinationCode, int weight) throws ListException, IllegalArgumentException {
        int u = internalGraph.getIndexForAirportCode(originCode);
        int v = internalGraph.getIndexForAirportCode(destinationCode);
        if (u == -1) {
            throw new IllegalArgumentException("El aeropuerto de origen '" + originCode + "' no existe como vértice.");
        }
        if (v == -1) {
            throw new IllegalArgumentException("El aeropuerto de destino '" + destinationCode + "' no existe como vértice.");
        }
        internalGraph.addEdge(u, v, weight);
    }

    public boolean modifyEdge(String originCode, String destinationCode, int newWeight) throws ListException, IllegalArgumentException {
        int u = internalGraph.getIndexForAirportCode(originCode);
        int v = internalGraph.getIndexForAirportCode(destinationCode);
        if (u == -1) {
            throw new IllegalArgumentException("El aeropuerto de origen '" + originCode + "' no existe como vértice.");
        }
        if (v == -1) {
            throw new IllegalArgumentException("El aeropuerto de destino '" + destinationCode + "' no existe como vértice.");
        }
        internalGraph.modifyEdge(u, v, newWeight);
        return true;
    }

    public int shortestPath(String startCode, String endCode) throws ListException {
        return internalGraph.shortestPath(startCode, endCode);
    }

    public int getNumVertices() {
        return internalGraph.getNumVertices();
    }

    public SinglyLinkedList getAllAirportCodes() throws ListException {
        return internalGraph.getAllAirportCodes();
    }

    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minDuration, int maxDuration) throws ListException {
        internalGraph.generateRandomRoutes(minRoutesPerAirport, maxRoutesPerAirport, minDuration, maxDuration);
    }

    public int getOutgoingRouteCount(String airportCode) {
        return internalGraph.getOutgoingRouteCount(airportCode);
    }
    /**
     * Verifica si existe una ARISTA DIRECTA (ruta de un solo segmento) entre dos aeropuertos.
     * Delega la verificación al método 'hasEdge' del grafo interno.
     * @param originCode El código del aeropuerto de origen.
     * @param destinationCode El código del aeropuerto de destino.
     * @return true si existe una arista directa, false en caso contrario.
     */
    public boolean hasDirectRoute(String originCode, String destinationCode) {
        try {
            // Llama al método hasEdge de tu grafo interno
            return internalGraph.hasEdge(originCode, destinationCode);
        } catch (ListException e) {
            System.err.println("Error al verificar ruta directa en grafo: " + e.getMessage());
            return false; // Si hay una excepción al acceder a la lista, asume que no hay ruta.
        }
    }

}