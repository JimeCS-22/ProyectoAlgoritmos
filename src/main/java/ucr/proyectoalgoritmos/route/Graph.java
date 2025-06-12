package ucr.proyectoalgoritmos.route;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.stack.Stack; // If you use Stack for algorithms

import java.util.*;

public class Graph {
    private Map<String, Integer> verticesMap; // Maps airport code to an integer index
    private SinglyLinkedList[] adjacencyList; // Adjacency list representation
    private int numVertices;
    private int maxVertices;

    public Graph(int maxVertices) {
        this.maxVertices = maxVertices;
        this.verticesMap = new HashMap<>();
        this.adjacencyList = new SinglyLinkedList[maxVertices];
        for (int i = 0; i < maxVertices; i++) {
            adjacencyList[i] = new SinglyLinkedList();
        }
        this.numVertices = 0;
    }

    public void addVertex(String airportCode) throws ListException {
        if (!verticesMap.containsKey(airportCode)) {
            if (numVertices >= maxVertices) {
                System.err.println("ADVERTENCIA: Límite de vértices alcanzado en el grafo. No se puede añadir " + airportCode);
                return;
            }
            verticesMap.put(airportCode, numVertices);
            numVertices++;
        }
    }

    public boolean containsVertex(String airportCode) {
        return verticesMap.containsKey(airportCode);
    }

    public void addEdge(String originCode, String destinationCode, int weight) throws ListException {
        if (!verticesMap.containsKey(originCode) || !verticesMap.containsKey(destinationCode)) {
            // System.err.println("ERROR: No se pueden añadir aristas. Uno o ambos vértices no existen: " + originCode + ", " + destinationCode);
            return;
        }
        int u = verticesMap.get(originCode);
        int v = verticesMap.get(destinationCode);

        // Check if edge already exists to prevent duplicates (optional, depending on your list's add behavior)
        boolean edgeExists = false;
        for (int i = 0; i < adjacencyList[u].size(); i++) {
            Edge existingEdge = (Edge) adjacencyList[u].get(i);
            if (existingEdge.getDestinationIndex() == v) {
                edgeExists = true;
                break;
            }
        }
        if (!edgeExists) {
            adjacencyList[u].add(new Edge(v, weight));
            // For undirected graph, add reverse edge as well
            // adjacencyList[v].add(new Edge(u, weight));
        }
    }

    public int getNumVertices() {
        return numVertices;
    }

    public SinglyLinkedList getAllAirportCodes() throws ListException {
        SinglyLinkedList codes = new SinglyLinkedList();
        for (String code : verticesMap.keySet()) {
            codes.add(code);
        }
        return codes;
    }

    // --- Shortest Path (Dijkstra-like conceptualization) ---
    // This is a placeholder. A real implementation would be more complex.
    public int shortestPath(String startCode, String endCode) {
        if (!verticesMap.containsKey(startCode) || !verticesMap.containsKey(endCode)) {
            return Integer.MAX_VALUE; // No path if vertices don't exist
        }

        int startIndex = verticesMap.get(startCode);
        int endIndex = verticesMap.get(endCode);


        try {
            if (hasPathDFS(startIndex, endIndex, new boolean[numVertices])) {
                // Simulate a plausible duration based on existence, not actual shortest path calculation
                return 60 + new Random().nextInt(300); // 1 to 5 hours
            }
        } catch (ListException e) {
            System.err.println("ERROR Grafo: " + e.getMessage());
        }

        return Integer.MAX_VALUE; // No path found
    }

    // Simple DFS to check if a path exists
    private boolean hasPathDFS(int current, int target, boolean[] visited) throws ListException {
        visited[current] = true;
        if (current == target) {
            return true;
        }

        SinglyLinkedList neighbors = adjacencyList[current];
        for (int i = 0; i < neighbors.size(); i++) {
            Edge edge = (Edge) neighbors.get(i);
            int neighborIndex = edge.getDestinationIndex();
            if (!visited[neighborIndex]) {
                if (hasPathDFS(neighborIndex, target, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Helper for generating random routes
    public void generateRandomRoutes(int numRoutes, int minDuration, int maxDuration, int maxAttempts) throws ListException {
        Random rand = new Random();
        List<String> codes = new ArrayList<>(verticesMap.keySet());
        if (codes.size() < 2) return;

        int routesAdded = 0;
        int attempts = 0;

        while (routesAdded < numRoutes && attempts < maxAttempts) {
            String origin = codes.get(rand.nextInt(codes.size()));
            String destination = codes.get(rand.nextInt(codes.size()));

            if (!origin.equals(destination)) {
                int duration = minDuration + rand.nextInt(maxDuration - minDuration + 1);
                try {
                    addEdge(origin, destination, duration);
                    addEdge(destination, origin, duration); // For undirected graph
                    routesAdded++;
                } catch (ListException e) {
                    // This can happen if the edge already exists
                }
            }
            attempts++;
        }
        //System.out.println("DEBUG Grafo: " + routesAdded + " rutas aleatorias añadidas.");
    }

    // Get outgoing route count for sorting airports
    public int getOutgoingRouteCount(String airportCode) {
        if (!verticesMap.containsKey(airportCode)) {
            return 0;
        }
        int index = verticesMap.get(airportCode);
        return adjacencyList[index].size();
    }

    // Edge class for adjacency list
    private static class Edge {
        private int destinationIndex;
        private int weight; // e.g., duration

        public Edge(int destinationIndex, int weight) {
            this.destinationIndex = destinationIndex;
            this.weight = weight;
        }

        public int getDestinationIndex() { return destinationIndex; }
        public int getWeight() { return weight; }
    }
}