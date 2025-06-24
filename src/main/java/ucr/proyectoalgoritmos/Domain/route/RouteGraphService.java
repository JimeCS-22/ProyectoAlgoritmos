package ucr.proyectoalgoritmos.Domain.route;

import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.list.SinglyLinkedList;
import ucr.proyectoalgoritmos.graph.DirectedSinglyLinkedListGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Deque;
import java.util.ArrayDeque;


public class RouteGraphService {
    private DirectedSinglyLinkedListGraph internalGraph;
    private Map<String, DualEdgeInfo> dualWeightEdges;

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


    public void addDualWeightRoute(String originCode, String destinationCode, double distance, double duration) throws ListException, IllegalArgumentException {
        internalGraph.addVertex(originCode);
        internalGraph.addVertex(destinationCode);

        String edgeKey = originCode + "-" + destinationCode;
        dualWeightEdges.put(edgeKey, new DualEdgeInfo(distance, duration));

        internalGraph.addEdge(internalGraph.getIndexForAirportCode(originCode),
                internalGraph.getIndexForAirportCode(destinationCode),
                (int) Math.round(distance));

        System.out.println("DEBUG RouteGraphService: Ruta dual añadida: " + originCode + "->" + destinationCode + " (Dist:" + distance + ", Dur:" + duration + ")");
    }

    public void removeDualWeightRoute(String originCode, String destinationCode) throws ListException, IllegalArgumentException {
        String edgeKey = originCode + "-" + destinationCode;

        if (!dualWeightEdges.containsKey(edgeKey)) {
            throw new IllegalArgumentException("La ruta con doble peso de " + originCode + " a " + destinationCode + " no existe.");
        }
        dualWeightEdges.remove(edgeKey);

        System.out.println("Ruta con doble peso de " + originCode + " a " + destinationCode + " eliminada (solo de estructura auxiliar).");
    }


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

        PriorityQueue<PathNode_Internal> pq = new PriorityQueue<>(internalGraph.getNumVertices(),
                Comparator.comparingDouble(n -> n.currentWeight));

        dist[startIndex] = 0;
        pq.add(new PathNode_Internal(startIndex, 0));



