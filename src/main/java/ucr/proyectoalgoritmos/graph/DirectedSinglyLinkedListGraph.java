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

/**
 * Representa un **grafo dirigido** utilizando una **lista de adyacencia**
 * implementada con {@link SinglyLinkedList}.
 * Este grafo está diseñado para almacenar aeropuertos como vértices (identificados por códigos de aeropuerto {@code String})
 * y rutas como aristas dirigidas con pesos (duración/distancia).
 * Utiliza un mapeo de códigos de aeropuerto a índices enteros para la eficiencia en las operaciones del grafo.
 */
public class DirectedSinglyLinkedListGraph {
    /**
     * Mapea códigos de aeropuerto (String) a sus índices enteros correspondientes en el grafo.
     * Permite una búsqueda rápida del índice de un vértice dado su código.
     */
    private final Map<String, Integer> airportCodeToIndexMap;
    /**
     * Un array que mapea índices enteros a sus códigos de aeropuerto correspondientes.
     * Es la inversa de {@code airportCodeToIndexMap}.
     */
    private String[] indexToAirportCodeArray;
    /**
     * La lista de adyacencia del grafo. Cada elemento es una {@link SinglyLinkedList}
     * que contiene las aristas salientes de un vértice específico.
     * Una arista se representa como un array `int[]` donde `[0]` es el índice del vértice destino
     * y `[1]` es el peso de la arista.
     */
    private final ArrayList<SinglyLinkedList> adjList;
    /**
     * El número actual de vértices en el grafo.
     */
    private int numVertices;
    /**
     * El número actual de aristas en el grafo.
     */
    private int numEdges;
    /**
     * Generador de números aleatorios para funcionalidades como la generación de rutas.
     */
    private final Random random;

    /**
     * Constructor para inicializar un nuevo grafo dirigido.
     * Establece las estructuras de datos internas y los contadores a cero.
     * Inicializa el array de códigos con una capacidad inicial y un generador aleatorio.
     */
    public DirectedSinglyLinkedListGraph() {
        this.airportCodeToIndexMap = new HashMap<>();
        this.indexToAirportCodeArray = new String[25]; // Capacidad inicial
        this.adjList = new ArrayList<>();
        this.numVertices = 0;
        this.numEdges = 0;
        this.random = new Random();
    }

    /**
     * Añade un nuevo vértice al grafo si no existe ya.
     * Cada vértice se identifica por un código de aeropuerto único.
     * Si el vértice ya existe, retorna su índice actual.
     * Si no existe, lo añade y le asigna un nuevo índice.
     *
     * @param airportCode El código único del aeropuerto a añadir como vértice.
     * @return El índice entero asignado al aeropuerto en el grafo.
     */
    public int addVertex(String airportCode) {
        if (airportCodeToIndexMap.containsKey(airportCode)) {
            return airportCodeToIndexMap.get(airportCode); // Retorna el índice si ya existe
        }

        // Redimensionar el array de mapeo de índices si la capacidad es insuficiente
        if (numVertices >= indexToAirportCodeArray.length) {
            indexToAirportCodeArray = Arrays.copyOf(indexToAirportCodeArray, indexToAirportCodeArray.length * 2);
        }

        airportCodeToIndexMap.put(airportCode, numVertices); // Asigna el siguiente índice disponible
        indexToAirportCodeArray[numVertices] = airportCode; // Guarda el código en el array de índices
        adjList.add(new SinglyLinkedList()); // Añade una nueva lista de adyacencia para el nuevo vértice

        return numVertices++; // Incrementa y retorna el número total de vértices
    }

    /**
     * Verifica si un vértice identificado por su código de aeropuerto existe en el grafo.
     *
     * @param airportCode El código del aeropuerto a buscar.
     * @return {@code true} si el aeropuerto existe como vértice en el grafo; {@code false} en caso contrario.
     */
    public boolean containsVertex(String airportCode) {
        return airportCodeToIndexMap.containsKey(airportCode);
    }

    /**
     * Añade una arista dirigida entre dos vértices (aeropuertos) con un peso específico.
     * No permite bucles propios (aristas de un vértice a sí mismo) ni aristas duplicadas
     * (misma dirección y mismo origen/destino).
     *
     * @param u El índice del vértice de origen.
     * @param v El índice del vértice de destino.
     * @param weight El peso de la arista (ej. duración del vuelo).
     * @throws IllegalArgumentException Si los índices de los vértices son inválidos.
     * @throws ListException Si ocurre un error interno en la {@link SinglyLinkedList} al añadir la arista.
     */
    public void addEdge(int u, int v, int weight) throws ListException {
        // Validar que los índices de los vértices estén dentro del rango válido
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para añadir arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }
        // No permitir aristas de un vértice a sí mismo (bucles propios)
        if (u == v) {
            return;
        }

