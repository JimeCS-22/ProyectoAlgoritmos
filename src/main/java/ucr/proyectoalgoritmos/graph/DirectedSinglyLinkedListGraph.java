package ucr.proyectoalgoritmos.graph;

import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
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
    private final ArrayList<SinglyLinkedList> adjList; // Lista de adyacencia
    private int numVertices;
    private int numEdges;
    private final Random random;

    public DirectedSinglyLinkedListGraph() {
        this.airportCodeToIndexMap = new HashMap<>();
        // Asumiendo un tamaño inicial razonable, se redimensiona dinámicamente
        this.indexToAirportCodeArray = new String[25];
        this.adjList = new ArrayList<>();
        this.numVertices = 0;
        this.numEdges = 0;
        this.random = new Random();
    }

    /**
     * Añade un vértice (código de aeropuerto) al grafo. Si ya existe, retorna su índice.
     * @param airportCode El código único del aeropuerto.
     * @return El índice del vértice añadido o existente.
     */
    public int addVertex(String airportCode) {
        if (airportCodeToIndexMap.containsKey(airportCode)) {
            return airportCodeToIndexMap.get(airportCode);
        }

        // Redimensionar el array si es necesario
        if (numVertices >= indexToAirportCodeArray.length) {
            indexToAirportCodeArray = Arrays.copyOf(indexToAirportCodeArray, indexToAirportCodeArray.length * 2);
        }

        airportCodeToIndexMap.put(airportCode, numVertices);
        indexToAirportCodeArray[numVertices] = airportCode;
        adjList.add(new SinglyLinkedList()); // Añadir una nueva lista de adyacencia para el nuevo vértice

        return numVertices++;
    }

    /**
     * Verifica si un vértice (aeropuerto) existe en el grafo.
     * @param airportCode El código del aeropuerto a buscar.
     * @return true si el aeropuerto existe, false en caso contrario.
     */
    public boolean containsVertex(String airportCode) {
        return airportCodeToIndexMap.containsKey(airportCode);
    }

    /**
     * Añade una arista (ruta) dirigida entre dos vértices (aeropuertos) con un peso (duración).
     * @param u Índice del vértice de origen.
     * @param v Índice del vértice de destino.
     * @param weight Peso/duración de la arista.
     * @throws IllegalArgumentException Si los índices de vértice son inválidos o si la arista ya existe.
     * @throws ListException Si hay un error al añadir a la SinglyLinkedList.
     */
    public void addEdge(int u, int v, int weight) throws ListException {
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para añadir arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }
        if (u == v) { // No permitir bucles propios
            return;
        }

        SinglyLinkedList connections = adjList.get(u);

        // Verificar si la arista ya existe
        for (int i = 0; i < connections.size(); i++) {
            int[] existingEdge = (int[]) connections.get(i);
            if (existingEdge[0] == v) {
                //System.out.println("ADVERTENCIA: La arista de " + indexToAirportCodeArray[u] + " a " + indexToAirportCodeArray[v] + " ya existe. No se añadió duplicado.");
                return; // La arista ya existe, no hacer nada
            }
        }

        int[] edge = new int[]{v, weight};
        connections.add(edge);
        numEdges++;
    }

    /**
     * Modifica el peso de una arista existente.
     * @param u Índice del vértice de origen.
     * @param v Índice del vértice de destino.
     * @param newWeight El nuevo peso/duración de la arista.
     * @throws IllegalArgumentException Si los índices de vértice son inválidos.
     * @throws ListException Si la arista no se encuentra o hay un error con la SinglyLinkedList.
     */
    public void modifyEdge(int u, int v, int newWeight) throws ListException {
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
                System.out.println("[GRAFO] Arista de " + indexToAirportCodeArray[u] + " a " + indexToAirportCodeArray[v] + " modificada a nuevo peso: " + newWeight);
                break;
            }
        }
        if (!found) {
            throw new ListException("Arista de " + indexToAirportCodeArray[u] + " a " + indexToAirportCodeArray[v] + " no encontrada para modificar.");
        }
    }

    /**
     * Verifica si existe una arista dirigida entre dos aeropuertos.
     * @param uAirportCode Código del aeropuerto de origen.
     * @param vAirportCode Código del aeropuerto de destino.
     * @return true si la arista existe, false en caso contrario.
     * @throws ListException Si hay un error al acceder a la lista de adyacencia.
     */
    public boolean containsEdge(String uAirportCode, String vAirportCode) throws ListException {
        int u = getIndexForAirportCode(uAirportCode);
        int v = getIndexForAirportCode(vAirportCode);

        if (u == -1 || v == -1) {
            return false; // Uno o ambos aeropuertos no existen
        }

        SinglyLinkedList connections = adjList.get(u);
        if (connections != null) {
            for (int i = 0; i < connections.size(); i++) {
                int[] existingEdge = (int[]) connections.get(i);
                if (existingEdge[0] == v) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Obtiene el número total de vértices (aeropuertos) en el grafo.
     * @return El número de vértices.
     */
    public int getNumVertices() {
        return numVertices;
    }

    /**
     * Obtiene el número total de aristas (rutas) en el grafo.
     * @return El número de aristas.
     */
    public int getNumEdges() {
        return numEdges;
    }

    /**
     * Obtiene el índice entero para un código de aeropuerto dado.
     * @param airportCode El código del aeropuerto.
     * @return El índice correspondiente, o -1 si el aeropuerto no existe.
     */
    public int getIndexForAirportCode(String airportCode) {
        return airportCodeToIndexMap.getOrDefault(airportCode, -1);
    }

    /**
     * Obtiene el código de aeropuerto para un índice entero dado.
     * @param index El índice del vértice.
     * @return El código del aeropuerto, o null si el índice es inválido.
     */
    public String getAirportCodeForIndex(int index) {
        if (index >= 0 && index < numVertices) {
            return indexToAirportCodeArray[index];
        }
        return null;
    }

    /**
     * Obtiene el conteo de rutas salientes desde un aeropuerto específico.
     * @param airportCode El código del aeropuerto.
     * @return El número de rutas salientes, o 0 si el aeropuerto no existe.
     */
    public int getOutgoingRouteCount(String airportCode) {
        int index = getIndexForAirportCode(airportCode);
        if (index == -1) {
            return 0;
        }
        SinglyLinkedList connections = adjList.get(index);
        return connections != null ? connections.size() : 0;
    }

    /**
     * Obtiene una lista de todos los códigos de aeropuerto en el grafo.
     * @return Una SinglyLinkedList con los códigos de aeropuerto.
     * @throws ListException Si hay un error al manipular la lista.
     */
    public SinglyLinkedList getAllAirportCodes() throws ListException {
        SinglyLinkedList codes = new SinglyLinkedList();
        // Usar la lista de índices para asegurar orden consistente (aunque no es estrictamente necesario aquí)
        for (int i = 0; i < numVertices; i++) {
            codes.add(indexToAirportCodeArray[i]);
        }
        return codes;
    }

    /**
     * Genera rutas aleatorias entre aeropuertos existentes dentro del grafo.
     * @param minRoutesPerAirport Número mínimo de rutas a generar por aeropuerto.
     * @param maxRoutesPerAirport Número máximo de rutas a generar por aeropuerto.
     * @param minWeight Peso/duración mínima para las rutas generadas.
     * @param maxWeight Peso/duración máxima para las rutas generadas.
     * @throws ListException Si hay un error al añadir rutas.
     */
    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minWeight, int maxWeight) throws ListException {
        SinglyLinkedList allCodes = getAllAirportCodes();

        if (allCodes.isEmpty() || allCodes.size() < 2) {
            // No hay suficientes aeropuertos para generar rutas.
            return;
        }

        //System.out.println("\n--- Generando rutas aleatorias ---");
        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k);
            int originIndex = getIndexForAirportCode(originCode);

            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0;
            final int MAX_ATTEMPTS_PER_ROUTE = 50; // Límite de intentos para encontrar una ruta válida

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size()));
                int destinationIndex = getIndexForAirportCode(destinationCode);

                int weight = random.nextInt(maxWeight - minWeight + 1) + minWeight;

                if (originIndex != destinationIndex) { // Evitar rutas a sí mismo
                    try {
                        // Se delega la verificación de existencia a addEdge, que retorna si la arista ya existe.
                        addEdge(originIndex, destinationIndex, weight);
                        // Solo incrementamos si addEdge realmente añade una nueva arista
                        // (es decir, si no existía previamente)
                        // Para esto, addEdge debería retornar un boolean o lanzar una excepción específica.
                        // Por ahora, asumimos que si no lanza excepción, se añadió o ya existía y no se duplico.
                        // Una forma más precisa sería hacer un `containsEdge` primero.
                        boolean edgeAlreadyExists = false; // Este bloque no es estrictamente necesario si addEdge ya maneja duplicados
                        SinglyLinkedList currentConnections = adjList.get(originIndex);
                        if (currentConnections != null) {
                            for (int i = 0; i < currentConnections.size(); i++) {
                                int[] existingEdge = (int[]) currentConnections.get(i);
                                if (existingEdge[0] == destinationIndex && existingEdge[1] == weight) { // Check if same edge with same weight
                                    edgeAlreadyExists = true;
                                    break;
                                }
                            }
                        }
                        if (!edgeAlreadyExists) { // This logic is simplified; addEdge already prevents duplicates based on u,v
                            generatedCount++;
                        }
                    } catch (IllegalArgumentException | ListException e) {
                        System.err.println("ADVERTENCIA: Error al añadir ruta aleatoria (" + originCode + " -> " + destinationCode + "): " + e.getMessage());
                    }
                }
                attemptCount++;
            }
            if (generatedCount < routesToGenerate) {
                //System.out.println("ADVERTENCIA: Solo se generaron " + generatedCount + " rutas para " + originCode + " (se intentaron " + routesToGenerate + ") debido al límite de intentos o falta de destinos válidos.");
            }
        }
        //System.out.println("--- Generación de Rutas Aleatorias Completada. Total de aristas: " + numEdges + " ---");
    }

    /**
     * Calcula la ruta más corta (en duración/peso) entre dos aeropuertos usando el algoritmo de Dijkstra.
     * @param startAirportCode Código del aeropuerto de origen.
     * @param endAirportCode Código del aeropuerto de destino.
     * @return La duración más corta en minutos, o Integer.MAX_VALUE si no hay un camino.
     * @throws ListException Si hay un error al acceder a las listas de adyacencia.
     */
    public int shortestPath(String startAirportCode, String endAirportCode) throws ListException {
        int startIndex = getIndexForAirportCode(startAirportCode);
        int endIndex = getIndexForAirportCode(endAirportCode);

        if (startIndex == -1 || endIndex == -1) {
            //System.err.println("ERROR: Uno o ambos aeropuertos no existen en el grafo para el cálculo de ruta más corta.");
            return Integer.MAX_VALUE; // No se puede calcular si los aeropuertos no existen
        }
        if (startIndex == endIndex) {
            return 0; // La distancia a sí mismo es 0
        }

        int[] distances = new int[numVertices];
        Arrays.fill(distances, Integer.MAX_VALUE); // Inicializar distancias a infinito

        boolean[] visited = new boolean[numVertices];
        Arrays.fill(visited, false); // Inicializar todos los nodos como no visitados

        // Cola de prioridad para seleccionar el nodo con la distancia más pequeña
        PriorityQueue<Node> pq = new PriorityQueue<>(numVertices, Comparator.comparingInt(Node::getDistance));

        distances[startIndex] = 0; // Distancia del origen a sí mismo es 0
        pq.add(new Node(startIndex, 0)); // Añadir el nodo de origen a la cola de prioridad

        while (!pq.isEmpty()) {
            int u = pq.poll().getVertex(); // Extraer el nodo con la distancia mínima

            if (visited[u]) {
                continue; // Si ya fue visitado, saltar
            }

            visited[u] = true; // Marcar como visitado

            if (u == endIndex) {
                return distances[endIndex]; // Se encontró la ruta más corta al destino
            }

            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) {
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edge = (int[]) neighbors.get(i);
                    int v = edge[0]; // Vértice adyacente
                    int weight = edge[1]; // Peso de la arista

                    // Relajación de la arista
                    if (!visited[v] && distances[u] != Integer.MAX_VALUE && distances[u] + weight < distances[v]) {
                        distances[v] = distances[u] + weight;
                        pq.add(new Node(v, distances[v]));
                    }
                }
            }
        }

        return distances[endIndex]; // Retorna la distancia al destino (MAX_VALUE si no es alcanzable)
    }

    /**
     * Clase interna privada para representar un nodo en la cola de prioridad de Dijkstra.
     */
    private static class Node {
        private final int vertex;
        private final int distance;

        public Node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        public int getVertex() { return vertex; }
        public int getDistance() { return distance; }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Grafo Dirigido (Vértices: " + numVertices + ", Aristas: " + numEdges + "):\n");
        for (int i = 0; i < numVertices; i++) {
            String originCode = indexToAirportCodeArray[i];
            sb.append("[").append(i).append("] ").append(originCode).append(" -> ");
            SinglyLinkedList connections = adjList.get(i);
            if (connections != null && !connections.isEmpty()) {
                try {
                    for (int j = 0; j < connections.size(); j++) {
                        int[] edge = (int[]) connections.get(j);
                        String destCode = indexToAirportCodeArray[edge[0]];
                        sb.append(destCode).append(" (").append(edge[1]).append(" min)").append(j < connections.size() - 1 ? ", " : "");
                    }
                } catch (ListException e) {
                    sb.append("ERROR: ").append(e.getMessage());
                }
            } else {
                sb.append("No hay rutas salientes.");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}