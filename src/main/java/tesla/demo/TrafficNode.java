package tesla.demo;
import java.util.*;

/**
 * The TrafficNode class represents a traffic node in a road map, 
 * capable of managing traffic signals and identifying paired nodes.
 * It extends the Node class and adds functionality for signal management 
 * and traffic control.
 */
public class TrafficNode extends Node {
    private int signal; // Current signal state (e.g., 0 for green, others for red)
    public int group; // Group identifier for the traffic node

    /**
     * Constructor to initialize a TrafficNode with coordinates, type, and signal state.
     *
     * @param x The x-coordinate of the node.
     * @param y The y-coordinate of the node.
     * @param type The type of the node ("TrafficNode").
     * @param trafficType The initial signal state of the traffic node.
     */
    public TrafficNode(int x, int y, String type, int trafficType) {
        super(x, y, type);
        this.signal = trafficType;
    }

    /**
     * Toggles the signal state of the traffic node.
     * The signal cycles through 0, 1, 2, and 3.
     */
    public void changeSignal() {
        signal = (signal + 1) % 4;
    }

    /**
     * Checks if the traffic signal is green (signal state 0).
     *
     * @return True if the signal is green, otherwise false.
     */
    public boolean isGreen() {
        return this.signal == 0;
    }

    /**
     * Checks if the traffic signal is red (any signal state other than 0).
     *
     * @return True if the signal is red, otherwise false.
     */
    public boolean isRed() {
        return this.signal != 0;
    }

    /**
     * Finds the paired traffic node for the current node, if any.
     * A pair is determined based on predefined coordinate offsets 
     * and matching signal states.
     *
     * @param currentNode The current TrafficNode for which to find a pair.
     * @return The paired TrafficNode if found, otherwise null.
     */
    public TrafficNode get_pair(TrafficNode currentNode) {
        Node pairNode;
        List<int[]> coordinateOffsets = Arrays.asList(
            new int[]{1, 3},
            new int[]{-3, 1},
            new int[]{-1, -3},
            new int[]{3, -1}
        );

        for (int[] offset : coordinateOffsets) {
            pairNode = GameMap.getInstance().getTrafficNode(x + offset[0], y + offset[1]);
            if (pairNode instanceof TrafficNode &&
                ((TrafficNode) pairNode).type.equals("TrafficNode") &&
                ((TrafficNode) pairNode).signal == currentNode.signal) {
                return (TrafficNode) pairNode;
            }
        }
        return null;
    }
}
