package ucr.proyectoalgoritmos.Domain.dijkstra;

import org.junit.jupiter.api.BeforeAll; // For static setup of place mappings
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

class DijkstraTest {

    // A map to store the mapping from place names (String) to vertex indices (int)
    private static Map<String, Integer> placeToIndexMap;
    // A map to store the mapping from vertex indices (int) to place names (String)
    private static String[] indexToPlaceArray;
    private static int nextIndex; // To assign unique indices

    // A helper method to get or create an index for a place name
    private static int getIndexForPlace(String placeName) {
        return placeToIndexMap.computeIfAbsent(placeName, k -> {
            // Expand indexToPlaceArray if necessary
            if (nextIndex >= indexToPlaceArray.length) {
                indexToPlaceArray = Arrays.copyOf(indexToPlaceArray, indexToPlaceArray.length * 2);
            }
            indexToPlaceArray[nextIndex] = placeName;
            return nextIndex++;
        });
    }

    // This method runs once before all test methods to initialize mappings
    @BeforeAll
    static void setupMappings() {
        placeToIndexMap = new HashMap<>();
        indexToPlaceArray = new String[10]; // Initial capacity, will auto-expand
        nextIndex = 0;

        // Populate common places here if desired, or let them be created on demand
        // For example:
        // getIndexForPlace("Paraíso");
        // getIndexForPlace("Cartago");
        // getIndexForPlace("San Jose");
    }

    // Helper method to convert String-based edges to int-based edges
    private int[][] convertEdges(Object[][] stringEdges) {
        List<int[]> intEdgesList = new ArrayList<>();
        for (Object[] edge : stringEdges) {
            String uName = (String) edge[0];
            String vName = (String) edge[1];
            int weight = (int) edge[2];

            int u = getIndexForPlace(uName);
            int v = getIndexForPlace(vName);

            intEdgesList.add(new int[]{u, v, weight});
        }
        return intEdgesList.toArray(new int[0][]);
    }

    // Helper method to convert String-based expected distances to int-based
    private int[] convertExpectedDistances(Map<String, Integer> stringDistances, int numVertices) {
        int[] expected = new int[numVertices];
        Arrays.fill(expected, Integer.MAX_VALUE); // Initialize with infinity

        for (Map.Entry<String, Integer> entry : stringDistances.entrySet()) {
            String placeName = entry.getKey();
            Integer distance = entry.getValue();
            int index = getIndexForPlace(placeName); // Get the index for the place
            expected[index] = distance;
        }
        return expected;
    }

    @Test
    void testDijkstraBasicDirectedGraphPlaces() {
        // Define edges using place names
        Object[][] stringEdges = {
                {"Paraíso", "Cartago", 4},
                {"Paraíso", "Tres Ríos", 8},
                {"Cartago", "Orotina", 6},
                {"Tres Ríos", "Alajuela", 2},
                {"Alajuela", "Orotina", 10}
        };

        String sourcePlace = "Paraíso";

        // Convert string edges to integer edges and get total vertices
        int[][] intEdges = convertEdges(stringEdges);
        // The total number of vertices will be the number of unique places found
        int V = nextIndex; // 'nextIndex' holds the total count of unique places

        int srcIndex = getIndexForPlace(sourcePlace);

        // Define expected distances using place names
        Map<String, Integer> expectedStringDistances = new HashMap<>();
        expectedStringDistances.put("Paraíso", 0);
        expectedStringDistances.put("Cartago", 4);
        expectedStringDistances.put("Tres Ríos", 8);
        expectedStringDistances.put("Alajuela", 10); // Paraíso -> Tres Ríos (8) -> Alajuela (2)
        expectedStringDistances.put("Orotina", 10);  // Paraíso -> Cartago (4) -> Orotina (6)

        int[] expectedDistances = convertExpectedDistances(expectedStringDistances, V);

        int[] actualDistances = Dijkstra.dijkstra(V, intEdges, srcIndex);

        // --- Print statements for demonstration ---
        System.out.println("--- Test: Basic Directed Graph with Places ---");
        System.out.println("Source: " + sourcePlace);
        System.out.println("Vertex to Place Mapping:");
        for (int i = 0; i < V; i++) {
            System.out.println("  " + i + " -> " + indexToPlaceArray[i]);
        }
        System.out.println("Expected shortest distances:");
        printDistances(expectedDistances, V);
        System.out.println("Actual shortest distances:");
        printDistances(actualDistances, V);
        System.out.println("------------------------------------");
        // --- End of print statements ---

        assertArrayEquals(expectedDistances, actualDistances, "Shortest distances for basic directed graph with places");
    }

