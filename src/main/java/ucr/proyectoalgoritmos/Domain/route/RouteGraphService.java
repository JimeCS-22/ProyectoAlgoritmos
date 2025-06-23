package ucr.proyectoalgoritmos.Domain.route;

import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList; // Asumo esta es la SinglyLinkedList sin genéricos
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;
// import ucr.proyectoalgoritmos.Domain.stack.Stack; // Posiblemente ya no necesites esta importación si usas java.util.Deque

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Deque; // Importación para Deque
import java.util.ArrayDeque; // Importación para ArrayDeque

/**
 * Representa y gestiona rutas aéreas, actuando como una fachada para un grafo dirigido.
 * Permite la manipulación de rutas utilizando códigos de aeropuerto en lugar de índices internos.
 * <p>
 * **Esta clase contiene una estructura paralela para gestionar los pesos dobles (distancia/duración)
 * de las aristas, sin modificar el DirectedSinglyLinkedListGraph existente.**
 * **Se asume que SinglyLinkedList NO tiene parámetros de tipo (es decir, usa Object).**
 * </p>
 */
public class RouteGraphService {
    private DirectedSinglyLinkedListGraph internalGraph;
    private Map<String, DualEdgeInfo> dualWeightEdges;

    /**
     * Clase interna para almacenar los pesos dobles de una arista.
     */
    public static class DualEdgeInfo {
        public double distance;
        public double duration;

        public DualEdgeInfo(double distance, double duration) {
            this.distance = distance;
            this.duration = duration;
        }

        @Override
        public String toString() {
            String distStr = String.format("%.2f", distance);
            String durStr = String.format("%.0f", duration);
            return "D:" + distStr + "km, Dur:" + durStr + "min";
        }
    }

    public RouteGraphService(int maxVertices) {
        this.internalGraph = new DirectedSinglyLinkedListGraph();
        this.dualWeightEdges = new HashMap<>();
    }

    public DirectedSinglyLinkedListGraph getInternalGraph() {
        return internalGraph;
    }

    public void addVertex(String airportCode) throws ListException {
        internalGraph.addVertex(airportCode);
    }

    public boolean containsVertex(String airportCode) {
        return internalGraph.getIndexForAirportCode(airportCode) != -1;
    }

    public void addEdge(String originCode, String destinationCode, int weight) throws ListException, IllegalArgumentException {
        int u = internalGraph.getIndexForAirportCode(originCode);
        int v = internalGraph.getIndexForAirportCode(destinationCode);
        if (u == -1) {
            throw new IllegalArgumentException("El aeropuerto de origen '" + originCode + "' no existe como vértice.");
        }
        if (v == -1) {
            throw new IllegalArgumentException("El aeropuerto de destino '" + destinationCode + "' no existe como vértice.");
        }
        internalGraph.addEdge(u, v, weight);
    }

