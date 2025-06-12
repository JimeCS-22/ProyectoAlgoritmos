package ucr.proyectoalgoritmos.Domain;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // Make sure this import is correct

// Asumiendo que Passenger implementa Comparable<Passenger>
// y que el elemento almacenado en el AVL es de un tipo que implementa Comparable.

public class AVL {
    private AVLNode root;
    private int size; // Ahora sí tenemos un contador de tamaño

    public AVL() {
        this.root = null;
        this.size = 0; // Inicializamos el tamaño a 0
    }

    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Devuelve el número de elementos (nodos) en el árbol AVL.
     * @return El número de elementos.
     */
    public int size() {
        return size;
    }

    /**
     * Busca un elemento en el árbol AVL.
     *
     * @param element El elemento a buscar. Debe ser Comparable.
     * @return El elemento encontrado si existe, o null si el árbol está vacío o el elemento no se encuentra.
     * @throws TreeException Si el elemento no es Comparable.
     */
    public Object search(Object element) throws TreeException {
        if (isEmpty()) {
            return null;
        }
        if (!(element instanceof Comparable)) {
            throw new TreeException("El elemento debe implementar Comparable para ser buscado en el AVL.");
        }
        return search(this.root, (Comparable) element);
    }

    /**
     * Método auxiliar recursivo para la búsqueda.
     *
     * @param node    El nodo actual a examinar.
     * @param element El elemento comparable a buscar.
     * @return El elemento encontrado, o null si no se encuentra.
     */
    private Object search(AVLNode node, Comparable element) {
        if (node == null) {
            return null;
        }

        int cmp = element.compareTo((Comparable) node.data);

        if (cmp < 0) {
            return search(node.left, element);
        } else if (cmp > 0) {
            return search(node.right, element);
        } else {
            return node.data;
        }
    }

    /**
     * Devuelve una lista doblemente enlazada con todos los elementos del árbol AVL
     * en orden ascendente (recorrido in-order).
     *
     * @return Una DoublyLinkedList que contiene los elementos del AVL en orden.
     * @throws ListException Si ocurre un error al añadir elementos a la lista.
     */
    public DoublyLinkedList inOrderList() throws ListException {
        DoublyLinkedList list = new DoublyLinkedList();
        inOrderList(this.root, list);
        return list;
    }

    /**
     * Método auxiliar recursivo para el recorrido in-order y añadir a una lista.
     *
     * @param node El nodo actual que se está visitando.
     * @param list La DoublyLinkedList donde se añadirán los elementos.
     * @throws ListException Si ocurre un error al añadir elementos a la lista.
     */
    private void inOrderList(AVLNode node, DoublyLinkedList list) throws ListException {
        if (node != null) {
            inOrderList(node.left, list);
            list.add(node.data);
            inOrderList(node.right, list);
        }
    }

    /**
     * Realiza un recorrido in-order imprimiendo los elementos.
     * Este método solo para propósitos de depuración o visualización directa.
     *
     * @return Siempre true (este método es de impresión, no de estado del árbol).
     */
    public boolean inOrder() { // <-- COMPLETE: Implementación de inOrder para imprimir
        System.out.print("In-Order Traversal: ");
        inOrder(this.root);
        System.out.println();
        return true; // Retorna true como un indicador de que se realizó la operación.
    }

    /**
     * Método auxiliar recursivo para el recorrido in-order (impresión).
     *
     * @param node El nodo actual que se está visitando.
     */
    private void inOrder(AVLNode node) {
        if (node != null) {
            inOrder(node.left);
            System.out.print(node.data + " "); // Asume que el objeto tiene un toString() significativo
            inOrder(node.right);
        }
    }

    /**
     * Inserta un nuevo pasajero en el árbol AVL.
     *
     * @param newPassenger El objeto Passenger a insertar. Debe ser Comparable.
     * @throws TreeException Si el pasajero ya existe o si no implementa Comparable.
     */
    public void insert(Passenger newPassenger) throws TreeException { // <-- COMPLETE: Implementación de insert
        if (newPassenger == null) {
            throw new TreeException("No se puede insertar un pasajero nulo.");
        }
        if (!(newPassenger instanceof Comparable)) {
            throw new TreeException("El pasajero debe implementar Comparable para ser insertado en el AVL.");
        }

        // Search for existing passenger first to prevent duplicates
        if (search(newPassenger) != null) {
            // Passenger with this ID already exists, do not insert
            // You might want to throw a specific exception here instead of just returning
            throw new TreeException("El pasajero con ID " + newPassenger.getId() + " ya existe y no puede ser duplicado.");
        }

        this.root = insert(this.root, newPassenger);
        size++; // Incrementamos el tamaño solo si la inserción fue exitosa (no era duplicado)
    }