        while (!pq.isEmpty()) {
            PathNode_Internal current = pq.poll();
            int u = current.vertexIndex;
            double currentDist = current.currentWeight;

            if (visited[u]) {
                System.out.println("DEBUG getShortestPathDualWeights: Vértice " + internalGraph.getAirportCodeForIndex(u) + " ya visitado. Continuando.");
                continue;
            }
            visited[u] = true;

            if (u == endIndex) {
                System.out.println("DEBUG getShortestPathDualWeights: ¡Vértice final " + internalGraph.getAirportCodeForIndex(endIndex) + " alcanzado! Distancia total por criterio: " + dist[endIndex]);
                break;
            }

            ArrayList<SinglyLinkedList> adjList = internalGraph.getAdjList();
            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) {
                System.out.println("DEBUG getShortestPathDualWeights: Procesando vecinos de " + internalGraph.getAirportCodeForIndex(u) + ":");
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edgeArray = (int[]) neighbors.get(i);
                    int v = edgeArray[0];

                    String edgeKey = internalGraph.getAirportCodeForIndex(u) + "-" + internalGraph.getAirportCodeForIndex(v);
                    DualEdgeInfo dualInfo = dualWeightEdges.get(edgeKey);

                    System.out.println(" DEBUG getShortestPathDualWeights: Vecino " + internalGraph.getAirportCodeForIndex(v) + " (Key: " + edgeKey + ")");

                    if (dualInfo == null) {
                        System.out.println(" DEBUG getShortestPathDualWeights: ADVERTENCIA: No se encontró DualEdgeInfo para la clave " + edgeKey + ". Saltando arista.");
                        continue;
                    }

                    double edgeWeightForCriteria;
                    if (criteria.equalsIgnoreCase("distance")) {
                        edgeWeightForCriteria = dualInfo.distance;
                    } else if (criteria.equalsIgnoreCase("duration")) {
                        edgeWeightForCriteria = dualInfo.duration;
                    } else {
                        System.err.println("DEBUG getShortestPathDualWeights: Criterio inválido: " + criteria);
                        throw new IllegalArgumentException("Criterio inválido: " + criteria);
                    }

                    System.out.println(" DEBUG getShortestPathDualWeights: Peso de la arista (" + criteria + "): " + edgeWeightForCriteria);

                    if (!visited[v] && currentDist != Double.POSITIVE_INFINITY && currentDist + edgeWeightForCriteria < dist[v]) {
                        dist[v] = currentDist + edgeWeightForCriteria;
                        prev[v] = u;
                        pq.add(new PathNode_Internal(v, dist[v]));
                        System.out.println("    DEBUG getShortestPathDualWeights: Actualizando dist(" + internalGraph.getAirportCodeForIndex(v) + ") a " + dist[v] + ". Añadiendo a PQ. Prev: " + internalGraph.getAirportCodeForIndex(u));
                    } else {
                        System.out.println(" DEBUG getShortestPathDualWeights: No se actualiza dist(" + internalGraph.getAirportCodeForIndex(v) + "). Ya visitado o nueva dist " + (currentDist + edgeWeightForCriteria) + " no es menor que actual " + dist[v] + ".");
                    }
                }
            }
        }

        if (dist[endIndex] == Double.POSITIVE_INFINITY) {
            System.out.println("DEBUG getShortestPathDualWeights: Vértice final " + internalGraph.getAirportCodeForIndex(endIndex) + " no alcanzado. Retornando null.");
            return null;
        }

        double totalDistance = 0;
        double totalDuration = 0;
        int currentVertexIndex = endIndex;
        Deque<Integer> pathStackForTraversal = new ArrayDeque<>();

        System.out.println("DEBUG getShortestPathDualWeights: Reconstruyendo camino...");
        while (currentVertexIndex != -1) {
            System.out.println("DEBUG getShortestPathDualWeights: Añadiendo a pila de recorrido: " + internalGraph.getAirportCodeForIndex(currentVertexIndex));
            pathStackForTraversal.push(currentVertexIndex);
            if (currentVertexIndex == startIndex) break;
            currentVertexIndex = prev[currentVertexIndex];
        }

        if (pathStackForTraversal.isEmpty() || pathStackForTraversal.peek() != startIndex) {
            System.out.println("DEBUG getShortestPathDualWeights: Fallo en la reconstrucción del camino.");
            return null;
        }

        int prevVertexIndex = pathStackForTraversal.pop();
        while (!pathStackForTraversal.isEmpty()) {
            int nextVertexIndex = pathStackForTraversal.pop();

            String edgeKey = internalGraph.getAirportCodeForIndex(prevVertexIndex) + "-" + internalGraph.getAirportCodeForIndex(nextVertexIndex);
            DualEdgeInfo dualInfo = dualWeightEdges.get(edgeKey);

            System.out.println("DEBUG getShortestPathDualWeights: Procesando arista " + internalGraph.getAirportCodeForIndex(prevVertexIndex) + "->" + internalGraph.getAirportCodeForIndex(nextVertexIndex) + " (Key: " + edgeKey + ")");

            if (dualInfo != null) {
                totalDistance += dualInfo.distance;
                totalDuration += dualInfo.duration;
                System.out.println("DEBUG getShortestPathDualWeights: Añadiendo: Dist=" + dualInfo.distance + ", Dur=" + dualInfo.duration + ". Totales: Dist=" + totalDistance + ", Dur=" + totalDuration);
            } else {
                System.err.println("DEBUG getShortestPathDualWeights: ERROR FATAL: Ruta " + internalGraph.getAirportCodeForIndex(prevVertexIndex) + "->" + internalGraph.getAirportCodeForIndex(nextVertexIndex) + " encontrada por Dijkstra pero sin DualEdgeInfo. Esto indica un problema de sincronización de datos.");
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
        System.out.println("DEBUG getShortestPathDualWeights: Resultado final: [" + result[0] + ", " + result[1] + "]");
        return result;
    }

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

            ArrayList<SinglyLinkedList> adjList = internalGraph.getAdjList();
            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) {
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edgeArray = (int[]) neighbors.get(i);
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
                        pq.add(new PathNode_Internal(v, dist[v]));
                    }
                }
            }
        }

        if (dist[endIndex] == Double.POSITIVE_INFINITY) {
            return new SinglyLinkedList();
        }

        int currentVertex = endIndex;
        Deque<String> pathStack = new ArrayDeque<>();
        while (currentVertex != -1) {
            pathStack.push(internalGraph.getAirportCodeForIndex(currentVertex));
            if (currentVertex == startIndex) break;
            currentVertex = prev[currentVertex];
        }

        while (!pathStack.isEmpty()) {
            pathList.add(pathStack.pop());
        }
        return pathList;
    }

    public void generateRandomDualWeightRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, double minDistance, double maxDistance) throws ListException {
        SinglyLinkedList allCodes = internalGraph.getAllAirportCodes();
        if (allCodes.isEmpty() || allCodes.size() < 2) {
            System.out.println("ADVERTENCIA: No hay suficientes aeropuertos cargados para generar rutas aleatorias.");
            return;
        }

        java.util.Random random = new java.util.Random();

        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k);
            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0;
            final int MAX_ATTEMPTS_PER_ROUTE = 50;

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size()));
                if (!originCode.equals(destinationCode)) {
                    double distance = minDistance + (maxDistance - minDistance) * random.nextDouble();
                    double duration = (distance / 5.0) + (random.nextDouble() * (distance / 10.0));

                    try {
                        addDualWeightRoute(originCode, destinationCode, distance, duration);
                        generatedCount++;
                    } catch (IllegalArgumentException | ListException e) {

                    }
                }
                attemptCount++;
            }
        }
    }

    private static class PathNode_Internal {
        public int vertexIndex;
        public double currentWeight;

        public PathNode_Internal(int vertexIndex, double currentWeight) {
            this.vertexIndex = vertexIndex;
            this.currentWeight = currentWeight;
        }
    }


    public String getGraphDetails() throws ListException {
        String result = "Grafo de Rutas (Vértices: " + internalGraph.getNumVertices() + "):\n";
        SinglyLinkedList allCodes = internalGraph.getAllAirportCodes();

        for (int i = 0; i < allCodes.size(); i++) {
            String originCode = (String) allCodes.get(i);
            result += "[" + i + "] " + originCode + " -> ";

            int originIndex = internalGraph.getIndexForAirportCode(originCode);
            if (originIndex != -1 && originIndex < internalGraph.getAdjList().size()) {
                ArrayList<SinglyLinkedList> adjList = internalGraph.getAdjList();
                SinglyLinkedList connections = adjList.get(originIndex);
                if (connections != null && !connections.isEmpty()) {
                    boolean first = true;
                    for (int j = 0; j < connections.size(); j++) {
                        int[] edgeArray = (int[]) connections.get(j);
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