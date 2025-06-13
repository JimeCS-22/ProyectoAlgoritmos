package ucr.proyectoalgoritmos.Domain;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger;



public class AVL {
    private AVLNode root;
    private int size;

    public AVL() {
        this.root = null;
        this.size = 0;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return size;
    }

    public Object search(Object element) throws TreeException {
        if (isEmpty()) {
            return null;
        }
        if (!(element instanceof Comparable)) {
            throw new TreeException("El elemento debe implementar Comparable para ser buscado en el AVL.");
        }
        return search(this.root, (Comparable) element);
    }

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

    public DoublyLinkedList inOrderList() throws ListException {
        DoublyLinkedList list = new DoublyLinkedList();
        inOrderList(this.root, list);
        return list;
    }

    private void inOrderList(AVLNode node, DoublyLinkedList list) throws ListException {
        if (node != null) {
            inOrderList(node.left, list);
            list.add(node.data);
            inOrderList(node.right, list);
        }
    }

    public boolean inOrder() {
        //System.out.print("In-Order Traversal: ");
        inOrder(this.root);
        System.out.println();
        return true;
    }

    private void inOrder(AVLNode node) {
        if (node != null) {
            inOrder(node.left);
            System.out.print(node.data + " ");
            inOrder(node.right);
        }
    }


    public void insert(Passenger newPassenger) throws TreeException {
        if (newPassenger == null) {
            throw new TreeException("No se puede insertar un pasajero nulo.");
        }
        if (!(newPassenger instanceof Comparable)) {
            throw new TreeException("El pasajero debe implementar Comparable para ser insertado en el AVL.");
        }


        if (search(newPassenger) != null) {
            throw new TreeException("El pasajero con ID " + newPassenger.getId() + " ya existe y no puede ser duplicado.");
        }

        this.root = insert(this.root, newPassenger);
        size++;
    }

    private AVLNode insert(AVLNode node, Comparable element) throws TreeException {
        // 1. Inserción normal de BST
        if (node == null) {
            return new AVLNode(element);
        }

        int cmp = element.compareTo((Comparable) node.data);

        if (cmp < 0) {
            node.left = insert(node.left, element);
            if (node.left != null) node.left.setParent(node);
        } else if (cmp > 0) {
            node.right = insert(node.right, element);
            if (node.right != null) node.right.setParent(node);
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

    private int height(AVLNode node) {
        return (node == null) ? -1 : node.height;
    }

    private void updateHeight(AVLNode node) {
        if (node != null) {
            node.height = 1 + Math.max(height(node.left), height(node.right));
        }
    }

    private int getBalanceFactor(AVLNode node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private AVLNode rotateRight(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        // Realizar rotación
        x.right = y;
        y.left = T2;

        // Actualizar padres
        x.setParent(y.getParent());
        y.setParent(x);
        if (T2 != null) T2.setParent(y);

        // Actualizar alturas
        updateHeight(y);
        updateHeight(x);

        return x;
    }


    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        // Realizar rotación
        y.left = x;
        x.right = T2;

        // Actualizar padres
        y.setParent(x.getParent());
        x.setParent(y);
        if (T2 != null) T2.setParent(x);

        // Actualizar alturas
        updateHeight(x);
        updateHeight(y);

        return y;
    }

}