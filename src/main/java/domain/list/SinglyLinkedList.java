package domain.list;

public class SinglyLinkedList implements List{
    private Node first; //apuntador al inicio de la lista
    private int size; // Para mantener el tamaño de la lista de forma eficiente

    public SinglyLinkedList() {
        this.first = null; //la lista no existe
        this.size = 0; // Inicializar tamaño
    }

    @Override
    public int size() throws ListException {
        return this.size; // Retornar el tamaño mantenido
    }

    @Override
    public void clear() {
        this.first = null; //anulamos la lista
        this.size = 0; // Resetear tamaño
    }

    @Override
    public boolean isEmpty() {
        return this.first == null; //si es nulo está vacía
    }

    @Override
    public boolean contains(Object element) throws ListException {
        if(isEmpty()) return false; // Si está vacía, el elemento no puede estar

        Node aux = first;
        while(aux!=null){
            // USAR .equals() AQUI
            if(aux.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
                return true;
            }
            aux = aux.next;
        }
        return false;
    }

    @Override
    public void add(Object element) {
        Node newNode = new Node(element);
        if(isEmpty()){
            first = newNode;
        }else{
            Node aux = first;
            while(aux.next!=null){
                aux=aux.next;
            }
            aux.next = newNode;
        }
        size++; // Incrementar tamaño al añadir
    }

    @Override
    public void addFirst(Object element) {
        Node newNode = new Node(element);
        if(isEmpty()){
            first = newNode;
        }else{
            newNode.next = first;
            first = newNode;
        }
        size++; // Incrementar tamaño al añadir
    }

    @Override
    public void addLast(Object element) {
        add(element); // Reutiliza el método add
    }

    @Override
    public void addInSortedList(Object element) {
        // No implementado, no se necesita cambiar
    }

    @Override
    public void remove(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        // Caso 1: El elemento a suprimir está al inicio
        // USAR .equals() AQUI
        if(first.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
            first = first.next;
            size--; // Decrementar tamaño al eliminar
            return; // Salir de la función después de eliminar
        }

        // Caso 2: El elemento a suprimir puede estar al medio o final
        Node prev = first;
        Node aux = first.next;
        // USAR .equals() AQUI
        while(aux!=null && !aux.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
            prev = aux;
            aux = aux.next;
        }
        // Se sale cuando alcanza nulo (elemento no encontrado)
        // o cuando encuentra el elemento
        // USAR .equals() AQUI
        if(aux!=null && aux.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
            // Ya lo encontró, procedo a desenlazar el nodo
            prev.next = aux.next;
            size--; // Decrementar tamaño al eliminar
        } else {
            // Opcional: lanzar excepción si el elemento no se encuentra
            // throw new ListException("Element " + element + " not found in the list.");
        }
    }

    @Override
    public Object removeFirst() throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Object data = first.data;
        first = first.next;
        size--; // Decrementar tamaño al eliminar
        return data;
    }

    @Override
    public Object removeLast() throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Object data = null; // Variable para almacenar la data del último nodo
        if(first.next == null){ // Sólo un elemento en la lista
            data = first.data;
            first = null;
        }else{
            Node aux = first;
            while(aux.next.next != null){ // Recorrer hasta que aux.next sea el último nodo
                aux = aux.next;
            }
            data = aux.next.data;
            aux.next = null; // Eliminar el último nodo
        }
        size--; // Decrementar tamaño al eliminar
        return data;
    }

    @Override
    public void sort() throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        // Aquí puedes seguir usando util.Utility.compare si está diseñado para ordenar,
        // pero asegúrate de que maneje tus objetos Vertex/EdgeWeight correctamente
        // si se usan para ordenar la lista de vértices.
        // Si no, también deberías considerar una forma de ordenar que use compareTo si tus objetos son Comparable.
        for (int i = 1; i <= size() ; i++) {
            for (int j = i+1; j <= size() ; j++) {
                if(util.Utility.compare(getNode(j).data, getNode(i).data)<0){
                    Object aux = getNode(i).data;
                    getNode(i).data = getNode(j).data;
                    getNode(j).data = aux;
                }
            }
        }
    }

    @Override
    public int indexOf(Object element) throws ListException {
        if(isEmpty()){
            return -1; // Si está vacía, el elemento no está
        }
        Node aux = first;
        int index=1; // Índice base 1
        while(aux!=null){
            // USAR .equals() AQUI
            if(aux.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
                return index;
            }
            index++;
            aux=aux.next;
        }
        return -1; // Elemento no encontrado
    }

    @Override
    public Object getFirst() throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        return first.data; // Retorna la DATA del primer nodo
    }

    // NUEVO MÉTODO: Retorna el primer objeto Node (requerido por el grafo)
    public Node getFirstNode() throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        return first; // Retorna el objeto Node del primer nodo
    }


    @Override
    public Object getLast() throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while(aux.next!=null){
            aux=aux.next;
        }
        return aux.data; // Retorna la DATA del último nodo
    }

    @Override
    public Object getPrev(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        // USAR .equals() AQUI
        if(first.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
            return "It's the first, it has no previous"; // Mejor retornar null o lanzar excepción específica
        }
        Node aux = first;
        while(aux.next!=null){
            // USAR .equals() AQUI
            if(aux.next.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
                return aux.data;
            }
            aux=aux.next;
        }
        return "Does not exist in Single Linked List"; // Mejor retornar null
    }

    @Override
    public Object getNext(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while(aux!=null){
            // USAR .equals() AQUI
            if(aux.data.equals(element)){ // <-- CORREGIDO: Usar .equals()
                if(aux.next != null){
                    return aux.next.data;
                } else {
                    return "It's the last, it has no next"; // Mejor retornar null
                }
            }
            aux = aux.next;
        }
        return "Does not exist in Single Linked List"; // Mejor retornar null
    }

    @Override
    public Node getNode(int index) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        if (index < 1 || index > size()) { // Usa el size() eficiente
            throw new ListException("Invalid index: " + index);
        }
        Node aux = first;
        int i = 1;
        while(aux!=null){
            if(i == index) { // Aquí la comparación es numérica, no de objetos, util.Utility.compare podría estar bien si es para ints.
                return aux;
            }
            i++;
            aux = aux.next;
        }
        return null; // No debería alcanzarse si los límites son correctos
    }

    // Este método ya lo tenías, lo mantengo.
    public Node getNode(Object element) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        Node aux = first;
        while(aux!=null){
            // USAR .equals() AQUI
            if(aux.data.equals(element)) { // <-- CORREGIDO: Usar .equals()
                return aux;
            }
            aux = aux.next;
        }
        return null;
    }

    @Override
    public String toString() {
        String result = "";
        Node aux = first;
        while(aux!=null){
            result+= "\n"+aux.data;
            aux = aux.next;
        }
        return result;
    }

    // Ya lo tenías, lo mantengo.
    public Object get(int index) throws ListException {
        if(isEmpty()){
            throw new ListException("Singly Linked List is Empty");
        }
        if (index < 1 || index > size()) {
            throw new ListException("Invalid index: " + index);
        }

        Node aux = first;
        int i = 1;
        while(aux != null){
            if(i == index) { // Aquí la comparación es numérica, no de objetos.
                return aux.data;
            }
            i++;
            aux = aux.next;
        }
        return null; // Should not reach here
    }
}