    @Test
    void testDijkstraUndirectedGraphPlaces() {
        Object[][] stringEdges = {
                {"A", "B", 1}, {"B", "A", 1},
                {"A", "C", 3}, {"C", "A", 3},
                {"B", "C", 1}, {"C", "B", 1},
                {"B", "D", 5}, {"D", "B", 5},
                {"C", "D", 1}, {"D", "C", 1}
        };
        String sourcePlace = "A";

        int[][] intEdges = convertEdges(stringEdges);
        int V = nextIndex;
        int srcIndex = getIndexForPlace(sourcePlace);

        Map<String, Integer> expectedStringDistances = new HashMap<>();
        expectedStringDistances.put("A", 0);
        expectedStringDistances.put("B", 1); // A->B (1)
        expectedStringDistances.put("C", 2); // A->B (1) -> C (1)
        expectedStringDistances.put("D", 3); // A->B (1) -> C (1) -> D (1)

        int[] expectedDistances = convertExpectedDistances(expectedStringDistances, V);
        int[] actualDistances = Dijkstra.dijkstra(V, intEdges, srcIndex);

        System.out.println("\n--- Test: Undirected Graph with Places ---");
        System.out.println("Source: " + sourcePlace);
        System.out.println("Vertex to Place Mapping:");
        for (int i = 0; i < V; i++) {
            System.out.println("  " + i + " -> " + indexToPlaceArray[i]);
        }
        System.out.println("Expected shortest distances:");
        printDistances(expectedDistances, V);
        System.out.println("Actual shortest distances:");
        printDistances(actualDistances, V);
        System.out.println("------------------------------------");

        assertArrayEquals(expectedDistances, actualDistances, "Shortest distances for undirected graph with places");
    }

    @Test
    void testDijkstraWithUnreachableNodesPlaces() {
        Object[][] stringEdges = {
                {"Start", "Middle", 1},
                {"Disconnected1", "Disconnected2", 1}
        };
        String sourcePlace = "Start";

        int[][] intEdges = convertEdges(stringEdges);
        int V = nextIndex;
        int srcIndex = getIndexForPlace(sourcePlace);

        Map<String, Integer> expectedStringDistances = new HashMap<>();
        expectedStringDistances.put("Start", 0);
        expectedStringDistances.put("Middle", 1);
        // Disconnected nodes will remain at MAX_VALUE, so we don't put them in the map
        // The convertExpectedDistances helper will fill them with MAX_VALUE by default.

        int[] expectedDistances = convertExpectedDistances(expectedStringDistances, V);

        int[] actualDistances = Dijkstra.dijkstra(V, intEdges, srcIndex);

        System.out.println("\n--- Test: Unreachable Nodes with Places ---");
        System.out.println("Source: " + sourcePlace);
        System.out.println("Vertex to Place Mapping:");
        for (int i = 0; i < V; i++) {
            System.out.println("  " + i + " -> " + indexToPlaceArray[i]);
        }
        System.out.println("Expected shortest distances:");
        printDistances(expectedDistances, V);
        System.out.println("Actual shortest distances:");
        printDistances(actualDistances, V);
        System.out.println("------------------------------------");

        assertArrayEquals(expectedDistances, actualDistances, "Shortest distances with unreachable nodes (places)");
    }

