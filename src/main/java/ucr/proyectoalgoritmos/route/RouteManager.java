package ucr.proyectoalgoritmos.route; // Adjust package

import com.google.gson.Gson; // Import for JSON parsing
import com.google.gson.reflect.TypeToken; // Import for generic type handling with Gson
import ucr.proyectoalgoritmos.Domain.aeropuetos.AirportManager; // Import AirportManager
import ucr.proyectoalgoritmos.Domain.dijkstra.Dijkstra;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import java.io.FileReader; // For reading the JSON file
import java.io.IOException; // For file I/O exceptions
import java.lang.reflect.Type; // For Gson's TypeToken
import java.util.List; // For List<Route>

public class RouteManager {
    private DirectedSinglyLinkedListGraph graph;
    private AirportManager airportManager; // New: Reference to AirportManager

    // Modified Constructor: Now accepts AirportManager
    public RouteManager(AirportManager airportManager) {
        this.graph = new DirectedSinglyLinkedListGraph();
        this.airportManager = airportManager; // Initialize the AirportManager reference
    }

    // New Method: Load routes from a JSON file
    public void loadRoutesFromJson(String filePath) throws IOException, ListException {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(filePath)) {
            Type routeListType = new TypeToken<List<Route>>() {}.getType();
            List<Route> routes = gson.fromJson(reader, routeListType);

            if (routes != null) {
                System.out.println("DEBUG RouteManager: Successfully read " + routes.size() + " routes from " + filePath);
                for (Route route : routes) {
                    // Get the numerical index for origin and destination airport codes
                    // This relies on AirportManager having mapped codes to indices when it loaded airports
                    int originIndex = airportManager.getAirportIndex(route.getOriginAirportCode());
                    int destinationIndex = airportManager.getAirportIndex(route.getDestinationAirportCode());

                    // Only add the route if both airports are found and valid
                    if (originIndex != -1 && destinationIndex != -1) {
                        // Delegates to the graph's addEdge method
                        graph.addEdge(originIndex, destinationIndex, route.getDistance());
                        // System.out.println("DEBUG RouteManager: Added edge: " + route.getOriginAirportCode() + " -> " + route.getDestinationAirportCode() + " (Weight: " + route.getDistance() + ")");
                    } else {
                        System.err.println("WARNING RouteManager: Skipping route " + route.getOriginAirportCode() + " -> " + route.getDestinationAirportCode() +
                                ". One or both airports not found in AirportManager's records or graph.");
                    }
                }
                System.out.println("DEBUG RouteManager: Finished loading routes. Total edges in graph: " + graph.getNumEdges());
            } else {
                System.out.println("DEBUG RouteManager: No routes found in the JSON file: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("ERROR RouteManager: Failed to read routes file '" + filePath + "': " + e.getMessage());
            throw e; // Re-throw to be handled by FlightSimulator's fallback
        } catch (Exception e) { // Catch any other unexpected parsing or data issues
            System.err.println("ERROR RouteManager: An unexpected error occurred while processing routes from '" + filePath + "': " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Error processing routes from JSON", e); // Wrap and re-throw
        }
    }


    // a. Add route (delegates to graph) - This method should ideally use airport codes too
    public void addRoute(String originCode, String destinationCode, int weight) throws ListException {
        int originIndex = airportManager.getAirportIndex(originCode);
        int destinationIndex = airportManager.getAirportIndex(destinationCode);

        if (originIndex == -1 || destinationIndex == -1) {
            throw new ListException("Cannot add route. Origin or destination airport code not found in AirportManager.");
        }
        graph.addEdge(originIndex, destinationIndex, weight); // Use addEdge directly
        System.out.println("DEBUG RouteManager: Manually added route " + originCode + " -> " + destinationCode + " with weight " + weight);
    }

    // b. Modify route (delegates to graph) - This method should ideally use airport codes too
    public void modifyRoute(String originCode, String destinationCode, int newWeight) throws ListException {
        int originIndex = airportManager.getAirportIndex(originCode);
        int destinationIndex = airportManager.getAirportIndex(destinationCode);

        if (originIndex == -1 || destinationIndex == -1) {
            throw new ListException("Cannot modify route. Origin or destination airport code not found in AirportManager.");
        }
        graph.modifyEdge(originIndex, destinationIndex, newWeight); // Assuming graph has a modifyEdge
        System.out.println("DEBUG RouteManager: Modified route " + originCode + " -> " + destinationCode + " to new weight " + newWeight);
    }

    // c. Calculate the shortest route between two airports using Dijkstra
    public int calculateShortestRoute(String originCode, String destinationCode) throws ListException {
        int originIndex = graph.getIndexForAirportCode(originCode); // This method should be in AirportManager
        int destinationIndex = graph.getIndexForAirportCode(destinationCode); // This method should be in AirportManager

        // Correct way to get indices: use airportManager
        originIndex = airportManager.getAirportIndex(originCode);
        destinationIndex = airportManager.getAirportIndex(destinationCode);


        if (originIndex == -1) {
            throw new ListException("Origin airport " + originCode + " not found in system or graph.");
        }
        if (destinationIndex == -1) {
            throw new ListException("Destination airport " + destinationCode + " not found in system or graph.");
        }
        if (originIndex == destinationIndex) {
            return 0; // Business Rule: No flights to the same airport implies 0 duration
        }

        int numVertices = graph.getNumVertices();
        int[][] edgesForDijkstra = graph.getAllEdgesForDijkstra();

        // Handle case where graph is empty (no edges loaded)
        if (edgesForDijkstra == null || edgesForDijkstra.length == 0) {
            System.out.println("[ROUTE] No routes (edges) available in the graph. Cannot calculate shortest path.");
            return Integer.MAX_VALUE; // No path available
        }

        // Call your Dijkstra algorithm
        int[] shortestDistances = Dijkstra.dijkstra(numVertices, edgesForDijkstra, originIndex);

        int distance = shortestDistances[destinationIndex];
        if (distance == Integer.MAX_VALUE) {
            System.out.println("[ROUTE] No path found from " + originCode + " to " + destinationCode);
        } else {
            System.out.println("[ROUTE] Shortest path from " + originCode + " to " + destinationCode + " is " + distance + " units.");
        }
        return distance;
    }

    public DirectedSinglyLinkedListGraph getGraph() {
        return graph;
    }
}