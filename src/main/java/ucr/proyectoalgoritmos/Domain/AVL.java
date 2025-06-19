package ucr.proyectoalgoritmos.Domain;

import ucr.proyectoalgoritmos.Domain.list.DoublyLinkedList;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.Domain.passenger.Passenger; // Assuming Passenger is Comparable and the type you store

public class AVL {
    private AVLNode root;
    private int size; // Correctly tracks the number of elements

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
            return node.data; // Element found
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
        //System.out.print("In-Order Traversal: "); // Uncomment for debugging
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

    // Changed to take a generic Comparable element to be more flexible,
    // or keep Passenger if AVL is strictly for Passengers and you want strong typing.
    // For now, keeping Passenger as per your original intent but note the generic alternative.
    public void insert(Passenger newPassenger) throws TreeException {
        if (newPassenger == null) {
            throw new TreeException("No se puede insertar un pasajero nulo.");
        }
        if (!(newPassenger instanceof Comparable)) {
            throw new TreeException("El pasajero debe implementar Comparable para ser insertado en el AVL.");
        }

        // Before attempting insertion, check if it already exists.
        // This is crucial for your PassengerManager's duplicate check.
        if (search(newPassenger) != null) {
            // If the element already exists, throw an exception here.
            // Your PassengerManager handles this, but it's good for the AVL to be explicit too.
            throw new TreeException("El elemento (pasajero con ID " + newPassenger.getId() + ") ya existe y no puede ser duplicado.");
        }

        // The size increment MUST happen when a NEW node is actually created.
        // The recursive insert method will now handle size increment.
        this.root = insert(this.root, newPassenger);
    }