    /**
     * Método auxiliar recursivo para la inserción en el árbol AVL.
     * Realiza la inserción y luego el balanceo del árbol.
     *
     * @param node El nodo actual en el que se está intentando insertar.
     * @param element El elemento (Comparable) a insertar.
     * @return El nodo raíz del subárbol después de la inserción y el balanceo.
     * @throws TreeException Si hay algún error durante la inserción o comparación.
     */
    private AVLNode insert(AVLNode node, Comparable element) throws TreeException {
        // 1. Inserción normal de BST
        if (node == null) {
            return new AVLNode(element);
        }

        int cmp = element.compareTo((Comparable) node.data);

        if (cmp < 0) {
            node.left = insert(node.left, element);
            if (node.left != null) node.left.setParent(node); // Set parent pointer
        } else if (cmp > 0) {
            node.right = insert(node.right, element);
            if (node.right != null) node.right.setParent(node); // Set parent pointer
        } else {
            // El elemento ya existe (duplicado), no hacemos nada
            return node;
        }

        // 2. Actualizar la altura del nodo actual
        updateHeight(node);

        // 3. Obtener el factor de balance y balancear si es necesario
        int balanceFactor = getBalanceFactor(node);

        // Rotación a la izquierda (Left Left Case)
        if (balanceFactor > 1 && element.compareTo((Comparable) node.left.data) < 0) {
            return rotateRight(node);
        }

        // Rotación a la derecha (Right Right Case)
        if (balanceFactor < -1 && element.compareTo((Comparable) node.right.data) > 0) {
            return rotateLeft(node);
        }

        // Rotación doble izquierda-derecha (Left Right Case)
        if (balanceFactor > 1 && element.compareTo((Comparable) node.left.data) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Rotación doble derecha-izquierda (Right Left Case)
        if (balanceFactor < -1 && element.compareTo((Comparable) node.right.data) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }


    // --- Métodos Auxiliares para AVL Balanceo ---

    /**
     * Calcula la altura de un nodo.
     * @param node El nodo cuya altura se quiere calcular.
     * @return La altura del nodo, o -1 si el nodo es nulo.
     */
    private int height(AVLNode node) { // <-- NEW: Helper for height
        return (node == null) ? -1 : node.height;
    }

    /**
     * Actualiza la altura de un nodo basándose en las alturas de sus hijos.
     * @param node El nodo cuya altura se va a actualizar.
     */
    private void updateHeight(AVLNode node) { // <-- NEW: Helper to update height
        if (node != null) {
            node.height = 1 + Math.max(height(node.left), height(node.right));
        }
    }

    /**
     * Calcula el factor de balance de un nodo.
     * (altura del subárbol izquierdo - altura del subárbol derecho)
     * @param node El nodo cuyo factor de balance se quiere calcular.
     * @return El factor de balance.
     */
    private int getBalanceFactor(AVLNode node) { // <-- NEW: Helper for balance factor
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    /**
     * Realiza una rotación simple a la derecha.
     */
    private AVLNode rotateRight(AVLNode y) { // <-- NEW: Rotation method
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        // Realizar rotación
        x.right = y;
        y.left = T2;

        // Actualizar padres
        x.setParent(y.getParent()); // x toma el padre de y
        y.setParent(x);             // y ahora es hijo derecho de x
        if (T2 != null) T2.setParent(y); // T2 es hijo izquierdo de y

        // Actualizar alturas
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    /**
     * Realiza una rotación simple a la izquierda.
     *
     * x                        y
     * / \                      / \
     * T1  y      --->          x   T3
     * / \                  / \
     * T2  T3               T1  T2
     *
     * @param x La raíz del subárbol desbalanceado.
     * @return La nueva raíz del subárbol (y).
     */
    private AVLNode rotateLeft(AVLNode x) { // <-- NEW: Rotation method
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        // Realizar rotación
        y.left = x;
        x.right = T2;

        // Actualizar padres
        y.setParent(x.getParent()); // y toma el padre de x
        x.setParent(y);             // x ahora es hijo izquierdo de y
        if (T2 != null) T2.setParent(x); // T2 es hijo derecho de x

        // Actualizar alturas
        updateHeight(x);
        updateHeight(y);

        return y;
    }

}