package ucr.proyectoalgoritmos.graph; // Adjust package

import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.util.Utility; // For comparisons
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random; // For random graph generation

public class DirectedSinglyLinkedListGraph {
    private final Map<String, Integer> airportCodeToIndexMap; // Maps airport code (String) to vertex index (int)
    private String[] indexToAirportCodeArray; // Maps vertex index (int) back to airport code (String)
    private final ArrayList<SinglyLinkedList> adjList; // Adjacency list: list of SinglyLinkedLists, each containing edges
    private int numVertices; // Current number of vertices in the graph
    private int numEdges;    // New: Keep track of the total number of edges
    private final Random random;

    public DirectedSinglyLinkedListGraph() {
        this.airportCodeToIndexMap = new HashMap<>();
        this.indexToAirportCodeArray = new String[25]; // Initial capacity for vertex array
        this.adjList = new ArrayList<>(); // Initial capacity for adjacency list
        this.numVertices = 0;
        this.numEdges = 0; // Initialize edge count
        this.random = new Random();
    }

    /**
     * Adds a vertex (airport) to the graph. If the airport code already exists,
     * it returns the existing index. Otherwise, it adds a new vertex.
     * @param airportCode The unique code of the airport.
     * @return The integer index assigned to the airport.
     */
    public int addVertex(String airportCode) {
        if (airportCodeToIndexMap.containsKey(airportCode)) {
            return airportCodeToIndexMap.get(airportCode); // Airport already exists
        }

        // Expand array if needed for indexToAirportCodeArray
        if (numVertices >= indexToAirportCodeArray.length) {
            indexToAirportCodeArray = Arrays.copyOf(indexToAirportCodeArray, indexToAirportCodeArray.length * 2);
        }

        airportCodeToIndexMap.put(airportCode, numVertices); // Map code to new index
        indexToAirportCodeArray[numVertices] = airportCode; // Map index to code
        adjList.add(new SinglyLinkedList()); // Add a new empty adjacency list for this vertex

        System.out.println("DEBUG Graph: Added vertex: " + airportCode + " at index " + numVertices);
        return numVertices++; // Increment vertex count and return the new index
    }

