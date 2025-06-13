package ucr.proyectoalgoritmos.Domain.Circular;

import ucr.proyectoalgoritmos.util.Utility;
import ucr.proyectoalgoritmos.Domain.list.List; // Assuming your List interface is here
import ucr.proyectoalgoritmos.Domain.list.Node; // Assuming your Node class is here
import ucr.proyectoalgoritmos.Domain.list.ListException; // Assuming your ListException is here


public class CircularLinkedList implements List {

    private Node first; // Pointer to the first node
    private Node last;  // Pointer to the last node
    private int count;  // New: Counter for number of nodes (O(1) size())

    // Constructor
    public CircularLinkedList(){
        this.first = this.last = null;
        this.count = 0; // Initialize count
    }

    // --- Core List Operations ---

    @Override
    public int size() { // No longer throws ListException; returns 0 if empty
        return this.count;
    }

    @Override
    public void clear() {
        this.first = this.last = null; // Nullify the list
        this.count = 0; // Reset count
    }

    @Override
    public boolean isEmpty() {
        return first == null; // Or count == 0; both are valid now
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty()) {
            return false; // Standard for contains to return false if empty
            // Or throw new ListException("Circular Linked List is empty"); if you prefer consistency with other methods
        }

        Node aux = first;
        // Traverse 'count' times to cover the entire circular list
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) {
                return true; // Element found
            }
            aux = aux.next; // Move aux to the next node
        }
        return false; // Element not found
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            last.next = newNode;
            last = newNode; // Move last pointer to the new last node
        }
        // Establish circular link at the end
        last.next = first;
        count++; // Increment count
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            newNode.next = first;
            first = newNode;
        }
        // Establish circular link
        last.next = first;
        count++; // Increment count
    }

    @Override
    public void addLast(Object element) {
        add(element); // add() already handles incrementing count and circular links
    }

    @Override
    public void addInSortedList(Object element) {
        // Not implemented for a general circular linked list,
        // as its order is usually based on insertion.
        // If sorting is required, consider a different list type or
        // implement a custom sort insertion here.
    }

    @Override
    public boolean remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }

        // Case 1: Only one node in the list
        if (count == 1) {
            if (Utility.compare(first.data, element) == 0) {
                clear(); // Remove the single node
                return true; // Element was found and removed
            } else {
                // Element not found in a single-node list
                throw new ListException("Element " + element + " does not exist in Circular Linked List.");
            }
        }

        Node current = first;
        Node prev = last; // In a circular list, the node before 'first' is 'last'

        // Traverse to find the element
        for (int i = 0; i < count; i++) {
            if (Utility.compare(current.data, element) == 0) {
                // Element found, now remove it
                if (current == first) { // If it's the first node
                    first = first.next;
                } else if (current == last) { // If it's the last node
                    last = prev; // Update last to the node before the removed one
                }
                prev.next = current.next; // Link previous node to the next node
                count--; // Decrement count
                // Re-establish circular link in case first or last changed
                last.next = first;
                return true; // Element was found and removed
            }
            prev = current;
            current = current.next;
        }

        // If loop completes, element not found
        throw new ListException("Element " + element + " does not exist in Circular Linked List for removal.");
    }


    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        Object value = first.data; // Store the data of the first node

        if (count == 1) { // If only one node
            clear(); // Effectively removes the node
        } else {
            first = first.next; // Move first pointer to the next node
            last.next = first; // Re-establish circular link
            count--; // Decrement count
        }
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        Object element = last.data; // Store the data of the last node

        if (count == 1) { // If only one node
            clear(); // Effectively removes the node
        } else {
            Node prev = first;
            // Find the node before 'last'
            for (int i = 0; i < count - 1; i++) { // Iterate (count - 1) times to reach the node before last
                prev = prev.next;
            }
            // 'prev' is now the new last node
            last = prev;
            last.next = first; // Re-establish circular link
            count--; // Decrement count
        }
        return element;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        int listSize = count; // Use count for efficiency

        // Simple Bubble Sort for linked lists (less efficient than array sorts)
        for (int i = 0; i < listSize - 1; i++) {
            Node nodeI = getNode(i); // Get node at 0-based index i
            for (int j = i + 1; j < listSize; j++) {
                Node nodeJ = getNode(j); // Get node at 0-based index j

                // Compare and swap data
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
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        Node aux = first;
        // Using 0-based indexing for consistency with Java collections
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) {
                return i; // Return 0-based index
            }
            aux = aux.next;
        }
        return -1; // Element not found
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Circular Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if (isEmpty())
            throw new ListException("Circular Linked List is empty");
        return last.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        Node aux = first;
        Node prev = last; // In a circular list, the node before 'first' is 'last'

        // Traverse 'count' times
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) {
                return prev.data; // Found the element, return previous node's data
            }
            prev = aux;
            aux = aux.next;
        }
        // If loop completes, element not found
        throw new ListException("Element " + element + " does not exist in Circular Linked List.");
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        Node aux = first;
        // Traverse 'count' times
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) {
                return aux.next.data; // Found the element, return next node's data
            }
            aux = aux.next;
        }
        // If loop completes, element not found
        throw new ListException("Element " + element + " does not exist in Circular Linked List.");
    }

    /**
     * Retrieves the Node object at a specific index (0-based).
     * This method is useful for internal operations like sorting.
     *
     * @param index The 0-based index of the node to retrieve.
     * @return The Node object at the specified index.
     * @throws ListException If the list is empty or the index is out of bounds.
     */
    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Linked List is empty");
        }
        // Validate index (0-based)
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) { // Traverse to the desired node
            aux = aux.next;
        }
        return aux; // Return the node at 'index'
    }

    /**
     * Retrieves the data of the node at a specific index (0-based).
     *
     * @param index The 0-based index of the data to retrieve.
     * @return The data object at the specified index.
     * @throws ListException If the list is empty or the index is out of bounds.
     */
    public Object get(int index) throws ListException {
        return getNode(index).data;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Circular Linked List is empty";
        String result = "Circular Linked List Content (Size: " + count + ")\n";
        Node aux = first; // Auxiliar node to traverse the list
        // Traverse 'count' times to cover the entire circular list
        for (int i = 0; i < count; i++) {
            result += aux.data + "\n";
            aux = aux.next;
        }
        return result;
    }

    public Node getFirstNode() {
        return first;
    }
}