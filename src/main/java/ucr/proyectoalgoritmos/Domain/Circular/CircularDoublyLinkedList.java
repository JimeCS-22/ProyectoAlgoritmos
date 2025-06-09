package ucr.proyectoalgoritmos.Domain.Circular;

import ucr.proyectoalgoritmos.util.Utility;
import ucr.proyectoalgoritmos.Domain.list.List; // Asegúrate de importar tu interfaz List
import ucr.proyectoalgoritmos.Domain.list.Node; // Asegúrate de importar tu clase Node
import ucr.proyectoalgoritmos.Domain.list.ListException; // Asegúrate de importar tu ListException

public class CircularDoublyLinkedList implements List {
    private Node first; //apuntador al inicio de la lista
    private Node last; //apuntador al ultimo nodo de la lista

    //Constructor
    public CircularDoublyLinkedList(){
        this.first = this.last = null;
    }

    @Override
    public int size() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        int counter = 0; //contador de nodos
        Node aux = first; //aux para moverme por la lista y no perder el puntero al inicio
        while(aux!=last){
            counter++;
            aux = aux.next;
        }
        //se sale del while cuando aux==last
        return counter+1;
    }

    @Override
    public void clear() {
        this.first = this.last = null; //anula la lista
    }

    @Override
    public boolean isEmpty() {
        return first == null;
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Node aux = first;
        while(aux!=last){
            if(Utility.compare(aux.data, element)==0) return true; //ya lo encontro
            aux = aux.next; //muevo aux al nodo sgte
        }
        //se sale del while cuando aux esta en el ult nodo
        if(Utility.compare(aux.data, element)==0) return true;
        return false; //significa que no encontro el elemento
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if(isEmpty())
            first = last = newNode;
        else{
            last.next = newNode;
            //hago el doble enlace
            newNode.prev = last;
            last = newNode; //movemos el apuntador al ult nodo
        }
        //al final hacenos el enlace circular
        last.next = first;
        //hago el doble enlace
        first.prev = last;
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if(isEmpty())
            first = last = newNode;
        else {
            newNode.next = first;
            //hago el doble enlace
            first.prev = newNode;
            first = newNode;
        }
        //hago el enlace circular y doble
        last.next = first;
        first.prev = last;
    }

    @Override
    public void addLast(Object element) {
        add(element);
    }

    @Override
    public void addInSortedList(Object element) {
        // Implementar si la lista va a soportar elementos ordenados
    }

    @Override
    public void remove(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");

        // Caso especial: si solo hay un nodo y es el que queremos eliminar
        if (first == last && Utility.compare(first.data, element) == 0) {
            clear();
            return;
        }

        // Caso 1: El elemento a suprimir es el primero de la lista
        if (Utility.compare(first.data, element) == 0) {
            first = first.next;
            // Actualizar enlaces circulares
            last.next = first;
            first.prev = last;
            return;
        }

        // Caso 2. El elemento puede estar en el medio o al final
        Node aux = first.next; // Empieza desde el segundo nodo
        Node prev = first; // El nodo anterior a 'aux'

        // Recorrer hasta encontrar el elemento o llegar de nuevo al 'first' (excluyendo 'first')
        while (aux != first && !(Utility.compare(aux.data, element) == 0)) {
            prev = aux;
            aux = aux.next;
        }

        // Si se encontró el elemento (aux no es first y el elemento coincide)
        if (aux != first && Utility.compare(aux.data, element) == 0) {
            prev.next = aux.next; // Desenlazar el nodo
            aux.next.prev = prev; // Mantener el doble enlace

            // Si el nodo eliminado era el 'last', actualizamos 'last'
            if (aux == last) {
                last = prev;
            }
            // Actualizar enlaces circulares
            last.next = first;
            first.prev = last;
        } else {
            // El elemento no se encontró en la lista
            throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List for removal.");
        }
    }


    @Override
    public Object removeFirst() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Object value = first.data;
        if (first == last) { // Si solo hay un nodo
            clear();
        } else {
            first = first.next; // movemos el apuntador al nodo sgte
            //hago el enlace circular y doble
            last.next = first;
            first.prev = last;
        }
        return value;
    }

    @Override
    public Object removeLast() throws ListException {
        if(isEmpty()){
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Object element = last.data; // Valor del último nodo a eliminar
        if (first == last) { // Si solo hay un nodo
            clear();
        } else {
            Node prev = last.prev; // El nodo antes del 'last'
            prev.next = first; // Lo enlazamos con el primer nodo
            first.prev = prev; // Actualizamos el 'prev' del 'first'
            last = prev; // 'prev' se convierte en el nuevo 'last'
        }
        return element;
    }

    @Override
    public void sort() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        // Optimizamos obteniendo el tamaño una vez
        int listSize = size();
        for (int i = 1; i <= listSize; i++) {
            for (int j = i + 1; j <= listSize; j++) {
                // Se asume que getNode(index) devuelve un nodo válido.
                // Es importante que Utility.compare funcione con Object y devuelva <0, 0, >0
                if (Utility.compare(getNode(j).data, getNode(i).data) < 0) {
                    Object aux = getNode(i).data;
                    getNode(i).data = getNode(j).data;
                    getNode(j).data = aux;
                }
            }
        }
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        Node aux = first;
        int index = 1; //el primer indice de la lista es 1
        // Recorrer hasta llegar de nuevo al 'first'
        do {
            if(Utility.compare(aux.data, element)==0) return index;
            index++;
            aux = aux.next;
        } while(aux!=first); // Se detiene cuando vuelve a 'first'

        return -1; //significa q el elemento no existe en la lista
    }

    @Override
    public Object getFirst() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        return first.data;
    }

    @Override
    public Object getLast() throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        return last.data;
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");

        Node aux = first;
        // Recorrer hasta llegar de nuevo al 'first' (puede ser el nodo actual o el que sigue)
        do {
            if(Utility.compare(aux.data, element)==0){ // Encontramos el elemento
                return aux.prev.data; // Retornamos el dato del nodo anterior
            }
            aux = aux.next;
        } while(aux!=first); // Continúa hasta dar la vuelta completa

        // Si se llegó aquí, el elemento no existe en la lista
        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Circular Doubly Linked List is empty");
        }
        Node aux = first; //dejar un rastro
        // Recorrer hasta llegar de nuevo al 'first'
        do {
            if(Utility.compare(aux.data, element)==0){
                return aux.next.data; //el elemento posterior
            }
            aux = aux.next;
        } while(aux!=first); // Continúa hasta dar la vuelta completa

        // Si se llegó aquí, el elemento no existe en la lista
        throw new ListException("Element " + element + " does not exist in Circular Doubly Linked List.");
    }


    public Object get(int index) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        if(index < 1 || index > size()){ // Validar que el índice esté dentro del rango
            throw new ListException("Index out of bounds: " + index + ". Size: " + size());
        }

        Node aux = first;
        for (int i = 1; i < index; i++) { // Recorre hasta el nodo deseado
            aux = aux.next;
        }
        return aux.data; // Devuelve el dato del nodo en la posición 'index'
    }


    @Override
    public Node getNode(int index) throws ListException {
        if(isEmpty())
            throw new ListException("Circular Doubly Linked List is empty");
        if(index < 1 || index > size()){ // Validar que el índice esté dentro del rango
            throw new ListException("Index out of bounds: " + index + ". Size: " + size());
        }

        Node aux = first;
        for (int i = 1; i < index; i++) { // Recorre hasta el nodo deseado
            aux = aux.next;
        }
        return aux; // Devuelve el nodo en la posición 'index'
    }

    @Override
    public String toString() {
        if(isEmpty()) return "Circular Doubly Linked List is empty";
        String result = "Circular Doubly Linked List Content\n";
        Node aux = first; //aux para moverme por la lista
        // Recorrer hasta llegar de nuevo al 'first'
        do {
            result += aux.data + "\n";
            aux = aux.next;
        } while(aux!=first); // Continúa hasta dar la vuelta completa

        return result;
    }
}