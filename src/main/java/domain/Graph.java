package domain;

import domain.list.ListException;
import domain.queue.QueueException;
import domain.stack.StackException;

public interface Graph {
    // devuelve el número de vértices que tiene el grafo
    public int size() throws ListException;
    //elimina todo el Grafo
    public void clear();
    // true si el grafo está vacío
    public boolean isEmpty();
    // true si el vértice indicado forma parte del grafo
    public boolean containsVertex(Object element)throws GraphException, ListException;
    // true si existe una artista que une los dos vértices indicados
    public boolean containsEdge(Object a, Object b)throws GraphException, ListException;
    //agrega un vértice al grafo
    public void addVertex(Object element)throws GraphException, ListException;
    //agrega una artista que permita unir dos vértices (el grafo es no dirigido)
    public void addEdge(Object a, Object b)throws GraphException, ListException;
    //agrega peso a una artista que une dos vértices (el grafo es no dirigido)
    public void addWeight(Object a, Object b, Object weight)throws GraphException, ListException;
    //agrega una arista y un peso entre dos vértices
    public void addEdgeWeight(Object a, Object b, Object weight)throws GraphException, ListException;
    //suprime el vertice indicado. Si tiene aristas asociadas, tambien seran suprimidas
    public void removeVertex(Object element)throws GraphException, ListException;
    //suprime la arista(si existe) entre los vertices a y b
    public void removeEdge(Object a, Object b)throws GraphException, ListException;
    //recorre el grafo utilizando el algoritmo de búsqueda en profundidad
    //depth-first search
    public String dfs()throws GraphException, StackException, ListException;
    //recorre el grafo utilizando el algoritmo de búsqueda en amplitud
    //breadth-first search
    public String bfs()throws GraphException, QueueException, ListException;
}
