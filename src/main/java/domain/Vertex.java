package domain;

import domain.list.SinglyLinkedList; // Asegúrate de que esta ruta sea correcta para tu clase SinglyLinkedList

public class Vertex {
    public Object data; // Los datos almacenados en el vértice (ej. nombre del personaje histórico)
    private boolean visited; // Para los algoritmos de recorrido (DFS, BFS)
    public SinglyLinkedList edgesList; // Lista de aristas conectadas a este vértice

    // Constructor
    public Vertex(Object data) {
        this.data = data;
        this.visited = false;
        this.edgesList = new SinglyLinkedList(); // Inicializa la lista de aristas
    }

    // Método para obtener el estado de visitado (para recorridos)
    public boolean isVisited() {
        return visited;
    }

    // Método para establecer el estado de visitado
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    // ¡ESTE ES EL MÉTODO QUE NECESITABAS AÑADIR!
    // Retorna la lista de aristas conectadas a este vértice
    public SinglyLinkedList getEdgesList() {
        return edgesList;
    }

    @Override
    public String toString() {
        return String.valueOf(data); // Convierte los datos del vértice a String para una representación legible
    }

    // Sobrescribe el método equals para comparar vértices por sus datos
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si es el mismo objeto, son iguales
        if (o == null || getClass() != o.getClass()) return false; // Si es nulo o de una clase diferente, no son iguales
        Vertex vertex = (Vertex) o; // Castea el objeto a Vertex
        // Compara los datos de los vértices.
        // Asume que 'data' es un tipo que implementa 'equals' correctamente (como String, Integer, etc.).
        // Si usas una clase de utilidad para comparar objetos genéricos, podrías usarla aquí:
        // return util.Utility.compare(this.data, vertex.data) == 0;
        return this.data.equals(vertex.data);
    }

    // Sobrescribe el método hashCode, lo cual es esencial cuando se sobrescribe equals
    @Override
    public int hashCode() {
        return data.hashCode(); // Genera un código hash basado en los datos del vértice
    }
}