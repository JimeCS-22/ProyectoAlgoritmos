package ucr.proyectoalgoritmos.Domain.dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Dijkstra {

    // Returns shortest distances from src to all other vertices
    public static int[] dijkstra(int V, int[][] edges, int src) { // Changed to public static

        // Create adjacency list
        ArrayList<ArrayList<ArrayList<Integer>>> adj =
                constructAdj(edges, V);

        // PriorityQueue to store vertices to be processed
        // Each element is a pair: [distance, node]
        PriorityQueue<ArrayList<Integer>> pq =
                new PriorityQueue<>(Comparator.comparingInt(a -> a.get(0)));

        // Create a distance array and initialize all distances as infinite
        int[] dist = new int[V];
        Arrays.fill(dist, Integer.MAX_VALUE);

        // Insert source with distance 0
        dist[src] = 0;
        ArrayList<Integer> start = new ArrayList<>();

        start.add(0);
        start.add(src);
        pq.offer(start);

        // Loop until the priority queue is empty
        while (!pq.isEmpty()) {

            // Get the node with the minimum distance
            ArrayList<Integer> curr = pq.poll();
            int d = curr.get(0);
            int u = curr.get(1);

            // If we've already found a shorter path, skip
            if (d > dist[u]) {
                continue;
            }

            // Traverse all adjacent vertices of the current node
            for (ArrayList<Integer> neighbor : adj.get(u)) {
                int v = neighbor.get(0);
                int weight = neighbor.get(1);

                // If there is a shorter path to v through u
                if (dist[v] > dist[u] + weight) {
                    // Update distance of v
                    dist[v] = dist[u] + weight;

                    // Add updated pair to the queue
                    ArrayList<Integer> temp = new ArrayList<>();
                    temp.add(dist[v]);
                    temp.add(v);
                    pq.offer(temp);
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
            int u = edge[0]; // Source vertex
            int v = edge[1]; // Target vertex
            int weight = edge[2]; // Weight of the edge

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