package ucr.proyectoalgoritmos.Domain.Circular;

public class Node {
    public Object data;
    public Node prev; //apuntador al nodo anterior
    public Node next; //apuntador al nodo siguiente

    //Constructor 1
    public Node(Object data) {
        this.data = data;
        this.prev = this.next = null; //puntero al sgte nodo es nulo por default
    }

    //Constructor 2
    public Node() {
        this.prev = this.next = null;
    }

}