    /**
     * Adds a directed route (edge) between two airports with a given weight.
     * Ensures both origin and destination airports exist as vertices.
     * Prevents self-loops and duplicate routes.
     * @param originCode The code of the origin airport.
     * @param destinationCode The code of the destination airport.
     * @param weight The weight (distance/cost) of the route.
     * @throws ListException If there's an issue with the underlying SinglyLinkedList.
     */
    public void addEdge(int u, int v, int weight) throws ListException {
        // This method is designed to be called by RouteManager after airport codes are converted to indices.
        // Thus, we assume u and v are valid indices.
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new ListException("Invalid vertex index for adding edge: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }
        if (u == v) {
            // System.out.println("[GRAPH] Skipping self-loop edge for vertex " + u);
            return; // Business rule: no flights to the same airport
        }

        SinglyLinkedList connections = adjList.get(u); // Get the adjacency list for the origin vertex 'u'

        // Check if route already exists before adding
        // Ensure connections is not null before iterating (though adjList.get(u) should ensure this)
        if (connections != null) {
            for (int i = 0; i < connections.size(); i++) {
                int[] existingEdge = (int[]) connections.get(i);
                if (existingEdge[0] == v) { // If the destination index matches
                    System.out.println("[GRAPH] Edge from index " + u + " to " + v + " already exists. Skipping add.");
                    return; // Route already exists
                }
            }
        }

        int[] edge = new int[]{v, weight}; // {destination_index, weight}
        connections.add(edge); // Add to the origin's adjacency list
        numEdges++; // Increment total edge count
        System.out.println("[GRAPH] Edge added: " + indexToAirportCodeArray[u] + " ("+u+") -> " + indexToAirportCodeArray[v] + " ("+v+") (Weight: " + weight + ")");
    }


    /**
     * Modifies the weight of an existing route (edge) between two airports.
     * @param originCode The code of the origin airport.
     * @param destinationCode The code of the destination airport.
     * @param newWeight The new weight (distance/cost) for the route.
     * @throws ListException If origin/destination airport not found or route not found.
     */
    public void modifyEdge(int u, int v, int newWeight) throws ListException {
        // This method assumes u and v are valid indices
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new ListException("Invalid vertex index for modifying edge: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }

        SinglyLinkedList connections = adjList.get(u);
        boolean found = false;
        if (connections != null) { // Defensive check
            for (int i = 0; i < connections.size(); i++) {
                int[] edge = (int[]) connections.get(i);
                if (edge[0] == v) { // If the destination index matches
                    edge[1] = newWeight; // Update the weight
                    found = true;
                    System.out.println("[GRAPH] Edge from index " + u + " ("+indexToAirportCodeArray[u]+") to " + v + " ("+indexToAirportCodeArray[v]+") modified to new weight: " + newWeight);
                    break;
                }
            }
        }
        if (!found) {
            throw new ListException("Edge from " + indexToAirportCodeArray[u] + " to " + indexToAirportCodeArray[v] + " not found to modify.");
        }
    }


    /**
     * Helper to get all edges in the format required by Dijkstra.dijkstra: {origin_idx, dest_idx, weight}.
     * @return A 2D array of integers representing all edges.
     * @throws ListException If there's an issue accessing internal lists.
     */
    public int[][] getAllEdgesForDijkstra() throws ListException {
        ArrayList<int[]> edgesList = new ArrayList<>();
        // Iterate through each vertex's adjacency list
        for (int u = 0; u < numVertices; u++) {
            SinglyLinkedList connections = adjList.get(u); // Get the SinglyLinkedList for vertex 'u'

            // CRITICAL CHECK for SinglyLinkedList consistency and emptiness
            if (connections != null && !connections.isEmpty()) {
                for (int i = 0; i < connections.size(); i++) {
                    int[] edge = (int[]) connections.get(i); // This should now be safe with the debugged SLL
                    edgesList.add(new int[]{u, edge[0], edge[1]}); // {origin_index, dest_index, weight}
                }
            } else if (connections != null && connections.size() > 0) {
                // This block should ideally never be hit if SinglyLinkedList is truly consistent
                System.err.println("CRITICAL INCONSISTENCY DETECTED in DirectedSinglyLinkedListGraph.getAllEdgesForDijkstra(): " +
                        "connections for vertex " + u + " is not empty but isEmpty() is true! Size: " + connections.size());
                // Potentially iterate unsafely or skip, depending on desired robustness.
                // For now, it will likely lead to a ListException from SLL.get(i) as designed.
            }
        }
        return edgesList.toArray(new int[0][]);
    }

    /**
     * Gets the total number of vertices (airports) in the graph.
     * @return The number of vertices.
     */
    public int getNumVertices() {
        return numVertices;
    }

    /**
     * Gets the total number of edges (routes) in the graph.
     * @return The number of edges.
     */
    public int getNumEdges() {
        return numEdges; // Return the maintained edge count
    }

    /**
     * Gets the numerical index for a given airport code.
     * @param airportCode The code of the airport.
     * @return The index, or -1 if the airport code is not found.
     */
    public int getIndexForAirportCode(String airportCode) {
        return airportCodeToIndexMap.getOrDefault(airportCode, -1);
    }

    /**
     * Gets the airport code for a given numerical index.
     * @param index The numerical index of the vertex.
     * @return The airport code, or null if the index is out of bounds.
     */
    public String getAirportCodeForIndex(int index) {
        if (index >= 0 && index < numVertices) {
            return indexToAirportCodeArray[index];
        }
        return null;
    }

    /**
     * Gets the number of outgoing routes from a specific airport.
     * @param airportCode The code of the airport.
     * @return The count of outgoing routes, or 0 if the airport is not found.
     */
    public int getOutgoingRouteCount(String airportCode) {
        int index = getIndexForAirportCode(airportCode);
        if (index == -1) {
            return 0; // Airport not in graph
        }
        SinglyLinkedList connections = adjList.get(index);
        return connections != null ? connections.size() : 0; // Defensive check for null
    }

    /**
     * Returns a SinglyLinkedList containing all airport codes present in the graph.
     * @return A SinglyLinkedList of airport codes (Strings).
     * @throws ListException If there's an issue with the SinglyLinkedList operations.
     */
    public SinglyLinkedList getAllAirportCodes() throws ListException {
        SinglyLinkedList codes = new SinglyLinkedList();
        // Iterate through the map's keys and add them to the SinglyLinkedList
        for (String code : airportCodeToIndexMap.keySet()) {
            codes.add(code);
        }
        return codes;
    }

    /**
     * Generates a specified number of random routes between existing airports.
     * This is a fallback or initial data generation method.
     * @param minRoutesPerAirport Minimum routes to generate per airport.
     * @param maxRoutesPerAirport Maximum routes to generate per airport.
     * @param minWeight Minimum weight for a route.
     * @param maxWeight Maximum weight for a route.
     * @throws ListException If there are not enough airports or an internal list error occurs.
     */
    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minWeight, int maxWeight) throws ListException {
        SinglyLinkedList allCodes = getAllAirportCodes(); // Use the existing method to get all airport codes

        if (allCodes.isEmpty() || allCodes.size() < 2) {
            System.err.println("[GRAPH] Not enough airports (" + allCodes.size() + ") to generate random routes.");
            return;
        }

        System.out.println("\n--- Generating Random Routes ---");
        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k); // Get origin airport code
            int originIndex = getIndexForAirportCode(originCode); // Get its index

            // Determine how many routes to generate for this origin
            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0; // To prevent infinite loops if few valid destinations
            final int MAX_ATTEMPTS_PER_ROUTE = 50; // Max attempts to find a unique, non-self-loop route

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                // Pick a random destination from the available airports
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size()));
                int destinationIndex = getIndexForAirportCode(destinationCode); // Get its index

                // Generate a random weight
                int weight = random.nextInt(maxWeight - minWeight + 1) + minWeight;

                // Check for self-loop and if route already exists
                if (originIndex != destinationIndex) { // No self-loops
                    try {
                        // Check if the edge already exists. If addEdge handles duplicates, this is fine.
                        // However, to be explicit, we can check here.
                        boolean exists = false;
                        SinglyLinkedList currentConnections = adjList.get(originIndex);
                        if (currentConnections != null) {
                            for (int i = 0; i < currentConnections.size(); i++) {
                                int[] existingEdge = (int[]) currentConnections.get(i);
                                if (existingEdge[0] == destinationIndex) {
                                    exists = true;
                                    break;
                                }
                            }
                        }

                        if (!exists) {
                            addEdge(originIndex, destinationIndex, weight); // Add the route using indices
                            generatedCount++;
                        } else {
                            // System.out.println("DEBUG: Route " + originCode + " -> " + destinationCode + " already exists. Trying another.");
                        }
                    } catch (ListException e) {
                        System.err.println("WARNING: Error adding random route (" + originCode + " -> " + destinationCode + "): " + e.getMessage());
                    }
                }
                attemptCount++;
            }
            if (generatedCount < routesToGenerate) {
                System.out.println("WARNING: Only generated " + generatedCount + " routes for " + originCode + " (intended " + routesToGenerate + ") due to attempts limit or no valid destinations.");
            }
        }
        System.out.println("--- Random Route Generation Complete. Total edges: " + numEdges + " ---");
    }

    /**
     * Gets the total number of edges (routes) in the graph.
     * This is a simple count of edges.
     * @return The total number of edges.
     */
    // This is a duplicate of getNumEdges(). Keeping one version for clarity.
    // public int getNumEdges() {
    //     return numEdges;
    // }

    // Utility for printing graph (optional)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Graph (Vertices: " + numVertices + ", Edges: " + numEdges + "):\n");
        for (int i = 0; i < numVertices; i++) {
            String originCode = indexToAirportCodeArray[i];
            sb.append("[").append(i).append("] ").append(originCode).append(" -> ");
            SinglyLinkedList connections = adjList.get(i);
            if (connections != null && !connections.isEmpty()) {
                try {
                    for (int j = 0; j < connections.size(); j++) {
                        int[] edge = (int[]) connections.get(j);
                        String destCode = indexToAirportCodeArray[edge[0]];
                        sb.append(destCode).append(" (").append(edge[1]).append(" units)").append(j < connections.size() - 1 ? ", " : "");
                    }
                } catch (ListException e) {
                    sb.append("ERROR: ").append(e.getMessage());
                }
            } else {
                sb.append("No outgoing routes.");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}