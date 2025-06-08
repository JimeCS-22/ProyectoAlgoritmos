package domain;

import domain.list.ListException;
import domain.list.Node;
import domain.list.SinglyLinkedList;
import domain.queue.LinkedQueue;
import domain.queue.QueueException;
import domain.stack.LinkedStack;
import domain.stack.StackException;

public class SinglyLinkedListGraph implements Graph {
    private SinglyLinkedList vertexList; //lista enlazada de vértices

    private LinkedStack stack;
    private LinkedQueue queue;

    //Constructor
    public SinglyLinkedListGraph() {
        this.vertexList = new SinglyLinkedList();
        this.stack = new LinkedStack();
        this.queue = new LinkedQueue();
    }

    @Override
    public int size() throws ListException {
        return vertexList.size();
    }

    @Override
    public void clear() {
        this.vertexList = new SinglyLinkedList();
    }

    @Override
    public boolean isEmpty() {
        return vertexList.isEmpty();
    }

    @Override
    public boolean containsVertex(Object element) throws GraphException, ListException {
        if (isEmpty())
            return false;
        return indexOf(element) != -1;
    }

    @Override
    public boolean containsEdge(Object a, Object b) throws GraphException, ListException {
        if (isEmpty())
            return false;
        int index = indexOf(a);
        if (index == -1) return false;
        Vertex vertex = (Vertex) vertexList.getNode(index).data;
        return vertex != null && vertex.edgesList != null && !vertex.edgesList.isEmpty()
                && vertex.edgesList.contains(new EdgeWeight(b, null));
    }

    @Override
    public void addVertex(Object element) throws GraphException, ListException {
        if (!containsVertex(element))
            vertexList.add(new Vertex(element));
    }

