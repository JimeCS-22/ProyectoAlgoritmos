package ucr.proyectoalgoritmos.route; // Adjust package


import ucr.proyectoalgoritmos.Domain.dijkstra.Dijkstra;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import java.util.Arrays;
import java.util.Stack; // For path reconstruction (if implementing)

public class RouteManager {
    private DirectedSinglyLinkedListGraph graph;

    public RouteManager() {
        this.graph = new DirectedSinglyLinkedListGraph();
    }

    // a. Add route (delegates to graph)
    public void addRoute(String originCode, String destinationCode, int weight) throws ListException {
        graph.addRoute(originCode, destinationCode, weight);
    }

    // b. Modify route (delegates to graph)
    public void modifyRoute(String originCode, String destinationCode, int newWeight) throws ListException {
        graph.modifyRoute(originCode, destinationCode, newWeight);
    }

    // c. Calculate the shortest route between two airports using Dijkstra
    public int calculateShortestRoute(String originCode, String destinationCode) throws ListException {
        int originIndex = graph.getIndexForAirportCode(originCode);
        int destinationIndex = graph.getIndexForAirportCode(destinationCode);

        if (originIndex == -1) {
            throw new ListException("Origin airport " + originCode + " not found in routes graph.");
        }
        if (destinationIndex == -1) {
            throw new ListException("Destination airport " + destinationCode + " not found in routes graph.");
        }
        if (originIndex == destinationIndex) { // Business Rule: No flights to the same airport implies 0 duration
            return 0;
        }

        int numVertices = graph.getNumVertices();
        int[][] edgesForDijkstra = graph.getAllEdgesForDijkstra();

        // Call your Dijkstra algorithm
        int[] shortestDistances = Dijkstra.dijkstra(numVertices, edgesForDijkstra, originIndex);

        int distance = shortestDistances[destinationIndex];
        if (distance == Integer.MAX_VALUE) {
            // System.out.println("[ROUTE] No direct or indirect route found from " + originCode + " to " + destinationCode); // Keep quiet for valid no-path scenario
        } else {
            // System.out.println("[ROUTE] Shortest route from " + originCode + " to " + destinationCode + " is " + distance + " units.");
        }
        return distance;
    }

    public DirectedSinglyLinkedListGraph getGraph() {
        return graph;
    }
}