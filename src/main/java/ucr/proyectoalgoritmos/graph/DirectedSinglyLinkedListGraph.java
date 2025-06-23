package ucr.proyectoalgoritmos.graph;

import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // Asumo esta es la SinglyLinkedList sin genéricos
import ucr.proyectoalgoritmos.Domain.list.ListException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Random;

/**
 * Representa un grafo dirigido utilizando una lista de adyacencia
 * implementada con {@link SinglyLinkedList}.
 * <p>
 * **Esta clase NO DEBE ser modificada internamente en sus métodos existentes.**
 * **Se asume que SinglyLinkedList NO tiene parámetros de tipo (es decir, usa Object).**
 * </p>
 */
public class DirectedSinglyLinkedListGraph {
    private final Map<String, Integer> airportCodeToIndexMap;
    private String[] indexToAirportCodeArray;

    // ESTRUCTURA PRINCIPAL:
    // Aquí, SinglyLinkedList no tiene tipo de parámetro, así que almacena Object.
    // Los elementos serán int[] que deberán ser casteados.
    private final ArrayList<SinglyLinkedList> adjList; // Declaración sin parámetros de tipo para SinglyLinkedList

    private int numVertices;
    private int numEdges;
    private final Random random;

    public DirectedSinglyLinkedListGraph() {
        this.airportCodeToIndexMap = new HashMap<>();
        this.indexToAirportCodeArray = new String[25]; // Tamaño inicial, se duplica si es necesario
        this.adjList = new ArrayList<>(); // Lista de SinglyLinkedList (sin genéricos)
        this.numVertices = 0;
        this.numEdges = 0;
        this.random = new Random();
    }

    /**
     * Añade un nuevo vértice al grafo si no existe ya.
     * Retorna el índice asignado o el existente.
     */
    public int addVertex(String airportCode) {
        if (airportCodeToIndexMap.containsKey(airportCode)) {
            return airportCodeToIndexMap.get(airportCode);
        }
        if (numVertices >= indexToAirportCodeArray.length) {
            indexToAirportCodeArray = Arrays.copyOf(indexToAirportCodeArray, indexToAirportCodeArray.length * 2);
        }
        airportCodeToIndexMap.put(airportCode, numVertices);
        indexToAirportCodeArray[numVertices] = airportCode;
        adjList.add(new SinglyLinkedList()); // Añadimos una SinglyLinkedList sin tipo
        return numVertices++;
    }

    /**
     * Añade una arista dirigida al grafo con un peso simple (int).
     * Este método EXISTE y NO DEBE ser modificado INTERNAMENTE.
     */
    public void addEdge(int u, int v, int weight) throws ListException {
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para añadir arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }
        if (u == v) {
            return;
        }

