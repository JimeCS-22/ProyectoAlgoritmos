package domain;

import java.util.Objects; // Import for Objects.hash

public class EdgeWeight {
    private Object edge; // This represents the target vertex's data for the edge
    private Object weight; // The weight of the edge

    public EdgeWeight(Object edge, Object weight) {
        this.edge = edge;
        this.weight = weight;
    }

    public Object getEdge() {
        return edge;
    }

    public void setEdge(Object edge) {
        this.edge = edge;
    }

    public Object getWeight() {
        return weight;
    }

    public void setWeight(Object weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        if(weight==null) return "Edge="+edge;
        else return "Edge="+edge+". Weight="+weight;
    }

    // IMPORTANT: Add this equals method
    // It defines when two EdgeWeight objects are considered "equal".
    // For an edge, equality is typically based on the target vertex it points to.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeWeight that = (EdgeWeight) o;
        // Assuming util.Utility.compare is available and correctly compares Objects
        return util.Utility.compare(this.edge, that.edge) == 0;
        // If util.Utility.compare is not available or suitable, use Objects.equals
        // return Objects.equals(this.edge, that.edge);
    }

    // IMPORTANT: Add this hashCode method (required when overriding equals)
    @Override
    public int hashCode() {
        return Objects.hash(edge);
    }
}