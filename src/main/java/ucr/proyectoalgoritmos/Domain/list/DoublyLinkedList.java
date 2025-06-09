package ucr.proyectoalgoritmos.Domain.list;


import ucr.proyectoalgoritmos.util.Utility;

public class DoublyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista

    //Constructor
    public DoublyLinkedList() {
        this.first = null;
    }

    @Override
    public int size() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        int counter = 0; //contador de nodos
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while (aux != null) {
            counter++;
            aux = aux.next;
        }
        return counter;
    }

    @Override
    public void clear() {
        this.first = null; //anula la lista
    }

    @Override
    public boolean isEmpty() {
        return first == null;
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Node aux = first;
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) return true; //ya lo encontro
            aux = aux.next; //muevo aux al nodo sgte
        }
        return false; //significa que no encontro el elemento
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if (isEmpty())
            first = newNode;
        else {
            Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
            while (aux.next != null) {
                aux = aux.next; //mueve aux al nodo sgte
            }
            //se sale del while cuando aux esta en el ult nodo
            aux.next = newNode;
            //hago el doble enlace
            newNode.prev = aux;
        }
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if (isEmpty())
            first = newNode;
        else {
            newNode.next = first;
            //hago el doble enlace
            first.prev = newNode;
            first = newNode;
        }
    }

    @Override
    public void addLast(Object element) {
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {

    }

    @Override
    public void remove(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        //Caso 1: El elemento a suprimir es el primero de la lista
        if (Utility.compare(first.data, element) == 0) {
            first = first.next;
            first.prev = null; //actualizo el enlace al nodo anteior
        }
        //Caso 2. El elemento puede estar en el medio o al final
        else {
            Node prev = first; //nodo anterior
            Node aux = first.next; //nodo sgte
            while (aux != null && !(Utility.compare(aux.data, element) == 0)) {
                prev = aux;
                aux = aux.next;
            }
            //se sale del while cuanda alcanza nulo
            //o cuando encuentra el elemento
            if (aux != null && Utility.compare(aux.data, element) == 0) {
                //debo desenlazar  el nodo
                prev.next = aux.next;
                //mantengo el doble enlace
                if (aux.next != null)
                    aux.next.prev = prev;
            }
        }
    }

    @Override
    public Object removeFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Object value = first.data;
        first = first.next; //movemos el apuntador al nodo sgte
        //rompo el doble enlace
        if (first != null)
            first.prev = null;
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        return null;
    }

    @Override
    public void sort() throws ListException {

    }

    @Override
    public int indexOf(Object element) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Node aux = first;
        int index = 1; //el primer indice de la lista es 1
        while (aux != null) {
            if (Utility.compare(aux.data, element) == 0) return index;
            index++;
            aux = aux.next;
        }
        return -1; //significa q el elemento no existe en la lista
    }

    @Override
    public Object getFirst() throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        return null;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        return null;
    }

    @Override
    public Object getNext(Object element) throws ListException {
        return null;
    }

    @Override
    public Node getNode(int index) throws ListException {
        if (isEmpty())
            throw new ListException("Doubly Linked List is empty");
        Node aux = first;
        int i = 1; //posicion del primer nodo
        while (aux != null) {
            if (Utility.compare(i, index) == 0) {
                return aux;
            }
            i++;
            aux = aux.next; //lo movemos al sgte nodo
        }
        return null;
    }

    @Override
    public String toString() {
        if (isEmpty()) return "Doubly Linked List is empty";
        String result = "Doubly Linked List Content\n";
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while (aux != null) {
            result += aux.data + " ";
            aux = aux.next; //lo muevo al sgte nodo
        }
        return result;
    }
}