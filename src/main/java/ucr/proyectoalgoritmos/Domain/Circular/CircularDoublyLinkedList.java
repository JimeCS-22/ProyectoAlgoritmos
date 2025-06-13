package ucr.proyectoalgoritmos.Domain.Circular; // Adjusted package name

import ucr.proyectoalgoritmos.util.Utility;
import ucr.proyectoalgoritmos.Domain.list.List;
import ucr.proyectoalgoritmos.Domain.list.Node;
import ucr.proyectoalgoritmos.Domain.list.ListException;

public class CircularDoublyLinkedList implements List {
    private Node first; // Pointer to the beginning of the list
    private Node last;  // Pointer to the last node of the list
    private int count;  // Node counter for O(1) size()

    // Constructor
    public CircularDoublyLinkedList() {
        this.first = this.last = null;
        this.count = 0; // Initialize counter
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public void clear() {
        this.first = this.last = null; // Nullify the list
        this.count = 0; // Reset counter
    }

    @Override
    public boolean isEmpty() {
        return first == null; // Or count == 0; both are valid now
    }

    @Override
    public boolean contains(Object element) throws ListException {
        // CORRECTION HERE: If empty, it simply means the element is not found.
        // No need to throw an exception in a contains method.
        if (isEmpty()) {
            return false; // Element cannot be in an empty list
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
            newNode.prev = last; // Double link
            last = newNode; // Move the pointer to the last node
        }
        // Establish circular and double links
        last.next = first;
        first.prev = last;
        count++; // Increment counter
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            newNode.next = first;
            first.prev = newNode; // Double link
            first = newNode;
        }
        // Establish circular and double links
        last.next = first;
        first.prev = last;
        count++; // Increment counter
    }

    @Override
    public void addLast(Object element) {
        add(element); // add() already handles incrementing the count and circular links
    }

    @Override
    public void addInSortedList(Object element) {
        // This method is typically implemented for sorted lists.
        // For a general-purpose circular doubly linked list, you might not need it,
        // or its implementation would depend on the comparison logic.
        // If implemented, ensure 'count' is incremented.
    }

    @Override
    public boolean remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        }

        // Case 1: Only one node in the list
        if (count == 1) {
            if (Utility.compare(first.data, element) == 0) {
                clear(); // Remove the single node
                return true; // Element was found and removed
            } else {
                // If element not found in single-node list
                return false; // Or throw an exception if preferred: throw new ListException("Element " + element + " does not exist...");
            }
        }

        Node aux = first;
        boolean found = false;
        int initialCount = count; // Store initial count for loop safety, though `count` for loop is safe
        // Traverse 'count' times to find the node
        for (int i = 0; i < initialCount; i++) { // Using initialCount for safety with `remove` operations
            if (Utility.compare(aux.data, element) == 0) {
                found = true;
                break;
            }
            aux = aux.next;
        }

        if (found) {
            // The element to remove is in 'aux'
            if (aux == first) { // If it's the first node
                first = first.next;
            } else if (aux == last) { // If it's the last node
                last = last.prev;
            }
            // Adjust the links of its neighbors
            aux.prev.next = aux.next;
            aux.next.prev = aux.prev;

            // Re-establish circular links only if list is not empty after removal
            if (count -1 > 0) { // Check if list will still have elements after decrementing
                last.next = first;
                first.prev = last;
            }

            count--; // Decrement counter
            return true; // Element was found and removed
        } else {
            // Element not found, but list was not empty. Return false.
            return false; // Or throw new ListException("Element " + element + " does not exist...");
        }
    }


    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        }
        Object value = first.data;
        if (count == 1) { // If only one node
            clear();
        } else {
            first = first.next; // Move the pointer to the next node
            // Re-establish circular and double links
            last.next = first;
            first.prev = last;
            count--; // Decrement counter
        }
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        }
        Object element = last.data; // Value of the last node to remove
        if (count == 1) { // If only one node
            clear();
        } else {
            last = last.prev; // The node before 'last' becomes the new last
            last.next = first; // Link it with the first node
            first.prev = last; // Update 'prev' of 'first'
            count--; // Decrement counter
        }
        return element;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        }
        // Using 'count' for efficiency
        int listSize = count;

        // Simple Bubble sort for linked lists
        // NOTE: For better performance, consider converting to array, sorting, and rebuilding for large lists.
        // Or implement a more efficient linked-list sort.
        for (int i = 0; i < listSize - 1; i++) {
            Node currentI = getNode(i); // Get node at position i
            for (int j = i + 1; j < listSize; j++) {
                Node currentJ = getNode(j); // Get node at position j

                // Compare and swap data
                if (Utility.compare(currentJ.data, currentI.data) < 0) {
                    Object temp = currentI.data;
                    currentI.data = currentJ.data;
                    currentJ.data = temp;
                }
            }
        }
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if (isEmpty()) {
            // CORRECTION HERE: If empty, element cannot be found, return -1.
            return -1;
        }
        Node aux = first;
        // Traverse 'count' times to cover the entire circular list
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) return i; // Return 0-based index
            aux = aux.next; // Move aux to the next node
        }

        return -1; // Element not found in the list
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        return last.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        }

        Node aux = first;
        for (int i = 0; i < count; i++) { // Traverse 'count' times
            if (Utility.compare(aux.data, element) == 0) { // Element found
                // If the element is the first, its previous is the last
                return (aux == first) ? last.data : aux.prev.data;
            }
            aux = aux.next;
        }

        // If execution reaches here, the element does not exist in the list
        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        }
        Node aux = first;
        for (int i = 0; i < count; i++) { // Traverse 'count' times
            if (Utility.compare(aux.data, element) == 0) {
                // If the element is the last, its next is the first
                return (aux == last) ? first.data : aux.next.data;
            }
            aux = aux.next;
        }

        // If execution reaches here, the element does not exist in the list
        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }


    public Object get(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        // Validate that the index is within range (0-based)
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) { // Traverse to the desired node (0-based)
            aux = aux.next;
        }
        return aux.data; // Return the data of the node at 'index'
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty"); // Appropriate to throw here
        // Validate that the index is within range (0-based)
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) { // Traverse to the desired node (0-based)
            aux = aux.next;
        }
        return aux; // Return the node at 'index'
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Circular Doubly Linked List is empty";
        String result = "Circular Doubly Linked List Content (Size: " + count + ")\n"; // Show the size
        Node aux = first; // Auxiliar node to traverse the list
        // Traverse 'count' times to cover the entire circular list
        for (int i = 0; i < count; i++) {
            result += aux.data + "\n";
            aux = aux.next;
        }
        return result;
    }
}