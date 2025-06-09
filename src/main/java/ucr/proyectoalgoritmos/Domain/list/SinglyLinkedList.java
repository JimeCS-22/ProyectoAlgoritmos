package ucr.proyectoalgoritmos.Domain.list;

import ucr.proyectoalgoritmos.util.Utility;

public class SinglyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista

    public SinglyLinkedList() {
        this.first = null; //la lista no existe
    }

    @Override
    public int size() throws ListException {
        // --- CRUCIAL CHANGE HERE ---
        // A list's size() method should return 0 if it's empty, not throw an exception.
        if (isEmpty()) {
            return 0;
        }
        // --- END CRUCIAL CHANGE ---

        Node aux = first;
        int count = 0;
        while (aux != null) {
            count++;
            aux = aux.next; //lo movemos al sgte nodo
        }
        return count;
    }

    @Override
    public void clear() {
        this.first = null; //anulamos la lista
    }

    @Override
    public boolean isEmpty() {
        return this.first == null; //si es nulo está vacía
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is empty");
        }
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                return true;
            }
            aux = aux.next; //lo movemos al sgte nodo
        }
        return false; //indica q el elemento no existe
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = newNode;
        } else {
            Node aux = first;
            //mientras no llegue al ult nodo
            while (aux.next != null) {
                aux = aux.next;
            }
            //una vez que se sale del while, quiere decir q
            //aux esta en el ult nodo, por lo q lo podemos enlazar
            //con el nuevo nodo
            aux.next = newNode;
        }
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = newNode;
        } else {
            newNode.next = first;
            first = newNode;
        }
    }

    @Override
    public void addLast(Object element) {
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {
        // Method not implemented yet.
    }

    @Override
    public void remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        //Caso 1. El elemento a suprimir esta al inicio
        if (Utility.compare(first.data, element) == 0) {
            first = first.next; //saltamos el primer nodo
        } else {  //Caso 2. El elemento a suprimir puede estar al medio o final
            Node prev = first; //dejo un apuntador al nodo anterior
            Node aux = first.next;
            while (aux != null && !(Utility.compare(aux.data, element) == 0)) {
                prev = aux;
                aux = aux.next;
            }
            //se sale cuando alcanza nulo o cuando encuentra el elemento
            if (aux != null && Utility.compare(aux.data, element) == 0) {
                //ya lo encontro, procedo a desenlazar el nodo
                prev.next = aux.next;
            }
        }
    }

    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Object removedData = first.data;
        first = first.next;
        return removedData;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        if (size() == 1) { // If only one element
            Object removedData = first.data;
            clear();
            return removedData;
        }
        Node aux = first;
        while (aux.next.next != null) { // Stop at the second to last node
            aux = aux.next;
        }
        Object removedData = aux.next.data;
        aux.next = null; // Disconnect the last node
        return removedData;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        // Bubble sort implementation for demonstration
        for (int i = 0; i < size(); i++) { // Changed <= size() to < size() for correct iteration
            for (int j = i + 1; j < size(); j++) { // Changed <= size() to < size()
                // Use getNode(int index).data for comparisons and swaps
                if (Utility.compare(get(j), get(i)) < 0) { // Changed getNode(j).data to get(j)
                    Object auxData = get(i); // Get data from node at i
                    // Now, you need to find the actual nodes to swap their data,
                    // or swap data using get/set if available (which you don't have a set method here).
                    // A direct swap of node.data is better.
                    Node nodeI = getNode(i);
                    Node nodeJ = getNode(j);

                    Object temp = nodeI.data;
                    nodeI.data = nodeJ.data;
                    nodeJ.data = temp;
                }
            }
        }
    }


    @Override
    public int indexOf(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        int index = 0; //la lista inicia en 0 (common for indices)
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                return index;
            }
            index++; //incremento el indice
            aux = aux.next; //muevo aux al sgte nodo
        }
        return -1; //indica q el elemento no existe
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        //mientras no llegue al ult nodo
        while (aux.next != null) {
            aux = aux.next;
        }
        //se sale del while cuando aux esta en el ult nodo
        return aux.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        if (Utility.compare(first.data, element) == 0) {
            return "It's the first, it has no previous"; // Consider returning null or throwing specific exception
        }
        Node aux = first;
        //mientras no llegue al ult nodo
        while (aux.next != null) {
            if (Utility.compare(aux.next.data, element) == 0) {
                return aux.data; //retornamos la data del nodo actual
            }
            aux = aux.next;
        }
        return "Does not exist in Single Linked List"; // Consider returning null or throwing specific exception
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                if (aux.next != null) {
                    return aux.next.data;
                } else {
                    return "It's the last, it has no next"; // Consider returning null
                }
            }
            aux = aux.next;
        }
        return "Element does not exist in Single Linked List"; // Consider returning null
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        // Validate index to be within [0, size()-1]
        if (index < 0 || index >= size()) {
            throw new ListException("Invalid index: " + index);
        }
        Node aux = first;
        int i = 0; // pos del primer nodo (0-indexed)
        while (aux != null) {
            if (Utility.compare(i, index) == 0) {  //ya encontro el indice
                return aux;
            }
            i++; //incremento la var local
            aux = aux.next; //muevo aux al sgte nodo
        }
        return null; // Should not be reached if index validation is correct
    }

    public Node getNode(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {  //ya encontro el elemento
                return aux;
            }
            aux = aux.next; //muevo aux al sgte nodo
        }
        return null; //si llega aqui es xq no encontro el index
    }

    @Override
    public String toString() {
        String result = "";
        Node aux = first;
        while (aux != null) {
            result += "\n" + aux.data;
            aux = aux.next;
        }
        return result;
    }


    public Object get(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        // --- CRUCIAL CHANGE HERE ---
        // Corrected: index >= size() is the proper upper bound check for 0-indexed lists
        if (index < 0 || index >= size()) {
            throw new ListException("Invalid index: " + index);
        }
        // --- END CRUCIAL CHANGE ---

        Node aux = first;
        int i = 0;
        while (aux != null) {
            if (Utility.compare(i, index) == 0) {
                return aux.data;
            }
            i++;
            aux = aux.next;
        }
        return null; // Should not be reached if index validation is correct
    }

    public Node getFirstNode() {
        return this.first;
    }
}