    private AVLNode insert(AVLNode node, Comparable element) throws TreeException {
        // 1. Normal BST insertion
        if (node == null) {
            size++; // Increment size ONLY when a new node is created
            return new AVLNode(element); // New node, height 0
        }

        int cmp = element.compareTo((Comparable) node.data);

        if (cmp < 0) {
            node.left = insert(node.left, element);
            if (node.left != null) node.left.setParent(node); // Ensure parent reference is updated
        } else if (cmp > 0) {
            node.right = insert(node.right, element);
            if (node.right != null) node.right.setParent(node); // Ensure parent reference is updated
        } else {
            // Element already exists (this case should ideally not be hit if pre-checked by public insert)
            // If it is hit, it means a duplicate was passed, and we don't insert or increment size.
            return node;
        }

        // 2. Update height of the current node
        updateHeight(node);

        // 3. Get balance factor and balance if necessary
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

        // Perform rotation
        x.right = y;
        y.left = T2;

        // Update parents
        // y's original parent becomes x's parent
        if (y.getParent() != null) {
            if (y.getParent().getLeft() == y) {
                y.getParent().setLeft(x);
            } else {
                y.getParent().setRight(x);
            }
        }
        x.setParent(y.getParent()); // x becomes the new root of the subtree
        y.setParent(x); // y becomes x's right child
        if (T2 != null) T2.setParent(y); // T2's parent is now y

        // Update heights
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    private AVLNode rotateLeft(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        // Perform rotation
        y.left = x;
        x.right = T2;

        // Update parents
        // x's original parent becomes y's parent
        if (x.getParent() != null) {
            if (x.getParent().getLeft() == x) {
                x.getParent().setLeft(y);
            } else {
                x.getParent().setRight(y);
            }
        }
        y.setParent(x.getParent()); // y becomes the new root of the subtree
        x.setParent(y); // x becomes y's left child
        if (T2 != null) T2.setParent(x); // T2's parent is now x

        // Update heights
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * Elimina un elemento del árbol AVL.
     *
     * @param element El elemento a eliminar. Debe ser Comparable.
     * @throws TreeException Si el árbol está vacío, el elemento no implementa Comparable,
     * o el elemento no se encuentra en el árbol.
     */
    public void remove(Object element) throws TreeException {
        if (isEmpty()) {
            throw new TreeException("El árbol AVL está vacío. No se puede eliminar.");
        }
        if (!(element instanceof Comparable)) {
            throw new TreeException("El elemento debe implementar Comparable para ser eliminado del AVL.");
        }
        // Check if the element exists first (optional, but provides a clearer error message)
        if (search(element) == null) {
            throw new TreeException("El elemento no se encuentra en el árbol para ser eliminado.");
        }

        // Before actual removal, check if element is present to avoid decrementing size twice
        int initialSize = size;
        this.root = remove(this.root, (Comparable) element);
        // Only decrement size if the removal was successful (i.e., root changed or element was found and removed)
        if (this.root == null && initialSize == 1) { // Removed last element
            size = 0;
        } else if (size == initialSize && this.root != null) {
            // This case means no element was removed by the recursive remove call,
            // even though search() initially found it. This shouldn't happen with correct logic.
            // Potentially remove this `size--` from here and let the recursive method return a boolean
            // indicating if a node was removed, or simply trust the recursive call's logic.
            // For now, let's assume the recursive remove properly handles decrementing.
            // If the element was found and removed, `size` should have decreased.
            // So, no explicit decrement here, it should be handled inside `remove(AVLNode node, Comparable element)`
            // when `node = null` is set for a leaf or single-child removal.
            // This is a tricky part of AVL removals regarding size updates.
            // Let's defer size update to the base case where a node is actually removed (becomes null)
            // in the recursive `remove` method.
        }
        // IMPORTANT: The `size--` should happen *inside* the recursive `remove` method
        // when a node is actually taken out of the tree. Let's fix that too.
    }

    private AVLNode remove(AVLNode node, Comparable element) {
        // 1. Perform standard BST deletion
        if (node == null) {
            return node; // Element not found, or base case for recursion
        }

        int cmp = element.compareTo((Comparable) node.data);

        if (cmp < 0) { // Element to delete is in the left subtree
            node.left = remove(node.left, element);
            if (node.left != null) node.left.setParent(node);
        } else if (cmp > 0) { // Element to delete is in the right subtree
            node.right = remove(node.right, element);
            if (node.right != null) node.right.setParent(node);
        } else { // This is the node to be deleted
            // Case 1: Node with zero or one child
            if (node.left == null || node.right == null) {
                AVLNode temp = (node.left != null) ? node.left : node.right;

                // No children
                if (temp == null) {
                    node = null; // Node is truly removed
                } else { // One child
                    // Ensure the parent reference of the child is updated
                    if (temp != null) temp.setParent(node.getParent()); // Temp becomes the new child of node's parent
                    node = temp; // Replace node with its child
                }
                size--; // Decrement size ONLY when a node is actually removed
            } else {
                // Case 2: Node with two children
                // Get the in-order successor (smallest in the right subtree)
                AVLNode temp = findMin(node.right);
                node.data = temp.data; // Copy the successor's data to this node

                // Delete the in-order successor (which will have at most one child)
                // The size decrement for the successor will happen in this recursive call
                node.right = remove(node.right, (Comparable) temp.data);
                if (node.right != null) node.right.setParent(node);
            }
        }

        // If the tree was empty or the last node was removed, node is null
        if (node == null) {
            return node;
        }

        // 2. Update height of the current node
        updateHeight(node);

        // 3. Get balance factor and balance if necessary
        int balanceFactor = getBalanceFactor(node);

        // Rotations based on the 4 AVL cases
        // Left Left (LL Case)
        if (balanceFactor > 1 && getBalanceFactor(node.left) >= 0) {
            return rotateRight(node);
        }

        // Left Right (LR Case)
        if (balanceFactor > 1 && getBalanceFactor(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Right Right (RR Case)
        if (balanceFactor < -1 && getBalanceFactor(node.right) <= 0) {
            return rotateLeft(node);
        }

        // Right Left (RL Case)
        if (balanceFactor < -1 && getBalanceFactor(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // Auxiliary method to find the node with the minimum value in a subtree
    private AVLNode findMin(AVLNode node) {
        AVLNode current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    public void clear() {
        this.root = null;
        this.size = 0;
    }
}