    public boolean modifyEdge(String originCode, String destinationCode, int newWeight) throws ListException, IllegalArgumentException {
        int u = internalGraph.getIndexForAirportCode(originCode);
        int v = internalGraph.getIndexForAirportCode(destinationCode);
        if (u == -1) {
            throw new IllegalArgumentException("El aeropuerto de origen '" + originCode + "' no existe como vértice.");
        }
        if (v == -1) {
            throw new IllegalArgumentException("El aeropuerto de destino '" + destinationCode + "' no existe como vértice.");
        }
        return internalGraph.modifyEdge(u, v, newWeight);
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

    public boolean hasDirectRoute(String originCode, String destinationCode) {
        try {
            return internalGraph.hasEdge(originCode, destinationCode);
        } catch (ListException e) {
            System.err.println("Error al verificar ruta directa en grafo: " + e.getMessage());
            return false;
        }
    }

    // --- INICIO DE NUEVOS MÉTODOS Y LÓGICA PARA DOBLE PESO ---

    public void addDualWeightRoute(String originCode, String destinationCode, double distance, double duration) throws ListException, IllegalArgumentException {
        internalGraph.addVertex(originCode);
        internalGraph.addVertex(destinationCode);

        String edgeKey = originCode + "-" + destinationCode;
        dualWeightEdges.put(edgeKey, new DualEdgeInfo(distance, duration));

        internalGraph.addEdge(internalGraph.getIndexForAirportCode(originCode),
                internalGraph.getIndexForAirportCode(destinationCode),
                (int) Math.round(distance));
    }

    public void removeDualWeightRoute(String originCode, String destinationCode) throws ListException, IllegalArgumentException {
        String edgeKey = originCode + "-" + destinationCode;

        if (!dualWeightEdges.containsKey(edgeKey)) {
            throw new IllegalArgumentException("La ruta con doble peso de " + originCode + " a " + destinationCode + " no existe.");
        }
        dualWeightEdges.remove(edgeKey);

        System.out.println("Ruta con doble peso de " + originCode + " a " + destinationCode + " eliminada (solo de estructura auxiliar).");
    }

    /**
     * Calcula los pesos (distancia y duración) de la ruta más corta entre dos aeropuertos
     * basándose en un criterio específico ("distance" o "duration").
     * Este método es NUEVO e implementa su propio Dijkstra para usar los pesos dobles.
     *
     * @param startCode El código del aeropuerto de origen.
     * @param endCode El código del aeropuerto de destino.
     * @param criteria El criterio para el camino más corto ("distance" o "duration").
     * @return Un array double[2] con {peso_total_por_criterio, otro_peso_total_del_camino}, o null si no hay ruta.
     * @throws ListException Si ocurre un error interno en la lista.
     * @throws IllegalArgumentException Si el criterio es inválido o los aeropuertos no existen.
     */
    public double[] getShortestPathDualWeights(String startCode, String endCode, String criteria) throws ListException {
        int startIndex = internalGraph.getIndexForAirportCode(startCode);
        int endIndex = internalGraph.getIndexForAirportCode(endCode);

        if (startIndex == -1 || endIndex == -1 || internalGraph.getNumVertices() == 0) {
            return null;
        }
        if (startIndex == endIndex) {
            return new double[]{0, 0};
        }

        double[] dist = new double[internalGraph.getNumVertices()];
        int[] prev = new int[internalGraph.getNumVertices()];
        boolean[] visited = new boolean[internalGraph.getNumVertices()];

        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        Arrays.fill(visited, false);

        // ¡Cambiado a PathNode_Internal!
        PriorityQueue<PathNode_Internal> pq = new PriorityQueue<>(internalGraph.getNumVertices(),
                Comparator.comparingDouble(n -> n.currentWeight));

        dist[startIndex] = 0;
        pq.add(new PathNode_Internal(startIndex, 0));

        while (!pq.isEmpty()) {
            PathNode_Internal current = pq.poll();
            int u = current.vertexIndex;
            double currentDist = current.currentWeight;

            if (visited[u]) {
                continue;
            }
            visited[u] = true;

            if (u == endIndex) {
                break;
            }

            // Ahora adjList no tiene genéricos, así que hay que castear
            ArrayList<SinglyLinkedList> adjList = internalGraph.getAdjList();
            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) {
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edgeArray = (int[]) neighbors.get(i); // <-- CAST EXPLÍCITO aquí
                    int v = edgeArray[0];

                    String edgeKey = internalGraph.getAirportCodeForIndex(u) + "-" + internalGraph.getAirportCodeForIndex(v);
                    DualEdgeInfo dualInfo = dualWeightEdges.get(edgeKey);

                    if (dualInfo == null) {
                        continue;
                    }

                    double edgeWeightForCriteria;
                    if (criteria.equalsIgnoreCase("distance")) {
                        edgeWeightForCriteria = dualInfo.distance;
                    } else if (criteria.equalsIgnoreCase("duration")) {
                        edgeWeightForCriteria = dualInfo.duration;
                    } else {
                        throw new IllegalArgumentException("Criterio inválido: " + criteria);
                    }

                    if (!visited[v] && currentDist != Double.POSITIVE_INFINITY && currentDist + edgeWeightForCriteria < dist[v]) {
                        dist[v] = currentDist + edgeWeightForCriteria;
                        prev[v] = u;
                        pq.add(new PathNode_Internal(v, dist[v])); // ¡Cambiado a PathNode_Internal!
                    }
                }
            }
        }

        if (dist[endIndex] == Double.POSITIVE_INFINITY) {
            return null;
        }

        double totalDistance = 0;
        double totalDuration = 0;
        int currentVertexIndex = endIndex;
        // Cambio de Stack a Deque/ArrayDeque
        Deque<Integer> pathStackForTraversal = new ArrayDeque<>();

        while (currentVertexIndex != -1) {
            pathStackForTraversal.push(currentVertexIndex); // push para Deque como pila
            if (currentVertexIndex == startIndex) break;
            currentVertexIndex = prev[currentVertexIndex];
        }

