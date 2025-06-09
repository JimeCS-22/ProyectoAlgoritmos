package ucr.proyectoalgoritmos.Domain.Circular;

import ucr.proyectoalgoritmos.util.Utility;
import ucr.proyectoalgoritmos.Domain.list.List; // Asegúrate de importar tu interfaz List
import ucr.proyectoalgoritmos.Domain.list.Node; // Asegúrate de importar tu clase Node
import ucr.proyectoalgoritmos.Domain.list.ListException; // Asegúrate de importar tu ListException

public class CircularDoublyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista
    private Node last;  //apuntador al ultimo nodo de la lista
    private int count;  // Nuevo: Contador de nodos para O(1) size()

    // Constructor
    public CircularDoublyLinkedList() {
        this.first = this.last = null;
        this.count = 0; // Inicializar contador
    }

    @Override
    public int size() { // Ya no lanza ListException, retorna 0 si está vacía
        return this.count;
    }

    @Override
    public void clear() {
        this.first = this.last = null; // anula la lista
        this.count = 0; // Resetear contador
    }

    @Override
    public boolean isEmpty() {
        return first == null; // O count == 0; ambas son válidas ahora
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty()) {
            // Es más común que contains retorne false si la lista está vacía.
            // Si la aplicación requiere un error si se busca en lista vacía,
            // se puede mantener el throw, pero el false es más estándar.
            return false; // o throw new ListException("Circular Doubly Linked List is empty"); si lo prefieres
        }

        Node aux = first;
        // Recorrer hasta encontrar el elemento o dar una vuelta completa
        for (int i = 0; i < count; i++) { // Iterar 'count' veces
            if (Utility.compare(aux.data, element) == 0) return true; // ya lo encontró
            aux = aux.next; // muevo aux al nodo sgte
        }
        return false; // significa que no encontró el elemento
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            last.next = newNode;
            newNode.prev = last; // doble enlace
            last = newNode; // movemos el apuntador al ult nodo
        }
        // Al final, hacemos el enlace circular (y doble)
        last.next = first;
        first.prev = last;
        count++; // Incrementar contador
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty()) {
            first = last = newNode;
        } else {
            newNode.next = first;
            first.prev = newNode; // doble enlace
            first = newNode;
        }
        // Hago el enlace circular y doble
        last.next = first;
        first.prev = last;
        count++; // Incrementar contador
    }

    @Override
    public void addLast(Object element) {
        add(element); // add() ya incrementa el count
    }

    @Override
    public void addInSortedList(Object element) {
        // Implementar si la lista va a soportar elementos ordenados
        // Si se implementa, asegurarse de incrementar 'count'.
    }

    @Override
    public boolean remove(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }

        // Caso especial: si solo hay un nodo
        if (count == 1) {
            if (Utility.compare(first.data, element) == 0) {
                clear(); // Elimina el único nodo
                return false;
            } else {
                throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
            }
        }

        Node aux = first;
        boolean found = false;
        for (int i = 0; i < count; i++) { // Iterar 'count' veces para encontrar el nodo
            if (Utility.compare(aux.data, element) == 0) {
                found = true;
                break;
            }
            aux = aux.next;
        }

        if (found) {
            // El elemento a eliminar está en 'aux'
            if (aux == first) { // Si es el primer nodo
                first = first.next;
                first.prev = last;
                last.next = first;
            } else if (aux == last) { // Si es el último nodo
                last = last.prev;
                last.next = first;
                first.prev = last;
            } else { // Si es un nodo en el medio
                aux.prev.next = aux.next;
                aux.next.prev = aux.prev;
            }
            count--; // Decrementar contador
        } else {
            throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List for removal.");
        }
        return found;
    }


    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Object value = first.data;
        if (count == 1) { // Si solo hay un nodo
            clear();
        } else {
            first = first.next; // movemos el apuntador al nodo sgte
            // hago el enlace circular y doble
            last.next = first;
            first.prev = last;
            count--; // Decrementar contador
        }
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Object element = last.data; // Valor del último nodo a eliminar
        if (count == 1) { // Si solo hay un nodo
            clear();
        } else {
            last = last.prev; // El nodo antes del 'last' se convierte en el nuevo last
            last.next = first; // Lo enlazamos con el primer nodo
            first.prev = last; // Actualizamos el 'prev' del 'first'
            count--; // Decrementar contador
        }
        return element;
    }

    @Override
    public void sort() throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        // Optimizamos obteniendo el tamaño una vez
        int listSize = count; // Usamos 'count' para el tamaño
        // Bubble sort, intercambiando datos directamente en los nodos
        for (int i = 0; i < listSize - 1; i++) { // Iterar hasta el penúltimo elemento
            for (int j = i + 1; j < listSize; j++) { // Iterar desde i+1 hasta el final
                Node nodeI = getNode(i); // Obtener el nodo en la posición i (0-based)
                Node nodeJ = getNode(j); // Obtener el nodo en la posición j (0-based)

                if (Utility.compare(nodeJ.data, nodeI.data) < 0) {
                    // Intercambiar datos
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
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Node aux = first;
        // Recorrer hasta encontrar el elemento o dar una vuelta completa
        for (int i = 0; i < count; i++) { // Iterar 'count' veces
            if (Utility.compare(aux.data, element) == 0) return i; // Retorna el índice 0-based
            aux = aux.next; // muevo aux al nodo sgte
        }

        return -1; // significa que el elemento no existe en la lista
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
        for (int i = 0; i < count; i++) { // Iterar 'count' veces
            if (Utility.compare(aux.data, element) == 0) { // Encontramos el elemento
                // Si el elemento es el primero, su anterior es el último
                return (aux == first) ? last.data : aux.prev.data;
            }
            aux = aux.next;
        }

        // Si se llegó aquí, el elemento no existe en la lista
        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Node aux = first; // dejar un rastro
        for (int i = 0; i < count; i++) { // Iterar 'count' veces
            if (Utility.compare(aux.data, element) == 0) {
                // Si el elemento es el último, su siguiente es el primero
                return (aux == last) ? first.data : aux.next.data;
            }
            aux = aux.next;
        }

        // Si se llegó aquí, el elemento no existe en la lista
        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }

    public Object get(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        // Validar que el índice esté dentro del rango (0-based)
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) { // Recorre hasta el nodo deseado (0-based)
            aux = aux.next;
        }
        return aux.data; // Devuelve el dato del nodo en la posición 'index'
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        // Validar que el índice esté dentro del rango (0-based)
        if (index < 0 || index >= count) {
            throw new ListException("Index out of bounds: " + index + ". Size: " + count);
        }

        Node aux = first;
        for (int i = 0; i < index; i++) { // Recorre hasta el nodo deseado (0-based)
            aux = aux.next;
        }
        return aux; // Devuelve el nodo en la posición 'index'
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Circular Doubly Linked List is empty";
        String result = "Circular Doubly Linked List Content (Size: " + count + ")\n"; // Mostrar el tamaño
        Node aux = first; // aux para moverme por la lista
        // Recorrer hasta dar una vuelta completa
        for (int i = 0; i < count; i++) { // Iterar 'count' veces
            result += aux.data + "\n";
            aux = aux.next;
        }
        return result;
    }
}