package ucr.proyectoalgoritmos.route;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import java.util.List;
import java.util.Random;

public class Graph {
    private DirectedSinglyLinkedListGraph internalGraph;

    public Graph(int maxVertices) {
        this.internalGraph = new DirectedSinglyLinkedListGraph();
    }

    public void addVertex(String airportCode) throws ListException {
        internalGraph.addVertex(airportCode);
    }

    public boolean containsVertex(String airportCode) {
        return internalGraph.getIndexForAirportCode(airportCode) != -1;
    }

    public void addEdge(String originCode, String destinationCode, int weight) throws ListException {
        int u = internalGraph.getIndexForAirportCode(originCode);
        int v = internalGraph.getIndexForAirportCode(destinationCode);

        if (u == -1 || v == -1) {
            System.err.println("ERROR: No se pueden añadir aristas. Uno o ambos vértices no existen: " + originCode + ", " + destinationCode);
            return;
        }

        internalGraph.addEdge(u, v, weight);
    }

    public boolean modifyEdge(String originCode, String destinationCode, int newWeight) throws ListException {
        int u = internalGraph.getIndexForAirportCode(originCode);
        int v = internalGraph.getIndexForAirportCode(destinationCode);

        if (u == -1 || v == -1) {
            System.err.println("ERROR: No se puede modificar la arista. Uno o ambos vértices no existen: " + originCode + ", " + destinationCode);
            return false;
        }

        try {
            internalGraph.modifyEdge(u, v, newWeight);
            return true;
        } catch (ListException e) {
            throw e;
        }
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
}