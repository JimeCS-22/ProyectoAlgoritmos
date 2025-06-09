package ucr.proyectoalgoritmos.Domain.dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Dijkstra {

    // Constants for clarity when accessing elements in ArrayLists
    private static final int DISTANCE_OR_TARGET_INDEX = 0;
    private static final int NODE_OR_WEIGHT_INDEX = 1;

    // Returns shortest distances from src to all other vertices
    public static int[] dijkstra(int V, int[][] edges, int src) {
        // Input validation
        if (V <= 0) {
            // A graph with 0 or negative vertices is not valid for Dijkstra
            return new int[0]; // Return an empty array or throw an exception
        }
        if (src < 0 || src >= V) {
            throw new IllegalArgumentException("Source vertex " + src + " is out of bounds for V=" + V);
        }
        if (edges == null) {
            throw new IllegalArgumentException("Edges array cannot be null.");
        }

        // Create adjacency list
        ArrayList<ArrayList<ArrayList<Integer>>> adj =
                constructAdj(edges, V);

        // PriorityQueue to store vertices to be processed
        // Each element is a pair: [distance, node]
        PriorityQueue<ArrayList<Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(a -> a.get(DISTANCE_OR_TARGET_INDEX))); // Use constant

        // Create a distance array and initialize all distances as infinite
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);

        // Insert source with distance 0
        dist[src] = 0;
        ArrayList<Integer> start = new ArrayList<>();
        start.add(0); // Distance
        start.add(src); // Node
        pq.offer(start);

        // Loop until the priority queue is empty
        while (!pq.isEmpty()) {

            // Get the node with the minimum distance
            ArrayList<Integer> curr = pq.poll();
            int d = curr.get(DISTANCE_OR_TARGET_INDEX); // Use constant
            int u = curr.get(NODE_OR_WEIGHT_INDEX);     // Use constant

            // If we've already found a shorter path, skip (optimization)
            if (d > dist[u]) {
                continue;
            }

            // Traverse all adjacent vertices of the current node
            // Check if adj.get(u) is not null and iterate
            if (u >= 0 && u < adj.size() && adj.get(u) != null) {
                for (ArrayList<Integer> neighbor : adj.get(u)) {
                    int v = neighbor.get(DISTANCE_OR_TARGET_INDEX); // Use constant (target node)
                    int weight = neighbor.get(NODE_OR_WEIGHT_INDEX);     // Use constant

                    // If there is a shorter path to v through u
                    // Be careful with Integer.MAX_VALUE + weight, it can overflow.
                    // This is a common pattern to avoid overflow:
                    if (dist[u] != Integer.MAX_VALUE && dist[v] > dist[u] + weight) {
                        // Update distance of v
                        dist[v] = dist[u] + weight;

                        // Add updated pair to the queue
                        ArrayList<Integer> temp = new ArrayList<>();
                        temp.add(dist[v]); // Updated distance
                        temp.add(v);      // Node
                        pq.offer(temp);
                    }
                }
            }
        }

        // Return the shortest distance array
        return dist;
    }

    /**
     * Constructs an adjacency list from a given list of edges.
     * The adjacency list will be represented as:
     * ArrayList<ArrayList<ArrayList<Integer>>> where the outer ArrayList
     * represents the source nodes, the middle ArrayList represents the
     * list of neighbors for that source node, and the innermost ArrayList
     * contains [target_node, weight].
     *
     * @param edges A 2D array where each inner array is [source, target, weight].
     * @param V The total number of vertices in the graph.
     * @return The adjacency list representation of the graph.
     */
    private static ArrayList<ArrayList<ArrayList<Integer>>> constructAdj(int[][] edges, int V) {
        ArrayList<ArrayList<ArrayList<Integer>>> adj = new ArrayList<>();
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }

        for (int[] edge : edges) {
            // Basic validation for edge format
            if (edge == null || edge.length != 3) {
                System.err.println("Skipping malformed edge: " + Arrays.toString(edge));
                continue;
            }

            int u = edge[0]; // Source vertex
            int v = edge[1]; // Target vertex
            int weight = edge[2]; // Weight of the edge

            // Validate vertex indices
            if (u < 0 || u >= V || v < 0 || v >= V) {
                System.err.println("Skipping edge with out-of-bounds vertices: " + Arrays.toString(edge));
                continue;
            }

            // Add edge from u to v
            ArrayList<Integer> uToV = new ArrayList<>();
            uToV.add(v);
            uToV.add(weight);
            adj.get(u).add(uToV);

            // Add edge from v to u (only for undirected graphs)
            // Uncomment the following lines if your graph is undirected
            /*
            ArrayList<Integer> vToU = new ArrayList<>();
            vToU.add(u);
            vToU.add(weight);
            adj.get(v).add(vToU);
            */
        }
        return adj;
    }
}