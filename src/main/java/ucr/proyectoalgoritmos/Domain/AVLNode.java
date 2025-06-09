package ucr.proyectoalgoritmos.Domain; // Adjust package to where your Node/List classes are, or create a new one for AVL

public class AVLNode {
    Object data;
    AVLNode left;
    AVLNode right;
    AVLNode parent; // Useful for traversals and some operations
    int height; // Height of the node in the AVL tree

    public AVLNode(Object data) {
        this.data = data;
        this.left = this.right = this.parent = null;
        this.height = 0; // A new node starts with height 0 (or 1 depending on convention)
    }

    // Getters for data, left, right, parent, height
    public Object getData() {
        return data;
    }

    public AVLNode getLeft() {
        return left;
    }

    public AVLNode getRight() {
        return right;
    }

    public AVLNode getParent() {
        return parent;
    }

    public int getHeight() {
        return height;
    }

    // Setters (though generally AVL logic handles these)
    public void setData(Object data) {
        this.data = data;
    }

    public void setLeft(AVLNode left) {
        this.left = left;
    }

    public void setRight(AVLNode right) {
        this.right = right;
    }

    public void setParent(AVLNode parent) {
        this.parent = parent;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}