        SinglyLinkedList connections = adjList.get(u);

        // Verificar si la arista ya existe para evitar duplicados
        for (int i = 0; i < connections.size(); i++) {
            int[] existingEdge = (int[]) connections.get(i);
            if (existingEdge[0] == v) { // Si ya hay una arista al mismo destino
                // System.out.println("ADVERTENCIA: La arista de " + indexToAirportCodeArray[u] + " a " + indexToAirportCodeArray[v] + " ya existe. No se añadió duplicado.");
                return; // La arista ya existe, no se hace nada
            }
        }

        // Crear y añadir la nueva arista (destino, peso)
        int[] edge = new int[]{v, weight};
        connections.add(edge); // Asume que SinglyLinkedList.add() añade al final
        numEdges++; // Incrementa el contador de aristas
    }

    /**
     * Modifica el peso de una arista existente entre dos vértices.
     *
     * @param u El índice del vértice de origen.
     * @param v El índice del vértice de destino.
     * @param newWeight El nuevo peso a asignar a la arista.
     * @throws IllegalArgumentException Si los índices de los vértices son inválidos.
     * @throws ListException Si la arista no se encuentra o si hay un error al acceder a la lista.
     */
    public void modifyEdge(int u, int v, int newWeight) throws ListException {
        // Validar índices de vértices
        if (u < 0 || u >= numVertices || v < 0 || v >= numVertices) {
            throw new IllegalArgumentException("Índice de vértice inválido para modificar arista: u=" + u + ", v=" + v + ", numVertices=" + numVertices);
        }

        SinglyLinkedList connections = adjList.get(u);
        boolean found = false;

        // Buscar la arista y actualizar su peso
        for (int i = 0; i < connections.size(); i++) {
            int[] edge = (int[]) connections.get(i);
            if (edge[0] == v) { // Si el destino coincide
                edge[1] = newWeight; // Actualiza el peso
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
     * Verifica si existe una arista directa entre dos aeropuertos dados sus códigos.
     *
     * @param uAirportCode El código del aeropuerto de origen.
     * @param vAirportCode El código del aeropuerto de destino.
     * @return {@code true} si existe una arista directa; {@code false} en caso contrario
     * (incluyendo si uno o ambos aeropuertos no existen en el grafo).
     * @throws ListException Si ocurre un error al acceder a la lista de adyacencia.
     */
    public boolean hasEdge(String uAirportCode, String vAirportCode) throws ListException {
        int u = getIndexForAirportCode(uAirportCode);
        int v = getIndexForAirportCode(vAirportCode);

        // Si alguno de los aeropuertos no existe como vértice, no puede haber arista
        if (u == -1 || v == -1) {
            return false;
        }

        SinglyLinkedList connections = adjList.get(u);
        if (connections != null) {
            // Recorre las conexiones para ver si existe una arista al destino 'v'
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
     * Retorna el número total de vértices (aeropuertos) en el grafo.
     *
     * @return El número de vértices.
     */
    public int getNumVertices() {
        return numVertices;
    }

    /**
     * Retorna el número total de aristas (rutas) en el grafo.
     *
     * @return El número de aristas.
     */
    public int getNumEdges() {
        return numEdges;
    }

    /**
     * Retorna el índice entero asociado a un código de aeropuerto.
     *
     * @param airportCode El código del aeropuerto.
     * @return El índice entero del aeropuerto, o -1 si el código no se encuentra en el grafo.
     */
    public int getIndexForAirportCode(String airportCode) {
        return airportCodeToIndexMap.getOrDefault(airportCode, -1);
    }

    /**
     * Retorna el código de aeropuerto asociado a un índice entero.
     *
     * @param index El índice entero del aeropuerto.
     * @return El código de aeropuerto (String), o {@code null} si el índice es inválido.
     */
    public String getAirportCodeForIndex(int index) {
        if (index >= 0 && index < numVertices) {
            return indexToAirportCodeArray[index];
        }
        return null;
    }

    /**
     * Retorna el número de rutas salientes (aristas) desde un aeropuerto dado su código.
     *
     * @param airportCode El código del aeropuerto de origen.
     * @return El número de rutas salientes, o 0 si el aeropuerto no existe o no tiene rutas.
     */
    public int getOutgoingRouteCount(String airportCode) {
        int index = getIndexForAirportCode(airportCode);
        if (index == -1) {
            return 0; // Aeropuerto no existe
        }
        SinglyLinkedList connections = adjList.get(index);
        return connections != null ? connections.size() : 0;
    }

    /**
     * Retorna una lista de todos los códigos de aeropuerto que son vértices en el grafo.
     *
     * @return Una {@link SinglyLinkedList} que contiene todos los códigos de aeropuerto.
     * @throws ListException Si hay un error interno en la lista al obtener los códigos.
     */
    public SinglyLinkedList getAllAirportCodes() throws ListException {
        SinglyLinkedList codes = new SinglyLinkedList();
        // Recorre el array de códigos por índice para asegurar el orden.
        for (int i = 0; i < numVertices; i++) {
            codes.add(indexToAirportCodeArray[i]);
        }
        return codes;
    }

    /**
     * Genera rutas aleatorias entre los aeropuertos existentes en el grafo.
     * Cada aeropuerto tendrá entre `minRoutesPerAirport` y `maxRoutesPerAirport` rutas salientes.
     * Los pesos (duraciones) de las rutas estarán entre `minWeight` y `maxWeight`.
     * Evita crear rutas a sí mismo y maneja la adición de aristas existentes.
     *
     * @param minRoutesPerAirport El número mínimo de rutas a generar por aeropuerto.
     * @param maxRoutesPerAirport El número máximo de rutas a generar por aeropuerto.
     * @param minWeight El peso mínimo para las rutas generadas.
     * @param maxWeight El peso máximo para las rutas generadas.
     * @throws ListException Si ocurre un error al manipular las listas internas del grafo.
     */
    public void generateRandomRoutes(int minRoutesPerAirport, int maxRoutesPerAirport, int minWeight, int maxWeight) throws ListException {
        SinglyLinkedList allCodes = getAllAirportCodes();

        // No hay suficientes aeropuertos para generar rutas significativas.
        if (allCodes.isEmpty() || allCodes.size() < 2) {
            System.out.println("ADVERTENCIA: No hay suficientes aeropuertos cargados para generar rutas aleatorias. Se necesitan al menos 2.");
            return;
        }

        // System.out.println("\n--- Generando rutas aleatorias ---");
        for (int k = 0; k < allCodes.size(); k++) {
            String originCode = (String) allCodes.get(k);
            int originIndex = getIndexForAirportCode(originCode);

            int routesToGenerate = random.nextInt(maxRoutesPerAirport - minRoutesPerAirport + 1) + minRoutesPerAirport;
            int generatedCount = 0;
            int attemptCount = 0;
            final int MAX_ATTEMPTS_PER_ROUTE = 50; // Límite de intentos para evitar bucles infinitos si hay pocos destinos posibles

            while (generatedCount < routesToGenerate && attemptCount < allCodes.size() * MAX_ATTEMPTS_PER_ROUTE) {
                String destinationCode = (String) allCodes.get(random.nextInt(allCodes.size()));
                int destinationIndex = getIndexForAirportCode(destinationCode);

                int weight = random.nextInt(maxWeight - minWeight + 1) + minWeight;

                if (originIndex != destinationIndex) { // Evitar rutas a sí mismo
                    try {
                        // addEdge ya maneja la no adición de duplicados y la validación de índices
                        this.addEdge(originIndex, destinationIndex, weight);
                        // Si addEdge no lanza excepción y de hecho añadió la arista, incrementamos el contador
                        // Una forma más robusta sería que addEdge retorne un boolean indicando si añadió
                        // o que numEdges se incremente SOLO si la arista fue nueva.
                        // Dado que numEdges se incrementa en addEdge, si no lanza excepción, asumimos que se añadió.
                        generatedCount++; // Incrementa si se intentó añadir una arista válida
                    } catch (IllegalArgumentException | ListException e) {
                        // System.err.println("ADVERTENCIA: Error al añadir ruta aleatoria (" + originCode + " -> " + destinationCode + "): " + e.getMessage());
                        // No incrementamos generatedCount si hubo un error o la arista ya existía
                    }
                }
                attemptCount++;
            }
            if (generatedCount < routesToGenerate) {
                System.out.println("ADVERTENCIA: Solo se generaron " + generatedCount + " rutas para " + originCode + " (se intentaron " + routesToGenerate + ") debido al límite de intentos o falta de destinos válidos.");
            }
        }
        // System.out.println("--- Generación de Rutas Aleatorias Completada. Total de aristas: " + numEdges + " ---");
    }

    /**
     * Calcula la ruta más corta (en términos de peso/duración) entre un aeropuerto de inicio y uno de fin
     * utilizando el algoritmo de Dijkstra.
     *
     * @param startAirportCode El código del aeropuerto de origen.
     * @param endAirportCode El código del aeropuerto de destino.
     * @return La distancia (peso total) de la ruta más corta. Retorna {@code Integer.MAX_VALUE}
     * si no hay una ruta accesible, si el aeropuerto de inicio/fin no existe, o si no hay vértices en el grafo.
     * @throws ListException Si ocurre un error al acceder a los elementos de la lista de adyacencia.
     */
    public int shortestPath(String startAirportCode, String endAirportCode) throws ListException {
        int startIndex = getIndexForAirportCode(startAirportCode);
        int endIndex = getIndexForAirportCode(endAirportCode);

        // Validar que los aeropuertos de inicio y fin existan en el grafo.
        if (startIndex == -1 || endIndex == -1) {
            System.err.println("ERROR: Uno o ambos aeropuertos no existen en el grafo para el cálculo de ruta más corta.");
            return Integer.MAX_VALUE; // No se puede calcular si los aeropuertos no existen
        }
        // Si el origen y el destino son el mismo, la distancia es 0.
        if (startIndex == endIndex) {
            return 0;
        }
        // Si el grafo está vacío
        if (numVertices == 0) {
            return Integer.MAX_VALUE;
        }


        // Array para almacenar las distancias más cortas conocidas desde el origen a cada vértice.
        int[] distances = new int[numVertices];
        Arrays.fill(distances, Integer.MAX_VALUE); // Inicializar todas las distancias a "infinito"

        // Array para marcar si un vértice ya ha sido visitado y su distancia finalizada.
        boolean[] visited = new boolean[numVertices];
        Arrays.fill(visited, false); // Inicializar todos los nodos como no visitados

        // Cola de prioridad para seleccionar el nodo con la distancia más pequeña
        // Permite extraer eficientemente el siguiente vértice a procesar en Dijkstra.
        PriorityQueue<Node> pq = new PriorityQueue<>(numVertices, Comparator.comparingInt(Node::getDistance));

        distances[startIndex] = 0; // La distancia del origen a sí mismo es 0
        pq.add(new Node(startIndex, 0)); // Añadir el nodo de origen a la cola de prioridad

        while (!pq.isEmpty()) {
            int u = pq.poll().getVertex(); // Extraer el nodo (vértice) con la distancia mínima actual

            if (visited[u]) {
                continue; // Si este vértice ya fue procesado, saltar a la siguiente iteración
            }

            visited[u] = true; // Marcar el vértice actual como visitado

            // Si hemos llegado al vértice destino, retornamos su distancia
            if (u == endIndex) {
                return distances[endIndex];
            }

            // Recorrer todos los vecinos (aristas salientes) del vértice actual 'u'
            SinglyLinkedList neighbors = adjList.get(u);
            if (neighbors != null) { // Asegurarse de que hay vecinos
                for (int i = 0; i < neighbors.size(); i++) {
                    int[] edge = (int[]) neighbors.get(i); // Obtener la arista [destino, peso]
                    int v = edge[0]; // Vértice adyacente (destino de la arista)
                    int weight = edge[1]; // Peso de la arista (duración/distancia)

                    // Paso de relajación: si encontramos una ruta más corta a 'v' a través de 'u'
                    if (!visited[v] && distances[u] != Integer.MAX_VALUE && (long)distances[u] + weight < distances[v]) { // (long) para evitar overflow
                        distances[v] = distances[u] + weight; // Actualizar la distancia
                        pq.add(new Node(v, distances[v])); // Añadir/actualizar en la cola de prioridad
                    }
                }
            }
        }

        // Si la cola de prioridad se vacía y no hemos llegado al destino,
        // significa que el destino no es alcanzable desde el origen.
        return distances[endIndex]; // Retorna la distancia final (Integer.MAX_VALUE si no es alcanzable)
    }

    /**
     * Clase interna estática y privada para representar un nodo en la cola de prioridad
     * utilizada por el algoritmo de Dijkstra. Contiene el índice del vértice y la distancia
     * acumulada desde el origen hasta ese vértice.
     */
    private static class Node {
        private final int vertex;
        private final int distance;

        /**
         * Crea una nueva instancia de Node.
         * @param vertex El índice del vértice.
         * @param distance La distancia acumulada a este vértice desde el origen.
         */
        public Node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }

        /**
         * Retorna el índice del vértice.
         * @return El índice del vértice.
         */
        public int getVertex() { return vertex; }

        /**
         * Retorna la distancia acumulada.
         * @return La distancia.
         */
        public int getDistance() { return distance; }
    }

    /**
     * Proporciona una representación en cadena del grafo, mostrando todos los vértices
     * y sus aristas salientes con sus respectivos pesos.
     *
     * @return Una cadena que describe la estructura del grafo.
     */
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
                        String destCode = indexToAirportCodeArray[edge[0]]; // Asume que edge[0] es un índice válido
                        sb.append(destCode).append(" (").append(edge[1]).append(" min)").append(j < connections.size() - 1 ? ", " : "");
                    }
                } catch (ListException e) {
                    sb.append("ERROR: ").append(e.getMessage()); // Manejo de error si la lista falla
                }
            } else {
                sb.append("No hay rutas salientes.");
            }
            sb.append("\n");
        }
        return sb.toString();
    }


}