package tesla.demo;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
/**
 * Represents a node in a graph with x,y coordinates and connection capabilities.
 * Contains information about node type and stall status, and maintains a list
 * of neighboring nodes. Implements equality based on coordinate position.
 */
class Node {
    int x, y;
    private boolean stalled;
    List<Node> neighbors;
    public String type;
    //Constructor
    public Node(int x, int y,String type) {
        this.x = x;
        this.y = y;
        this.type=type;
        this.neighbors = new ArrayList<>();
        this.stalled = false;
    }
    
    /**
     * Checks if the node is currently in a stalled state.
     * @return true if the node is stalled (blocked), false otherwise
     */   
    public boolean isStalled() {
        return stalled;
    }

    /**
     * Sets the stalled state of the node.
     * @param stalled true to mark the node as stalled, false otherwise
     */
    public void setStalled(boolean stalled) {
        this.stalled = stalled;
    }

    /**
     * Checks if two Node objects are equal based on their x and y coordinates.
     * @param o the object to compare with
     * @return true if the objects have the same coordinates, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }

    /**
     * Generates a hash code for the node based on its x and y coordinates.
     * @return hash code value for the node
     */
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    /**
     * Returns a string representation of the node in the format "(x,y)".
     * @return coordinate pair as a string
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
