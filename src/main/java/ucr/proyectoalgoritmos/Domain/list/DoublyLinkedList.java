package ucr.proyectoalgoritmos.Domain.list;

import ucr.proyectoalgoritmos.util.Utility; // Your Utility class for compare method

public class DoublyLinkedList implements List {
    private Node first; // Pointer to the beginning of the list
    private Node last;  // NEW: Pointer to the end of the list for O(1) addLast
    private int count;  // NEW: Counter for the number of nodes (for O(1) size())

    // Constructor
    public DoublyLinkedList() {
        this.first = null;
        this.last = null; // Initialize last pointer
        this.count = 0;   // Initialize count
    }

    @Override
    public int size() throws ListException {
        // Now returns the maintained count, making it O(1)
        return count;
    }

    @Override
    public void clear() {
        this.first = null; // Nullifies the list
        this.last = null;  // Nullify last pointer
        this.count = 0;    // Reset count
    }

    @Override
    public boolean isEmpty() {
        // Can also be 'return count == 0;' which is more robust if 'first' isn't perfectly maintained
        return first == null;
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) return true; // Found it
            aux = aux.next; // Move aux to the next node
        }
        return false; // Element not found
    }

    @Override
    public void add(Object element) { // This now acts as addLast(element) efficiently
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = newNode;
            last = newNode; // If empty, first and last are the new node
        } else {
            last.next = newNode; // Link old last node to new node
            newNode.prev = last; // Link new node back to old last
            last = newNode;      // Update last pointer to the new node
        }
        count++; // Increment count
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = newNode;
            last = newNode;
        } else {
            newNode.next = first;
            first.prev = newNode;
            first = newNode;
        }
        count++; // Increment count
    }

    @Override
    public void addLast(Object element) {
        add(element); // Simply call the efficient add method
    }

    @Override
    public void addInSortedList(Object element) {
        // This method is pending implementation. If not used, you can leave it empty or remove it.
    }

    @Override
    public boolean remove(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");

        // Special case: if only one node and it's the one we want to remove
        if (first == last && Utility.compare(first.data, element) == 0) {
            clear();
            return false;
        }

        // Case 1: The element to suppress is the first of the list
        if (Utility.compare(first.data, element) == 0) {
            first = first.next;
            if (first != null) {
                first.prev = null;
            } else { // List became empty
                last = null; // Update last if list is now empty
            }
            count--; // Decrement count
            return false;
        }
        // Case 2. The element can be in the middle or at the end
        Node aux = first.next; // Start from the second node

        while (aux != null && !(Utility.compare(aux.data, element) == 0)) {
            aux = aux.next;
        }

        if (aux != null && Utility.compare(aux.data, element) == 0) {
            // Unlink 'aux' node
            aux.prev.next = aux.next;
            // Maintain double link
            if (aux.next != null) {
                aux.next.prev = aux.prev;
            } else { // aux was the last node
                last = aux.prev; // Update last pointer
            }
            count--; // Decrement count
        } else {
            // Element not found
            throw new ListException("Element " + element + " does not exist in Doubly Linked List for removal.");
        }
        return false;
    }

    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Object value = first.data;
        first = first.next;
        if (first != null) {
            first.prev = null;
        } else { // List became empty
            last = null; // Update last if list is now empty
        }
        count--; // Decrement count
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Doubly Linked List is empty");
        }
        Object value;
        if (first == last) { // If only one node
            value = first.data;
            clear(); // The list becomes empty
        } else {
            value = last.data;
            Node prevNode = last.prev;
            prevNode.next = null;
            last = prevNode; // Update last pointer
        }
        count--; // Decrement count
        return value;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");

        // IMPORTANT: For efficiency, consider using a standard sort algorithm or
        // converting to an array, sorting, and rebuilding the list for large lists.
        // Bubble sort on a linked list is O(N^2) and very slow.
        int listSize = size();
        for (int i = 0; i < listSize - 1; i++) { // Adjusted for 0-based indexing
            for (int j = i + 1; j < listSize; j++) { // Adjusted for 0-based indexing
                Node nodeI = getNode(i); // Get nodes using 0-based index
                Node nodeJ = getNode(j); // Get nodes using 0-based index

                if (Utility.compare(nodeJ.data, nodeI.data) < 0) {
                    Object temp = nodeI.data;
                    nodeI.data = nodeJ.data;
                    nodeJ.data = temp;
                }
            }
        }
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Node aux = first;
        int index = 0; // START AT 0 for 0-based indexing
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) return index;
            index++;
            aux = aux.next;
        }
        return -1; // Element not found
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        return last.data; // Now O(1) thanks to 'last' pointer
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");

        // Special case: if the element is the first, it has no previous in the list
        if (Utility.compare(first.data, element) == 0) {
            throw new ListException("The first element does not have a previous element.");
        }

        Node aux = first; // Start from first, check its next
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                // Since this is a Doubly Linked List, we can directly access aux.prev
                if (aux.prev == null) { // This case should be caught by the first element check
                    throw new ListException("Internal error: Element has no previous but is not first.");
                }
                return aux.prev.data;
            }
            aux = aux.next;
        }
        throw new ListException("Element " + element + " does not exist in Doubly Linked List.");
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Doubly Linked List is empty");
        }
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) {
                if (aux.next == null) { // If it's the last element, it has no next
                    throw new ListException("The last element does not have a next element.");
                }
                return aux.next.data;
            }
            aux = aux.next;
        }
        throw new ListException("Element " + element + " does not exist in Doubly Linked List.");
    }

    public Object get(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        // *** CRITICAL CHANGE: Validate 0-based index ***
        if (index < 0 || index >= count) { // Index must be from 0 to count-1
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        // Loop 'index' times to reach the desired node
        for (int i = 0; i < index; i++) {
            aux = aux.next;
        }
        return aux.data;
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        // *** CRITICAL CHANGE: Validate 0-based index ***
        if (index < 0 || index >= count) { // Index must be from 0 to count-1
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        // Loop 'index' times to reach the desired node
        for (int i = 0; i < index; i++) {
            aux = aux.next;
        }
        return aux;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Doubly Linked List is empty";
        String result = "Doubly Linked List Content\n";
        Node aux = first;
        while (aux != null) {
            result += aux.data + "\n"; // Added newline for readability
            aux = aux.next;
        }
        return result;
    }


}