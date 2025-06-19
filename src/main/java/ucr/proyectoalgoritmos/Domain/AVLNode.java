// Assuming this is your AVLNode.java or inner class
package ucr.proyectoalgoritmos.Domain; // Adjust package if AVLNode is in a different one

public class AVLNode {
    Object data;
    AVLNode left;
    AVLNode right;
    AVLNode parent; // Crucial for re-linking after rotations
    int height;

    public AVLNode(Object data) {
        this.data = data;
        this.left = null;
        this.right = null;
        this.parent = null; // Initially no parent
        this.height = 0; // Height of a new node (leaf) is 0
    }

    // Constructor with parent (optional, but good for linking during insertion)
    public AVLNode(Object data, AVLNode parent) {
        this.data = data;
        this.left = null;
        this.right = null;
        this.parent = parent;
        this.height = 0;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public AVLNode getLeft() {
        return left;
    }

    public void setLeft(AVLNode left) {
        this.left = left;
    }

    public AVLNode getRight() {
        return right;
    }

    public void setRight(AVLNode right) {
        this.right = right;
    }

    public AVLNode getParent() {
        return parent;
    }

    public void setParent(AVLNode parent) {
        this.parent = parent;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}