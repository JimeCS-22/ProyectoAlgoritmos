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


public class DirectedSinglyLinkedListGraph {
    private final Map<String, Integer> airportCodeToIndexMap;
    private String[] indexToAirportCodeArray;

    private final ArrayList<SinglyLinkedList> adjList;

    private int numVertices;
    private int numEdges;
    private final Random random;

    public DirectedSinglyLinkedListGraph() {
        this.airportCodeToIndexMap = new HashMap<>();
        this.indexToAirportCodeArray = new String[25];
        this.adjList = new ArrayList<>();
        this.numVertices = 0;
        this.numEdges = 0;
        this.random = new Random();
    }


    public int addVertex(String airportCode) {
        if (airportCodeToIndexMap.containsKey(airportCode)) {
            return airportCodeToIndexMap.get(airportCode);
        }
        if (numVertices >= indexToAirportCodeArray.length) {
            indexToAirportCodeArray = Arrays.copyOf(indexToAirportCodeArray, indexToAirportCodeArray.length * 2);
        }
        airportCodeToIndexMap.put(airportCode, numVertices);
        indexToAirportCodeArray[numVertices] = airportCode;
        adjList.add(new SinglyLinkedList());
        return numVertices++;
    }


    public void addEdge(int u, int v, int weight) throws ListException {
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para añadir arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }
        if (u == v) {
            return;
        }

        SinglyLinkedList connections = adjList.get(u);
        boolean found = false;
        for (int i = 0; i < connections.size(); i++) {
            int[] edge = (int[]) connections.get(i);
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

    public boolean modifyEdge(int u, int v, int newWeight) throws ListException {
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para modificar arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }

        SinglyLinkedList connections = adjList.get(u);
        boolean found = false;
        for (int i = 0; i < connections.size(); i++) {
            int[] edge = (int[]) connections.get(i);
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

        PriorityQueue<DijkstraNode> pq = new PriorityQueue<>(numVertices, Comparator.comparingInt(DijkstraNode::getDistance));

        distances[startIndex] = 0;
        pq.add(new DijkstraNode(startIndex, 0));



        while (!pq.isEmpty()) {
            DijkstraNode currentNode = pq.poll();
            int u = currentNode.getVertex();
            int dist_u = currentNode.getDistance();

            if (visited[u]) {
                System.out.println("DEBUG Dijkstra: Vértice " + getAirportCodeForIndex(u) + " ya visitado. Continuando.");
                continue;
            }
            visited[u] = true;

            if (u == endIndex) {
                System.out.println("DEBUG Dijkstra: ¡Vértice final " + getAirportCodeForIndex(endIndex) + " alcanzado! Distancia total: " + distances[endIndex]);
                return distances[endIndex];
            }

            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) {
                System.out.println("DEBUG Dijkstra: Procesando vecinos de " + getAirportCodeForIndex(u) + ":");
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edge = (int[]) neighbors.get(i);
                    int v = edge[0]; // Vértice destino
                    int weight = edge[1]; // Peso de la arista

                    System.out.println("  DEBUG Dijkstra: Vecino " + getAirportCodeForIndex(v) + " (peso " + weight + ")");

                    if (!visited[v] && distances[u] != Integer.MAX_VALUE) {

                        if ((long)distances[u] + weight < distances[v]) {
                            distances[v] = distances[u] + weight;
                            pq.add(new DijkstraNode(v, distances[v]));
                            System.out.println("  DEBUG Dijkstra: Actualizando dist(" + getAirportCodeForIndex(v) + ") a " + distances[v] + ". Añadiendo a PQ.");
                        } else {
                            System.out.println("    DEBUG Dijkstra: No se actualiza dist(" + getAirportCodeForIndex(v) + "). Nueva dist " + ((long)distances[u] + weight) + " no es menor que actual " + distances[v] + ".");
                        }
                    } else {
                        System.out.println("    DEBUG Dijkstra: Vecino " + getAirportCodeForIndex(v) + " ya visitado o distancia a " + getAirportCodeForIndex(u) + " es infinita.");
                    }
                }
            }
        }
        System.out.println("DEBUG Dijkstra: PQ vacía. Vértice final " + getAirportCodeForIndex(endIndex) + " no alcanzado. Distancia final: " + distances[endIndex]);
        return distances[endIndex];
    }

    public boolean hasEdge(String uAirportCode, String vAirportCode) throws ListException {
        int u = getIndexForAirportCode(uAirportCode);
        int v = getIndexForAirportCode(vAirportCode);
        if (u == -1 || v == -1) {
            return false;
        }
        SinglyLinkedList connections = adjList.get(u);
        if (connections != null) {
            for (int i = 0; i < connections.size(); i++) {
                int[] edge = (int[]) connections.get(i);
                if (edge[0] == v) {
                    return true;
                }
            }
        }
        return false;
    }

    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minWeight, int maxWeight) throws ListException {
        SinglyLinkedList allCodes = getAllAirportCodes();
        if (allCodes.isEmpty() || allCodes.size() < 2) {
            System.out.println("ADVERTENCIA: No hay suficientes aeropuertos cargados para generar rutas aleatorias.");
            return;
        }

        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k);
            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0;
            final int MAX_ATTEMPTS_PER_ROUTE = 50;

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size()));
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


    private static class DijkstraNode {
        private final int vertex;
        private final int distance;

        public DijkstraNode(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
        public int getVertex() { return vertex; }
        public int getDistance() { return distance; }
    }

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
        SinglyLinkedList connections = adjList.get(index);
        return connections != null ? connections.size() : 0;
    }
    public SinglyLinkedList getAllAirportCodes() throws ListException {
        SinglyLinkedList codes = new SinglyLinkedList();
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
    public ArrayList<SinglyLinkedList> getAdjList() {
        return adjList;
    }

    @Override
    public String toString() {
        String result = "Grafo Dirigido (Vértices: " + numVertices + ", Aristas: " + numEdges + "):\n";
        for (int i = 0; i < numVertices; i++) {
            String originCode = indexToAirportCodeArray[i];
            result += "[" + i + "] " + originCode + " -> ";
            SinglyLinkedList connections = adjList.get(i);
            if (connections != null && !connections.isEmpty()) {
                try {
                    boolean first = true;
                    for (int j = 0; j < connections.size(); j++) {
                        int[] edgeArray = (int[]) connections.get(j);
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