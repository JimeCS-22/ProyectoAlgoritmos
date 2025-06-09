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
    private final Map<String, Integer> airportCodeToIndexMap;
    private String[] indexToAirportCodeArray; // For reverse lookup
    private final ArrayList<SinglyLinkedList> adjList; // Adjacency list: list of lists of edges (int[] {dest_idx, weight})
    private int numVertices;
    private final Random random;

    public DirectedSinglyLinkedListGraph() {
        this.airportCodeToIndexMap = new HashMap<>();
        this.indexToAirportCodeArray = new String[25]; // Initial size for 15-20 airports
        this.adjList = new ArrayList<>();
        this.numVertices = 0;
        this.random = new Random();
    }

    // Add a vertex (airport) to the graph
    public int addVertex(String airportCode) {
        if (airportCodeToIndexMap.containsKey(airportCode)) {
            return airportCodeToIndexMap.get(airportCode);
        }
        // Expand array if needed
        if (numVertices >= indexToAirportCodeArray.length) {
            indexToAirportCodeArray = Arrays.copyOf(indexToAirportCodeArray, indexToAirportCodeArray.length * 2);
        }
        airportCodeToIndexMap.put(airportCode, numVertices);
        indexToAirportCodeArray[numVertices] = airportCode;
        adjList.add(new SinglyLinkedList()); // Add a new empty list for this vertex
        return numVertices++;
    }

    // a. Add route (with weight)
    public void addRoute(String originCode, String destinationCode, int weight) throws ListException {
        if (originCode.equalsIgnoreCase(destinationCode)) {
            // System.out.println("[GRAPH] Skipping self-loop route: " + originCode + " -> " + destinationCode);
            return; // Business rule: no flights to the same airport (implies no routes)
        }

        int u = addVertex(originCode); // Ensure both airports are registered as vertices
        int v = addVertex(destinationCode);

        // Check if route already exists before adding
        SinglyLinkedList connections = adjList.get(u);
        for (int i = 0; i < connections.size(); i++) {
            int[] existingEdge = (int[]) connections.get(i);
            if (existingEdge[0] == v) {
                System.out.println("[GRAPH] Route from " + originCode + " to " + destinationCode + " already exists. Skipping add.");
                return; // Route already exists
            }
        }

        int[] edge = new int[]{v, weight}; // {destination_index, weight}
        connections.add(edge); // Add to the origin's adjacency list
        System.out.println("[GRAPH] Route added: " + originCode + " ("+u+") -> " + destinationCode + " ("+v+") (Weight: " + weight + ")");
    }

    // b. Modify route (change distance)
    public void modifyRoute(String originCode, String destinationCode, int newWeight) throws ListException {
        int u = getIndexForAirportCode(originCode);
        int v = getIndexForAirportCode(destinationCode);

        if (u == -1 || v == -1) {
            throw new ListException("Origin or destination airport not found for route modification.");
        }

        SinglyLinkedList connections = adjList.get(u);
        boolean found = false;
        for (int i = 0; i < connections.size(); i++) {
            int[] edge = (int[]) connections.get(i);
            if (edge[0] == v) { // If the destination index matches
                edge[1] = newWeight; // Update the weight
                found = true;
                System.out.println("[GRAPH] Route " + originCode + " -> " + destinationCode + " modified to new weight: " + newWeight);
                break;
            }
        }
        if (!found) {
            throw new ListException("Route from " + originCode + " to " + destinationCode + " not found to modify.");
        }
    }

    // Helper to get all edges in the format required by Dijkstra.dijkstra
    public int[][] getAllEdgesForDijkstra() throws ListException {
        ArrayList<int[]> edgesList = new ArrayList<>();
        for (int u = 0; u < numVertices; u++) {
            SinglyLinkedList connections = adjList.get(u);
            for (int i = 0; i < connections.size(); i++) {
                int[] edge = (int[]) connections.get(i);
                edgesList.add(new int[]{u, edge[0], edge[1]}); // {origin_index, dest_index, weight}
            }
        }
        return edgesList.toArray(new int[0][]);
    }

    // Get total number of vertices (airports)
    public int getNumVertices() {
        return numVertices;
    }

    // Get index for an airport code
    public int getIndexForAirportCode(String airportCode) {
        return airportCodeToIndexMap.getOrDefault(airportCode, -1);
    }

    // Get airport code for an index
    public String getAirportCodeForIndex(int index) {
        if (index >= 0 && index < numVertices) {
            return indexToAirportCodeArray[index];
        }
        return null;
    }

    // Get number of outgoing routes for a given airport
    public int getOutgoingRouteCount(String airportCode) {
        int index = getIndexForAirportCode(airportCode);
        if (index == -1) {
            return 0;
        }
        try {
            return adjList.get(index).size();
        } catch (ListException e) {
            System.err.println("Error getting outgoing route count for " + airportCode + ": " + e.getMessage());
            return 0;
        }
    }

    public ArrayList<String> getAllAirportCodes() {
        return new ArrayList<>(airportCodeToIndexMap.keySet());
    }

    // Generate random routes for initial setup
    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minWeight, int maxWeight) throws ListException {
        ArrayList<String> allCodes = getAllAirportCodes();
        if (allCodes.size() < 2) {
            System.err.println("[GRAPH] Not enough airports to generate routes.");
            return;
        }

        System.out.println("\n--- Generating Random Routes ---");
        for (String originCode : allCodes) {
            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            while (generatedCount < routesToGenerate) {
                String destinationCode = allCodes.get(random.nextInt(allCodes.size()));
                int weight = random.nextInt(maxWeight - minWeight + 1) + minWeight;

                try {
                    addRoute(originCode, destinationCode, weight);
                    generatedCount++;
                } catch (ListException e) {
                    // Likely a duplicate route or self-loop. Try again.
                    // System.err.println("Skipping route generation due to: " + e.getMessage());
                }
            }
        }
        System.out.println("--- Random Route Generation Complete ---");
    }
}