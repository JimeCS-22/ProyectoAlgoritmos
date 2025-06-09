package ucr.proyectoalgoritmos.Domain.list;

import ucr.proyectoalgoritmos.util.Utility;

public class SinglyLinkedList implements List {
    private Node first; // Pointer to the start of the list
    private int count; // Maintain a count for O(1) size()

    public SinglyLinkedList() {
        this.first = null; // The list is empty
        this.count = 0;    // Initialize count
        System.out.println("SLL DEBUG: New list created. State: first=" + (first != null ? "not null" : "null") + ", count=" + count);
    }

    @Override
    public int size() {
        // No debug print needed here as it's a simple getter
        return this.count; // O(1) operation
    }

    @Override
    public void clear() {
        System.out.println("SLL DEBUG: clear() called. Before: count=" + count);
        this.first = null; // Nullify the list
        this.count = 0;    // Reset count
        System.out.println("SLL DEBUG: clear() completed. After: count=" + count);
    }

    @Override
    public boolean isEmpty() {
        boolean empty = (this.first == null);
        // CRITICAL INCONSISTENCY CHECK
        if (empty && this.count != 0) {
            System.err.println("SLL CRITICAL INCONSISTENCY: isEmpty() is true (first=null) but count is " + this.count + "!");
        } else if (!empty && this.count == 0) {
            System.err.println("SLL CRITICAL INCONSISTENCY: isEmpty() is false (first=" + first.data + ") but count is 0!");
        }
        return empty;
    }

