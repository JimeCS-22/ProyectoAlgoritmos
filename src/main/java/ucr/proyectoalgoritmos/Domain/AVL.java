package ucr.proyectoalgoritmos.Domain; // Adjust package as needed, e.g., ucr.proyectoalgoritmos.Domain.avl

import ucr.proyectoalgoritmos.Domain.AVLNode;
import ucr.proyectoalgoritmos.Domain.list.ListException;
import ucr.proyectoalgoritmos.util.Utility; // Assuming you have a Utility class with a compare method

public class AVL {
    private AVLNode root;
    private int count; // To keep track of the number of elements in the tree

    public AVL() {
        this.root = null;
        this.count = 0;
    }

    public int size() {
        return count;
    }

    public void clear() {
        this.root = null;
        this.count = 0;
    }

    public boolean isEmpty() {
        return root == null;
    }

    // --- AVL Tree Helper Methods ---

    // Method to get the height of a node (null node has height -1)
    private int height(AVLNode node) {
        return node == null ? -1 : node.height;
    }

    // Method to calculate the balance factor of a node
    // Balance Factor = height(left subtree) - height(right subtree)
    private int balanceFactor(AVLNode node) {
        return height(node.left) - height(node.right);
    }

    // Method to update the height of a node
    private void updateHeight(AVLNode node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    // --- Rotations for Balancing ---

    // Right Rotation (LL Case)
    private AVLNode rotateRight(AVLNode node) {
        AVLNode newRoot = node.left;
        node.left = newRoot.right;
        if (newRoot.right != null) {
            newRoot.right.parent = node;
        }
        newRoot.right = node;
        newRoot.parent = node.parent; // Set parent of newRoot
        node.parent = newRoot; // Set parent of old root
        updateHeight(node); // Update height of old root (now child)
        updateHeight(newRoot); // Update height of new root
        return newRoot;
    }

    // Left Rotation (RR Case)
    private AVLNode rotateLeft(AVLNode node) {
        AVLNode newRoot = node.right;
        node.right = newRoot.left;
        if (newRoot.left != null) {
            newRoot.left.parent = node;
        }
        newRoot.left = node;
        newRoot.parent = node.parent; // Set parent of newRoot
        node.parent = newRoot; // Set parent of old root
        updateHeight(node); // Update height of old root (now child)
        updateHeight(newRoot); // Update height of new root
        return newRoot;
    }

    // Left-Right Rotation (LR Case)
    private AVLNode rotateLeftRight(AVLNode node) {
        node.left = rotateLeft(node.left);
        return rotateRight(node);
    }

    // Right-Left Rotation (RL Case)
    private AVLNode rotateRightLeft(AVLNode node) {
        node.right = rotateRight(node.right);
        return rotateLeft(node);
    }

    // --- Insertion ---

    public void insert(Object element) throws ListException {
        // We need elements to be comparable for a sorted tree.
        // Assuming your Passenger object (or whatever element is) has a natural ordering
        // or that Utility.compare works for it.
        // If Passenger objects are compared by ID, ensure Utility.compare handles Passenger objects correctly.
        if (element == null) {
            throw new ListException("Cannot insert null element.");
        }
        root = insert(root, element);
        count++;
    }

    private AVLNode insert(AVLNode node, Object element) throws ListException {
        // Base case: If the node is null, we found the insertion point
        if (node == null) {
            return new AVLNode(element);
        }

        int comparison = Utility.compare(element, node.data);

        if (comparison < 0) { // Element is smaller, go left
            AVLNode insertedNode = insert(node.left, element);
            node.left = insertedNode;
            insertedNode.parent = node;
        } else if (comparison > 0) { // Element is larger, go right
            AVLNode insertedNode = insert(node.right, element);
            node.right = insertedNode;
            insertedNode.parent = node;
        } else {
            // Element already exists (assuming no duplicates, based on standard AVL behavior for unique keys)
            // If duplicates are allowed, you'd modify this logic (e.g., add to a list in the node).
            throw new ListException("Duplicate element: " + element);
        }

        // Update height of current node
        updateHeight(node);

        // Balance the node
        int balance = balanceFactor(node);

        // Left Left Case
        if (balance > 1 && Utility.compare(element, node.left.data) < 0) {
            return rotateRight(node);
        }
        // Right Right Case
        if (balance < -1 && Utility.compare(element, node.right.data) > 0) {
            return rotateLeft(node);
        }
        // Left Right Case
        if (balance > 1 && Utility.compare(element, node.left.data) > 0) {
            return rotateLeftRight(node);
        }
        // Right Left Case
        if (balance < -1 && Utility.compare(element, node.right.data) < 0) {
            return rotateRightLeft(node);
        }

        return node; // Return the (potentially rebalanced) node
    }

    // --- Search ---

    public Object search(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("AVL tree is empty.");
        }
        return search(root, element);
    }