    @Override
    public void addEdge(Object a, Object b) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes [" + a + "] y [" + b + "]: one or both not found.");
        if (!containsEdge(a, b)) {
            addRemoveVertexEdgeWeight(a, b, null, "addEdge");
            addRemoveVertexEdgeWeight(b, a, null, "addEdge");
        }
    }

    public int indexOf(Object element) throws ListException {
        if (isEmpty()) return -1;
        for (int i = 1; i <= vertexList.size(); i++) {
            Vertex vertex = (Vertex) vertexList.getNode(i).data;
            if (util.Utility.compare(vertex.data, element) == 0) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if (!containsEdge(a, b))
            throw new GraphException("There is no edge between the vertexes[" + a + "] y [" + b + "]");
        addRemoveVertexEdgeWeight(a, b, weight, "addWeight");
        addRemoveVertexEdgeWeight(b, a, weight, "addWeight");
    }

    @Override
    public void addEdgeWeight(Object a, Object b, Object weight) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b))
            throw new GraphException("Cannot add edge between vertexes [" + a + "] y [" + b + "]");
        if (!containsEdge(a, b)) {
            addRemoveVertexEdgeWeight(a, b, weight, "addEdge");
            addRemoveVertexEdgeWeight(b, a, weight, "addEdge");
        } else {
            addWeight(a, b, weight);
        }
    }

    @Override
    public void removeVertex(Object element) throws GraphException, ListException {
        if (isEmpty())
            throw new GraphException("Singly Linked List Graph is Empty");

        int removedIndex = indexOf(element);
        if (removedIndex == -1) {
            return; // Vértice no encontrado, no hay nada que eliminar
        }

        vertexList.remove(new Vertex(element));

        int currentVertexListSize = vertexList.size(); // Obtener el tamaño después de la posible eliminación del vértice

        for (int j = 1; j <= currentVertexListSize; j++) {
            Vertex currentVertex = (Vertex) vertexList.getNode(j).data;
            if (currentVertex != null && currentVertex.edgesList != null && !currentVertex.edgesList.isEmpty()) {

                // Intenta remover la arista que apunta al 'element' (el vértice que se eliminó)
                currentVertex.edgesList.remove(new EdgeWeight(element, null));
            }
        }
    }

    @Override
    public void removeEdge(Object a, Object b) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b))
            throw new GraphException("There's no some of the vertexes");
        addRemoveVertexEdgeWeight(a, b, null, "remove");
        addRemoveVertexEdgeWeight(b, a, null, "remove");
    }

    private void addRemoveVertexEdgeWeight(Object a, Object b, Object weight, String action) throws ListException {
        int indexA = indexOf(a);
        if (indexA == -1) {
            throw new ListException("Vertex '" + a + "' not found during edge operation.");
        }
        Vertex vertex = (Vertex) vertexList.getNode(indexA).data;

        if (vertex.edgesList == null) {
            vertex.edgesList = new SinglyLinkedList();
        }

        switch (action) {
            case "addEdge":
                if (!vertex.edgesList.contains(new EdgeWeight(b, null))) {
                    vertex.edgesList.add(new EdgeWeight(b, weight));
                }
                break;
            case "addWeight":
                domain.list.Node edgeNode = vertex.edgesList.getNode(new EdgeWeight(b, null));
                if (edgeNode != null) {
                    ((EdgeWeight) edgeNode.getData()).setWeight(weight);
                }
                break;
            case "remove":
                if (vertex.edgesList != null && !vertex.edgesList.isEmpty())
                    vertex.edgesList.remove(new EdgeWeight(b, weight));
                break;
        }
    }

    @Override
    public String dfs() throws GraphException, StackException, ListException {
        if (isEmpty()) throw new GraphException("Singly Linked List Graph is Empty for DFS");
        setVisited(false);
        Vertex vertex = (Vertex) vertexList.getNode(1).data;
        String info = vertex.data + ", ";
        vertex.setVisited(true);
        stack.clear();
        stack.push(1);
        while (!stack.isEmpty()) {
            int index = adjacentVertexNotVisited((int) stack.top());
            if (index == -1)
                stack.pop();
            else {
                vertex = (Vertex) vertexList.getNode(index).data;
                vertex.setVisited(true);
                info += vertex.data + ", ";
                stack.push(index);
            }
        }
        return info;
    }//dfs

    @Override
    public String bfs() throws GraphException, QueueException, ListException {
        if (isEmpty()) throw new GraphException("Singly Linked List Graph is Empty for BFS");
        setVisited(false);
        Vertex vertex = (Vertex) vertexList.getNode(1).data;
        String info = vertex.data + ", ";
        vertex.setVisited(true);
        queue.clear();
        queue.enQueue(1);
        int index2;
        while (!queue.isEmpty()) {
            int index1 = (int) queue.deQueue();
            while ((index2 = adjacentVertexNotVisited(index1)) != -1) {
                vertex = (Vertex) vertexList.getNode(index2).data;
                vertex.setVisited(true);
                info += vertex.data + ", ";
                queue.enQueue(index2);
            }
        }
        return info;
    }

    private void setVisited(boolean value) throws ListException {
        if (isEmpty()) return;
        for (int i = 1; i <= vertexList.size(); i++) {
            Vertex vertex = (Vertex) vertexList.getNode(i).data;
            vertex.setVisited(value);
        }
    }

    private int adjacentVertexNotVisited(int index) throws ListException {
        if (isEmpty()) return -1;
        Vertex vertex1 = (Vertex) vertexList.getNode(index).data;
        for (int i = 1; i <= vertexList.size(); i++) {
            Vertex vertex2 = (Vertex) vertexList.getNode(i).data;
            if (util.Utility.compare(vertex1.data, vertex2.data) != 0 &&
                    !vertex2.edgesList.isEmpty() && vertex2.edgesList
                    .contains(new EdgeWeight(vertex1.data, null))
                    && !vertex2.isVisited())
                return i;
        }
        return -1;
    }

    @Override
    public String toString() {
        String result = "";
        try {
            if (isEmpty()) return "(Graph is empty)\n";

            for (int i = 1; i <= vertexList.size(); i++) {
                Vertex vertex = (Vertex) vertexList.getNode(i).data;
                result += "The vertex in position " + i + " is: " + vertex.data + "\n";

                if (vertex.edgesList != null && !vertex.edgesList.isEmpty()) {
                    result += "        EDGES AND WEIGHTS: ";
                    // CAMBIO AQUI: Usa getFirstNode() para obtener el objeto Node
                    domain.list.Node currentEdgeNode = vertex.edgesList.getFirstNode(); // AHORA ES CORRECTO
                    while (currentEdgeNode != null) {
                        EdgeWeight ew = (EdgeWeight) currentEdgeNode.getData();
                        result += "[" + ew.getEdge() + ", " + ew.getWeight() + "] ";
                        currentEdgeNode = currentEdgeNode.next;
                    }
                    result += "\n";
                }
            }
        } catch (ListException ex) {
            System.err.println("Error in toString: " + ex.getMessage());
            return "Error rendering graph content: " + ex.getMessage();
        }
        return result;
    }

    public SinglyLinkedList getVertexList() {
        return this.vertexList;
    }

    public Object getVertexDataByIndex(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Graph is empty, cannot get vertex by index.");
        }
        if (index < 1 || index > vertexList.size()) {
            throw new ListException("Index out of bounds: " + index);
        }
        Vertex vertex = (Vertex) vertexList.getNode(index).data;
        return vertex.data;
    }

    public Object getEdgeWeight(Object a, Object b) throws GraphException, ListException {
        if (!containsVertex(a) || !containsVertex(b)) {
            throw new GraphException("Cannot get edge weight: one or both vertices not found.");
        }
        int indexA = indexOf(a);
        Vertex vertexA = (Vertex) vertexList.getNode(indexA).data;

        if (vertexA != null && vertexA.edgesList != null) {
            domain.list.Node edgeNode = vertexA.edgesList.getNode(new EdgeWeight(b, null));
            if (edgeNode != null) {
                return ((EdgeWeight) edgeNode.getData()).getWeight();
            }
        }
        return null;
    }
}