    @Override
    public boolean contains(Object element) throws ListException {
        // Avoid throwing ListException if list is empty for 'contains'.
        // It's more conventional for 'contains' to simply return false if the collection is empty.
        // The ListException check is redundant if isEmpty() is correctly implemented.
        if (isEmpty()) {
            return false;
        }
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                return true;
            }
            aux = aux.next;
        }
        return false;
    }

    @Override
    public void add(Object element) {
        System.out.println("SLL DEBUG: add(" + element + ") called. Before: count=" + count);
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = newNode;
        } else {
            Node aux = first;
            while (aux.next != null) {
                aux = aux.next;
            }
            aux.next = newNode;
        }
        count++; // Increment count on add
        System.out.println("SLL DEBUG: add(" + element + ") completed. After: first=" + (first != null ? first.data : "null") + ", count=" + count);
    }

    @Override
    public void addFirst(Object element) {
        System.out.println("SLL DEBUG: addFirst(" + element + ") called. Before: count=" + count);
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = newNode;
        } else {
            newNode.next = first;
            first = newNode;
        }
        count++; // Increment count on add
        System.out.println("SLL DEBUG: addFirst(" + element + ") completed. After: first=" + (first != null ? first.data : "null") + ", count=" + count);
    }

    @Override
    public void addLast(Object element) {
        // This method already calls add(), which correctly handles count.
        // We'll call add() which has the O(n) traversal for the last node anyway.
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {
        // Method not implemented yet. If implemented, ensure count is incremented and first/last pointers are handled correctly.
        // For now, if called, it should throw an exception or be implemented.
        throw new UnsupportedOperationException("addInSortedList not yet implemented.");
    }

    @Override
    public boolean remove(Object element) throws ListException {
        System.out.println("SLL DEBUG: remove(" + element + ") called. Before: first=" + (first != null ? first.data : "null") + ", count=" + count);
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }

        // Case 1: The element to delete is at the beginning
        if (Utility.compare(first.data, element) == 0) {
            Object removedData = first.data; // Store data to print in debug
            first = first.next; // Move first to the next node
            count--; // Decrement count

            // CRITICAL FIX: If the list becomes empty after removing the first element
            if (count == 0) {
                first = null; // Ensure first is null when list is truly empty
            }
            System.out.println("SLL DEBUG: remove(" + removedData + ") (first) completed. After: first=" + (first != null ? first.data : "null") + ", count=" + count);
            return true; // Element was found and removed
        }

        // Case 2: The element to delete is in the middle or at the end
        Node prev = first; // Pointer to the previous node
        Node aux = first.next; // Pointer to the current node (starts from second node)

        while (aux != null && Utility.compare(aux.data, element) != 0) {
            prev = aux;
            aux = aux.next;
        }

        // Exits loop when it reaches null or finds the element
        if (aux != null && Utility.compare(aux.data, element) == 0) {
            // Element found, proceed to unlink the node
            Object removedData = aux.data; // Store data to print in debug
            prev.next = aux.next;
            count--; // Decrement count

            System.out.println("SLL DEBUG: remove(" + removedData + ") (middle/last) completed. After: first=" + (first != null ? first.data : "null") + ", count=" + count);
            return true; // Element was found and removed
        }

        System.out.println("SLL DEBUG: remove(" + element + ") - Element not found. After: first=" + (first != null ? first.data : "null") + ", count=" + count);
        return false; // Element not found
    }

    @Override
    public Object removeFirst() throws ListException {
        System.out.println("SLL DEBUG: removeFirst() called. Before: first=" + (first != null ? first.data : "null") + ", count=" + count);
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Object removedData = first.data;
        first = first.next;
        count--; // Decrement count

        // CRITICAL FIX: If list becomes empty after removing the first element
        if (count == 0) {
            first = null; // Ensure first is null when list is truly empty
        }
        System.out.println("SLL DEBUG: removeFirst() completed. Removed: " + removedData + ". After: first=" + (first != null ? first.data : "null") + ", count=" + count);
        return removedData;
    }

    @Override
    public Object removeLast() throws ListException {
        System.out.println("SLL DEBUG: removeLast() called. Before: first=" + (first != null ? first.data : "null") + ", count=" + count);
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        if (size() == 1) { // If only one element
            Object removedData = first.data;
            clear(); // This correctly sets first=null and count=0
            System.out.println("SLL DEBUG: removeLast() (single element) completed. Removed: " + removedData + ". After: first=" + (first != null ? first.data : "null") + ", count=" + count);
            return removedData;
        }
        Node aux = first;
        // Iterate until aux.next is the last node
        while (aux.next != null && aux.next.next != null) {
            aux = aux.next;
        }
        Object removedData = aux.next.data;
        aux.next = null; // Disconnect the last node
        count--; // Decrement count on removal
        System.out.println("SLL DEBUG: removeLast() completed. Removed: " + removedData + ". After: first=" + (first != null ? first.data : "null") + ", count=" + count);
        return removedData;
    }

    @Override
    public void sort() throws ListException {
        System.out.println("SLL DEBUG: sort() called. Before: " + this.toString().replace("\n", ", "));
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        // Bubble sort implementation
        for (int i = 0; i < count - 1; i++) {
            for (int j = i + 1; j < count; j++) {
                Node nodeI = getNode(i); // This calls get(int index) which is O(n)
                Node nodeJ = getNode(j); // This calls get(int index) which is O(n)

                // This makes the sort O(n^3). For production, consider converting to array, sorting, then re-creating.
                // For now, it's functional.
                if (Utility.compare(nodeJ.data, nodeI.data) < 0) {
                    // Swap data
                    Object temp = nodeI.data;
                    nodeI.data = nodeJ.data;
                    nodeJ.data = temp;
                }
            }
        }
        System.out.println("SLL DEBUG: sort() completed. After: " + this.toString().replace("\n", ", "));
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        int index = 0;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                return index;
            }
            index++;
            aux = aux.next;
        }
        return -1; // Element not found
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
        while (aux.next != null) {
            aux = aux.next;
        }
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
                return aux.data;
            }
            aux = aux.next;
        }
        return null; // Element not found
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
        if (index < 0 || index >= count) { // Use 'count' for upper bound check
            throw new ListException("Invalid index: " + index + ", Size: " + count);
        }
        Node aux = first;
        for (int i = 0; i < index; i++) {
            aux = aux.next;
        }
        return aux;
    }

    public Node getNode(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                return aux;
            }
            aux = aux.next;
        }
        return null; // Element not found
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "SinglyLinkedList (empty)";
        }
        StringBuilder result = new StringBuilder("SinglyLinkedList (size=" + count + "):");
        Node aux = first;
        while (aux != null) {
            result.append("\n  ").append(aux.data);
            aux = aux.next;
        }
        return result.toString();
    }

     // This is an override, but your List interface doesn't define it. Check your List interface.
    public Object get(int index) throws ListException {
        // This method also internally uses getNode(index), but let's keep it robust
        return getNode(index).data; // getNode already handles empty and index bounds
    }

    public Node getFirstNode() {
        return this.first;
    }
}