    private Object search(AVLNode node, Object element) {
        if (node == null) {
            return null; // Element not found
        }

        int comparison = Utility.compare(element, node.data);

        if (comparison < 0) {
            return search(node.left, element);
        } else if (comparison > 0) {
            return search(node.right, element);
        } else {
            return node.data; // Element found
        }
    }

    // --- Deletion (Optional - often more complex) ---
    /*
    public void delete(Object element) throws ListException {
        if (isEmpty()) {
            throw new ListException("AVL tree is empty. Cannot delete " + element);
        }
        root = delete(root, element);
    }

    private AVLNode delete(AVLNode node, Object element) throws ListException {
        if (node == null) { // Element not found
            return node;
        }

        int comparison = Utility.compare(element, node.data);

        if (comparison < 0) {
            node.left = delete(node.left, element);
            if (node.left != null) node.left.parent = node;
        } else if (comparison > 0) {
            node.right = delete(node.right, element);
            if (node.right != null) node.right.parent = node;
        } else { // Node to be deleted found
            // Case 1: Node with no children or one child
            if (node.left == null || node.right == null) {
                AVLNode temp = (node.left != null) ? node.left : node.right;

                if (temp == null) { // No child case
                    temp = node;
                    node = null;
                } else { // One child case
                    node = temp;
                    node.parent = temp.parent; // Update parent correctly
                }
                count--;
            } else {
                // Case 2: Node with two children (get inorder successor)
                AVLNode temp = findMin(node.right);
                node.data = temp.data; // Copy the inorder successor's data to this node
                node.right = delete(node.right, temp.data); // Delete the inorder successor
                if (node.right != null) node.right.parent = node; // Update parent
            }
        }

        if (node == null) { // If the node was deleted and it was a leaf/root
            return node;
        }

        // Update height of current node
        updateHeight(node);

        // Balance the node
        int balance = balanceFactor(node);

        // Left Left Case
        if (balance > 1 && balanceFactor(node.left) >= 0) {
            return rotateRight(node);
        }
        // Left Right Case
        if (balance > 1 && balanceFactor(node.left) < 0) {
            return rotateLeftRight(node);
        }
        // Right Right Case
        if (balance < -1 && balanceFactor(node.right) <= 0) {
            return rotateLeft(node);
        }
        // Right Left Case
        if (balance < -1 && balanceFactor(node.right) > 0) {
            return rotateRightLeft(node);
        }
        return node;
    }

    private AVLNode findMin(AVLNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
    */

    // --- Traversal Methods (Optional, but useful for debugging/display) ---

    public String inOrder() {
        if (isEmpty()) return "AVL tree is empty.";
        StringBuilder sb = new StringBuilder();
        inOrder(root, sb);
        return sb.toString().trim();
    }

    private void inOrder(AVLNode node, StringBuilder sb) {
        if (node != null) {
            inOrder(node.left, sb);
            sb.append(node.data).append(" ");
            inOrder(node.right, sb);
        }
    }

    // You can add preOrder, postOrder, etc. as needed

    @Override
    public String toString() {
        return inOrder(); // Default to in-order traversal for string representation
    }
}