        SinglyLinkedList connections = adjList.get(u); // Obtenemos SinglyLinkedList (Object)
        boolean found = false;
        for (int i = 0; i < connections.size(); i++) {
            int[] edge = (int[]) connections.get(i); // <-- CAST EXPLÍCITO aquí
            if (edge[0] == v) {
                edge[1] = weight;
                found = true;
                break;
            }
        }
        if (!found) {
            connections.add(new int[]{v, weight});
            numEdges++;
        }
    }

    /**
     * Modifica el peso de una arista existente.
     * Este método EXISTE y NO DEBE ser modificado INTERNAMENTE.
     */
    public boolean modifyEdge(int u, int v, int newWeight) throws ListException {
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para modificar arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }

        SinglyLinkedList connections = adjList.get(u); // Obtenemos SinglyLinkedList (Object)
        boolean found = false;
        for (int i = 0; i < connections.size(); i++) {
            int[] edge = (int[]) connections.get(i); // <-- CAST EXPLÍCITO aquí
            if (edge[0] == v) {
                edge[1] = newWeight;
                found = true;
                break;
            }
        }
        if (!found) {
            return false;
        }
        return true;
    }


    /**
     * Calcula la ruta más corta (basada en el peso 'int' de la arista)
     * utilizando el algoritmo de Dijkstra.
     * Este método EXISTE y NO DEBE ser modificado INTERNAMENTE.
     */
    public int shortestPath(String startAirportCode, String endAirportCode) throws ListException {
        int startIndex = getIndexForAirportCode(startAirportCode);
        int endIndex = getIndexForAirportCode(endAirportCode);

        if (startIndex == -1 || endIndex == -1 || numVertices == 0) {
            return Integer.MAX_VALUE;
        }
        if (startIndex == endIndex) {
            return 0;
        }

        int[] distances = new int[numVertices];
        Arrays.fill(distances, Integer.MAX_VALUE);
        boolean[] visited = new boolean[numVertices];
        Arrays.fill(visited, false);

        // ¡Cambiado a DijkstraNode!
        PriorityQueue<DijkstraNode> pq = new PriorityQueue<>(numVertices, Comparator.comparingInt(DijkstraNode::getDistance));

        distances[startIndex] = 0;
        pq.add(new DijkstraNode(startIndex, 0));

        while (!pq.isEmpty()) {
            int u = pq.poll().getVertex();

            if (visited[u]) {
                continue;
            }
            visited[u] = true;

            if (u == endIndex) {
                return distances[endIndex];
            }

            SinglyLinkedList neighbors = adjList.get(u); // Obtenemos SinglyLinkedList (Object)
            if (neighbors != null) {
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edge = (int[]) neighbors.get(i); // <-- CAST EXPLÍCITO aquí
                    int v = edge[0];
                    int weight = edge[1];

                    if (!visited[v] && distances[u] != Integer.MAX_VALUE && (long)distances[u] + weight < distances[v]) {
                        distances[v] = distances[u] + weight;
                        pq.add(new DijkstraNode(v, distances[v])); // ¡Cambiado a DijkstraNode!
                    }
                }
            }
        }
        return distances[endIndex];
    }

    /**
     * Verifica si existe una arista directa entre dos aeropuertos dados sus códigos.
     * Este método EXISTE y NO DEBE ser modificado INTERNAMENTE.
     */
    public boolean hasEdge(String uAirportCode, String vAirportCode) throws ListException {
        int u = getIndexForAirportCode(uAirportCode);
        int v = getIndexForAirportCode(vAirportCode);
        if (u == -1 || v == -1) {
            return false;
        }
        SinglyLinkedList connections = adjList.get(u); // Obtenemos SinglyLinkedList (Object)
        if (connections != null) {
            for (int i = 0; i < connections.size(); i++) {
                int[] edge = (int[]) connections.get(i); // <-- CAST EXPLÍCITO aquí
                if (edge[0] == v) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Genera rutas aleatorias con un solo peso 'int'.
     * Este método EXISTE y NO DEBE ser modificado INTERNAMENTE.
     */
    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minWeight, int maxWeight) throws ListException {
        SinglyLinkedList allCodes = getAllAirportCodes(); // getAllAirportCodes ya devuelve SinglyLinkedList sin genéricos
        if (allCodes.isEmpty() || allCodes.size() < 2) {
            System.out.println("ADVERTENCIA: No hay suficientes aeropuertos cargados para generar rutas aleatorias.");
            return;
        }

        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k); // <-- CAST EXPLÍCITO aquí
            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0;
            final int MAX_ATTEMPTS_PER_ROUTE = 50;

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size())); // <-- CAST EXPLÍCITO aquí
                if (!originCode.equals(destinationCode)) {
                    int weight = random.nextInt(maxWeight - minWeight + 1) + minWeight;
                    try {
                        addEdge(getIndexForAirportCode(originCode), getIndexForAirportCode(destinationCode), weight);
                        generatedCount++;
                    } catch (IllegalArgumentException | ListException e) {
                        // Puede que la arista ya exista o haya algún otro error
                    }
                }
                attemptCount++;
            }
        }
    }

    // Clase interna para la PriorityQueue (DijkstraNode - Renombrada)
    private static class DijkstraNode { // <-- RENOMBRADO de Node a DijkstraNode
        private final int vertex;
        private final int distance;

        public DijkstraNode(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
        public int getVertex() { return vertex; }
        public int getDistance() { return distance; }
    }

    // --- MÉTODOS AUXILIARES EXISTENTES ---
    public int getNumVertices() { return numVertices; }
    public int getNumEdges() { return numEdges; }
    public int getIndexForAirportCode(String airportCode) { return airportCodeToIndexMap.getOrDefault(airportCode, -1); }
    public String getAirportCodeForIndex(int index) {
        if (index >= 0 && index < numVertices) {
            return indexToAirportCodeArray[index];
        }
        return null;
    }
    public int getOutgoingRouteCount(String airportCode) {
        int index = getIndexForAirportCode(airportCode);
        if (index == -1) { return 0; }
        SinglyLinkedList connections = adjList.get(index); // Obtenemos SinglyLinkedList (Object)
        return connections != null ? connections.size() : 0;
    }
    public SinglyLinkedList getAllAirportCodes() throws ListException {
        SinglyLinkedList codes = new SinglyLinkedList(); // Crea una SinglyLinkedList sin genéricos
        for (int i = 0; i < numVertices; i++) {
            if (indexToAirportCodeArray[i] != null) {
                codes.add(indexToAirportCodeArray[i]);
            }
        }
        return codes;
    }
    public String[] getVertices() {
        String[] vertices = new String[numVertices];
        System.arraycopy(indexToAirportCodeArray, 0, vertices, 0, numVertices);
        return vertices;
    }

    // ¡NUEVO GETTER AÑADIDO!
    public ArrayList<SinglyLinkedList> getAdjList() { // <-- Retorna ArrayList de SinglyLinkedList sin genéricos
        return adjList;
    }

    @Override
    public String toString() {
        String result = "Grafo Dirigido (Vértices: " + numVertices + ", Aristas: " + numEdges + "):\n";
        for (int i = 0; i < numVertices; i++) {
            String originCode = indexToAirportCodeArray[i];
            result += "[" + i + "] " + originCode + " -> ";
            SinglyLinkedList connections = adjList.get(i); // Obtenemos SinglyLinkedList (Object)
            if (connections != null && !connections.isEmpty()) {
                try {
                    boolean first = true;
                    for (int j = 0; j < connections.size(); j++) {
                        int[] edgeArray = (int[]) connections.get(j); // <-- CAST EXPLÍCITO aquí
                        int destIndex = edgeArray[0];
                        String destCode = indexToAirportCodeArray[destIndex];
                        if (!first) {
                            result += ", ";
                        }
                        result += destCode + " (Peso int: " + edgeArray[1] + ")";
                        first = false;
                    }
                } catch (ListException e) {
                    result += "ERROR: " + e.getMessage();
                }
            } else {
                result += "No hay rutas salientes.";
            }
            result += "\n";
        }
        return result;
    }

    public boolean containsVertex(String airportCode) {
        return getIndexForAirportCode(airportCode) != -1;
    }
}