    @Test
    void testDijkstraSingleNodeGraphPlaces() {
        Object[][] stringEdges = {};
        String sourcePlace = "OnlyNode";

        int[][] intEdges = convertEdges(stringEdges);
        int V = nextIndex; // Should be 1 after processing "OnlyNode"
        int srcIndex = getIndexForPlace(sourcePlace);

        Map<String, Integer> expectedStringDistances = new HashMap<>();
        expectedStringDistances.put("OnlyNode", 0);

        int[] expectedDistances = convertExpectedDistances(expectedStringDistances, V);
        int[] actualDistances = Dijkstra.dijkstra(V, intEdges, srcIndex);

        System.out.println("\n--- Test: Single Node Graph with Places ---");
        System.out.println("Source: " + sourcePlace);
        System.out.println("Vertex to Place Mapping:");
        for (int i = 0; i < V; i++) {
            System.out.println("  " + i + " -> " + indexToPlaceArray[i]);
        }
        System.out.println("Expected shortest distances:");
        printDistances(expectedDistances, V);
        System.out.println("Actual shortest distances:");
        printDistances(actualDistances, V);
        System.out.println("------------------------------------");

        assertArrayEquals(expectedDistances, actualDistances, "Shortest distances for single node graph (places)");
    }

    @Test
    void testDijkstraLargerGraphPlaces() {
        Object[][] stringEdges = {
                {"Paraíso", "Cartago", 5}, {"Paraíso", "Tres Ríos", 2},
                {"Cartago", "Orotina", 4}, {"Cartago", "Tres Ríos", 1},
                {"Tres Ríos", "Cartago", 1}, {"Tres Ríos", "San Jose", 7}, {"Tres Ríos", "Alajuela", 6},
                {"Alajuela", "San Jose", 3}, {"Alajuela", "Heredia", 2},
                {"San Jose", "Heredia", 1},
                {"Heredia", "Limon", 8}
        };
        String sourcePlace = "Paraíso";

        int[][] intEdges = convertEdges(stringEdges);
        int V = nextIndex;
        int srcIndex = getIndexForPlace(sourcePlace);

        Map<String, Integer> expectedStringDistances = new HashMap<>();
        expectedStringDistances.put("Paraíso", 0);
        expectedStringDistances.put("Cartago", 3); // Paraíso(0) -> Tres Ríos(2) -> Cartago(1) = 3
        expectedStringDistances.put("Tres Ríos", 2); // Paraíso(0) -> Tres Ríos(2) = 2
        expectedStringDistances.put("Alajuela", 8); // Paraíso(0) -> Tres Ríos(2) -> Alajuela(6) = 8
        expectedStringDistances.put("San Jose", 9); // Paraíso(0) -> Tres Ríos(2) -> Alajuela(8) -> San Jose(3) = 11, OR Paraíso(0) -> Tres Ríos(2) -> San Jose(7) = 9
        expectedStringDistances.put("Orotina", 7); // Paraíso(0) -> Tres Ríos(2) -> Cartago(3) -> Orotina(4) = 7
        expectedStringDistances.put("Heredia", 10); // Paraíso(0) -> Tres Ríos(2) -> San Jose(9) -> Heredia(1) = 10
        expectedStringDistances.put("Limon", 18); // Paraíso(0) -> Tres Ríos(2) -> San Jose(9) -> Heredia(10) -> Limon(8) = 18

        int[] expectedDistances = convertExpectedDistances(expectedStringDistances, V);
        int[] actualDistances = Dijkstra.dijkstra(V, intEdges, srcIndex);

        System.out.println("\n--- Test: Larger Graph with Places ---");
        System.out.println("Source: " + sourcePlace);
        System.out.println("Vertex to Place Mapping:");
        for (int i = 0; i < V; i++) {
            System.out.println("  " + i + " -> " + indexToPlaceArray[i]);
        }
        System.out.println("Expected shortest distances:");
        printDistances(expectedDistances, V);
        System.out.println("Actual shortest distances:");
        printDistances(actualDistances, V);
        System.out.println("------------------------------------");

        assertArrayEquals(expectedDistances, actualDistances, "Shortest distances for a larger graph (places)");
    }


    // Helper method to print distances with place names
    private void printDistances(int[] distances, int V) {
        System.out.print("[");
        for (int i = 0; i < V; i++) {
            System.out.print(indexToPlaceArray[i] + ": ");
            if (distances[i] == Integer.MAX_VALUE) {
                System.out.print("INF");
            } else {
                System.out.print(distances[i]);
            }
            if (i < V - 1) {
                System.out.print(", ");
            }
        }
        System.out.println("]");
    }
}