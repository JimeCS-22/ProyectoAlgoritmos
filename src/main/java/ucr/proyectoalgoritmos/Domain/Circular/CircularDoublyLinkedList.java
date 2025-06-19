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
        return first == null;
    }

    @Override
    public boolean contains(Object element) throws ListException {

        if (isEmpty()) {
            return false;
        }

        Node aux = first;
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) {
                return true; // Element found
            }
            aux = aux.next;
        }
        return false;
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            last.next = newNode;
            newNode.prev = last;
            last = newNode;
        }
        last.next = first;
        first.prev = last;
        count++;
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            newNode.next = first;
            first.prev = newNode;
            first = newNode;
        }
        last.next = first;
        first.prev = last;
        count++;
    }

    @Override
    public void addLast(Object element) {
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {

    }

    @Override
    public boolean remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }


        if (count == 1) {
            if (Utility.compare(first.data, element) == 0) {
                clear();
                return true;
            } else {

                return false;
            }
        }

        Node aux = first;
        boolean found = false;
        int initialCount = count;
        for (int i = 0; i < initialCount; i++) {
            if (Utility.compare(aux.data, element) == 0) {
                found = true;
                break;
            }
            aux = aux.next;
        }

        if (found) {
            if (aux == first) {
                first = first.next;
            } else if (aux == last) {
                last = last.prev;
            }

            aux.prev.next = aux.next;
            aux.next.prev = aux.prev;


            if (count -1 > 0) {
                last.next = first;
                first.prev = last;
            }

            count--;
            return true;
        } else {
            return false;
        }
    }


    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Object value = first.data;
        if (count == 1) {
            clear();
        } else {
            first = first.next;
            last.next = first;
            first.prev = last;
            count--;
        }
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Object element = last.data;
        if (count == 1) {
            clear();
        } else {
            last = last.prev;
            last.next = first;
            first.prev = last;
            count--;
        }
        return element;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        int listSize = count;

        for (int i = 0; i < listSize - 1; i++) {
            Node currentI = getNode(i);
            for (int j = i + 1; j < listSize; j++) {
                Node currentJ = getNode(j);

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
            return -1;
        }
        Node aux = first;
        // Traverse 'count' times to cover the entire circular list
        for (int i = 0; i < count; i++) {
            if (Utility.compare(aux.data, element) == 0) return i;
            aux = aux.next;
        }

        return -1;
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        return last.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }

        Node aux = first;
        for (int i = 0; i < count; i++) { // Traverse 'count' times
            if (Utility.compare(aux.data, element) == 0) { // Element found
                return (aux == first) ? last.data : aux.prev.data;
            }
            aux = aux.next;
        }

        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Node aux = first;
        for (int i = 0; i < count; i++) { // Traverse 'count' times
            if (Utility.compare(aux.data, element) == 0) {
                return (aux == last) ? first.data : aux.next.data;
            }
            aux = aux.next;
        }

        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }


    public Object get(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) {
            aux = aux.next;
        }
        return aux.data;
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        // Validate that the index is within range (0-based)
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) {
            aux = aux.next;
        }
        return aux;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Circular Doubly Linked List is empty";
        String result = "Circular Doubly Linked List Content (Size: " + count + ")\n";
        Node aux = first;
        for (int i = 0; i < count; i++) {
            result += aux.data + "\n";
            aux = aux.next;
        }
        return result;
    }
}