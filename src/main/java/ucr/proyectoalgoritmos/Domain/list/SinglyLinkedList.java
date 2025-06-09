package ucr.proyectoalgoritmos.Domain.list;

import ucr.proyectoalgoritmos.util.Utility;

public class SinglyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista
    private int count; // Maintain a count for O(1) size()

    public SinglyLinkedList() {
        this.first = null; //la lista no existe
        this.count = 0;    // Initialize count
    }

    @Override
    public int size() { // No ListException here, returns actual size
        return this.count; // O(1) operation
    }

    @Override
    public void clear() {
        this.first = null; //anulamos la lista
        this.count = 0;    // Reset count
    }

    @Override
    public boolean isEmpty() {
        return this.first == null; //si es nulo está vacía
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty()) {
            // It's generally better to return false for contains if empty,
            // or let the caller handle it. Throwing here can be problematic.
            // For now, keeping your original behavior, but consider just returning false.
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
        count++; // Increment count on add
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
        count++; // Increment count on add
    }

    @Override
    public void addLast(Object element) {
        add(element); // calls add(), which increments count
    }

    @Override
    public void addInSortedList(Object element) {
        // Method not implemented yet.
        // If implemented, ensure count is incremented.
    }

    @Override
    public boolean remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        //Case 1. The element to delete is at the beginning
        if (Utility.compare(first.data, element) == 0) {
            first = first.next; //jump the first node
            count--; // Decrement count on removal
        } else {  //Case 2. The element to delete can be in the middle or end
            Node prev = first; //pointer to the previous node
            Node aux = first.next;
            while (aux != null && !(Utility.compare(aux.data, element) == 0)) {
                prev = aux;
                aux = aux.next;
            }
            //exits when it reaches null or finds the element
            if (aux != null && Utility.compare(aux.data, element) == 0) {
                //found it, proceed to unlink the node
                prev.next = aux.next;
                count--; // Decrement count on removal
            }
        }
        return false;
    }

    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Object removedData = first.data;
        first = first.next;
        count--; // Decrement count on removal
        return removedData;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        if (size() == 1) { // If only one element
            Object removedData = first.data;
            clear(); // Resets first to null and count to 0
            return removedData;
        }
        Node aux = first;
        // Iterate until aux is the second to last node
        while (aux.next != null && aux.next.next != null) {
            aux = aux.next;
        }
        Object removedData = aux.next.data;
        aux.next = null; // Disconnect the last node
        count--; // Decrement count on removal
        return removedData;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        // Bubble sort implementation for demonstration
        // Directly swap data within nodes using getNode.
        for (int i = 0; i < count - 1; i++) { // Outer loop: up to second to last element
            for (int j = i + 1; j < count; j++) { // Inner loop: from i+1 to last element
                Node nodeI = getNode(i);
                Node nodeJ = getNode(j);

                // Use Utility.compare for comparison
                if (Utility.compare(nodeJ.data, nodeI.data) < 0) {
                    // Swap data
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
            return null; // First element has no previous
        }
        Node aux = first;
        while (aux.next != null) {
            if (Utility.compare(aux.next.data, element) == 0) {
                return aux.data; // Return data of the current node (which is previous to element)
            }
            aux = aux.next;
        }
        return null; // Element not found or it's the first element.
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
                    return null; // It's the last, no next
                }
            }
            aux = aux.next;
        }
        return null; // Element does not exist
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        // Validate index to be within [0, size()-1]
        if (index < 0 || index >= count) { // Use 'count' for size check
            throw new ListException("Invalid index: " + index + ", Size: " + count);
        }
        Node aux = first;
        int i = 0; // pos del primer nodo (0-indexed)
        while (aux != null) {
            if (i == index) {  //ya encontro el indice
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
        // Corrected: index >= count is the proper upper bound check for 0-indexed lists
        if (index < 0 || index >= count) { // Use 'count' for size check
            throw new ListException("Invalid index: " + index + ", Size: " + count);
        }

        Node aux = first;
        int i = 0;
        while (aux != null) {
            if (i == index) {
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

