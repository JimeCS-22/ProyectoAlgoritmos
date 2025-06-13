package ucr.proyectoalgoritmos.Domain;

public class AVLNode {
    public Object element;
    Object data;
    AVLNode left;
    AVLNode right;
    AVLNode parent;
    int height;

    public AVLNode(Object data) {
        this.data = data;
        this.left = this.right = this.parent = null;
        this.height = 0;
    }

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