        if (pathStackForTraversal.isEmpty() || pathStackForTraversal.peek() != startIndex) {
            return null;
        }

        int prevVertexIndex = pathStackForTraversal.pop(); // pop para Deque como pila
        while (!pathStackForTraversal.isEmpty()) {
            int nextVertexIndex = pathStackForTraversal.pop(); // pop para Deque como pila

            String edgeKey = internalGraph.getAirportCodeForIndex(prevVertexIndex) + "-" + internalGraph.getAirportCodeForIndex(nextVertexIndex);
            DualEdgeInfo dualInfo = dualWeightEdges.get(edgeKey);

            if (dualInfo != null) {
                totalDistance += dualInfo.distance;
                totalDuration += dualInfo.duration;
            } else {
                System.err.println("Error de sincronización: Ruta " + internalGraph.getAirportCodeForIndex(prevVertexIndex) + "->" + internalGraph.getAirportCodeForIndex(nextVertexIndex) + " encontrada por Dijkstra pero sin DualEdgeInfo.");
            }
            prevVertexIndex = nextVertexIndex;
        }

        double[] result = new double[2];
        if (criteria.equalsIgnoreCase("distance")) {
            result[0] = totalDistance;
            result[1] = totalDuration;
        } else {
            result[0] = totalDuration;
            result[1] = totalDistance;
        }
        return result;
    }

    /**
     * Obtiene la secuencia de aeropuertos que forman la ruta más corta, basada en un criterio.
     * Este método es NUEVO. Reconstruye el camino usando el 'prev' array de Dijkstra de doble peso.
     */
    public SinglyLinkedList getPathDualWeight(String startCode, String endCode, String criteria) throws ListException {
        int startIndex = internalGraph.getIndexForAirportCode(startCode);
        int endIndex = internalGraph.getIndexForAirportCode(endCode);

        SinglyLinkedList pathList = new SinglyLinkedList();
        if (startIndex == -1 || endIndex == -1 || internalGraph.getNumVertices() == 0) {
            return pathList;
        }
        if (startIndex == endIndex) {
            pathList.add(startCode);
            return pathList;
        }

        double[] dist = new double[internalGraph.getNumVertices()];
        int[] prev = new int[internalGraph.getNumVertices()];
        boolean[] visited = new boolean[internalGraph.getNumVertices()];

        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        Arrays.fill(prev, -1);
        Arrays.fill(visited, false);

        // ¡Cambiado a PathNode_Internal!
        PriorityQueue<PathNode_Internal> pq = new PriorityQueue<>(internalGraph.getNumVertices(),
                Comparator.comparingDouble(n -> n.currentWeight));

        dist[startIndex] = 0;
        pq.add(new PathNode_Internal(startIndex, 0));

        while (!pq.isEmpty()) {
            PathNode_Internal current = pq.poll();
            int u = current.vertexIndex;
            double currentDist = current.currentWeight;

            if (visited[u]) {
                continue;
            }
            visited[u] = true;

            if (u == endIndex) {
                break;
            }

            // Ahora adjList no tiene genéricos, así que hay que castear
            ArrayList<SinglyLinkedList> adjList = internalGraph.getAdjList();
            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) {
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edgeArray = (int[]) neighbors.get(i); // <-- CAST EXPLÍCITO aquí
                    int v = edgeArray[0];

                    String edgeKey = internalGraph.getAirportCodeForIndex(u) + "-" + internalGraph.getAirportCodeForIndex(v);
                    DualEdgeInfo dualInfo = dualWeightEdges.get(edgeKey);

                    if (dualInfo == null) {
                        continue;
                    }

                    double edgeWeightForCriteria;
                    if (criteria.equalsIgnoreCase("distance")) {
                        edgeWeightForCriteria = dualInfo.distance;
                    } else if (criteria.equalsIgnoreCase("duration")) {
                        edgeWeightForCriteria = dualInfo.duration;
                    } else {
                        throw new IllegalArgumentException("Criterio inválido: " + criteria);
                    }

                    if (!visited[v] && currentDist != Double.POSITIVE_INFINITY && currentDist + edgeWeightForCriteria < dist[v]) {
                        dist[v] = currentDist + edgeWeightForCriteria;
                        prev[v] = u;
                        pq.add(new PathNode_Internal(v, dist[v])); // ¡Cambiado a PathNode_Internal!
                    }
                }
            }
        }

        if (dist[endIndex] == Double.POSITIVE_INFINITY) {
            return new SinglyLinkedList();
        }

        int currentVertex = endIndex;
        // Cambio de Stack a Deque/ArrayDeque
        Deque<String> pathStack = new ArrayDeque<>();
        while (currentVertex != -1) {
            pathStack.push(internalGraph.getAirportCodeForIndex(currentVertex)); // push para Deque como pila
            if (currentVertex == startIndex) break;
            currentVertex = prev[currentVertex];
        }

        while (!pathStack.isEmpty()) {
            pathList.add(pathStack.pop()); // pop para Deque como pila
        }
        return pathList;
    }

    /**
     * Genera rutas aleatorias con doble peso (distancia y duración).
     * Este método es NUEVO. Utiliza addDualWeightRoute.
     */
    public void generateRandomDualWeightRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, double minDistance, double maxDistance) throws ListException {
        SinglyLinkedList allCodes = internalGraph.getAllAirportCodes();
        if (allCodes.isEmpty() || allCodes.size() < 2) {
            System.out.println("ADVERTENCIA: No hay suficientes aeropuertos cargados para generar rutas aleatorias.");
            return;
        }

        java.util.Random random = new java.util.Random();

        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k); // <-- CAST EXPLÍCITO aquí
            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0;
            final int MAX_ATTEMPTS_PER_ROUTE = 50;

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size())); // <-- CAST EXPLÍCITO aquí
                if (!originCode.equals(destinationCode)) {
                    double distance = minDistance + (maxDistance - minDistance) * random.nextDouble();
                    double duration = (distance / 5.0) + (random.nextDouble() * (distance / 10.0));

                    try {
                        addDualWeightRoute(originCode, destinationCode, distance, duration);
                        generatedCount++;
                    } catch (IllegalArgumentException | ListException e) {
                        // Puede fallar si ya existe o por otras razones, se ignora y se intenta de nuevo.
                    }
                }
                attemptCount++;
            }
        }
    }

    // Clase interna para la PriorityQueue (PathNode_Internal - Renombrada)
    private static class PathNode_Internal { // <-- RENOMBRADO de PathNode a PathNode_Internal
        public int vertexIndex;
        public double currentWeight;

        public PathNode_Internal(int vertexIndex, double currentWeight) {
            this.vertexIndex = vertexIndex;
            this.currentWeight = currentWeight;
        }
    }

    /**
     * Devuelve una representación en String del grafo y sus aristas con pesos dobles.
     */
    public String getGraphDetails() throws ListException {
        String result = "Grafo de Rutas (Vértices: " + internalGraph.getNumVertices() + "):\n";
        SinglyLinkedList allCodes = internalGraph.getAllAirportCodes();

        for (int i = 0; i < allCodes.size(); i++) {
            String originCode = (String) allCodes.get(i); // <-- CAST EXPLÍCITO aquí
            result += "[" + i + "] " + originCode + " -> ";

            int originIndex = internalGraph.getIndexForAirportCode(originCode);
            if (originIndex != -1 && originIndex < internalGraph.getAdjList().size()) {
                ArrayList<SinglyLinkedList> adjList = internalGraph.getAdjList(); // getAdjList retorna ArrayList<SinglyLinkedList>
                SinglyLinkedList connections = adjList.get(originIndex);
                if (connections != null && !connections.isEmpty()) {
                    boolean first = true;
                    for (int j = 0; j < connections.size(); j++) {
                        int[] edgeArray = (int[]) connections.get(j); // <-- CAST EXPLÍCITO aquí
                        int destIndex = edgeArray[0];
                        String destCode = internalGraph.getAirportCodeForIndex(destIndex);
                        String edgeKey = originCode + "-" + destCode;
                        DualEdgeInfo dualInfo = dualWeightEdges.get(edgeKey);

                        if (!first) {
                            result += ", ";
                        }
                        if (dualInfo != null) {
                            result += destCode + " (" + dualInfo.toString() + ")";
                        } else {
                            result += destCode + " (Int W: " + edgeArray[1] + " - No Dual Info)";
                        }
                        first = false;
                    }
                } else {
                    result += "No hay rutas salientes.";
                }
            } else {
                result += "Error: Índice de origen inválido o adjList no disponible.";
            }
            result += "\n";
        }
        return result;
    }
}