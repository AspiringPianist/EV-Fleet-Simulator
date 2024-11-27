package tesla.demo;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * The TrafficManager class manages traffic signals and their behavior in a simulated environment.
 * It controls traffic nodes, manages signal updates, and ensures safe movement of vehicles (EVs) within the simulation.
 */
public class TrafficManager {
    public static TrafficManager instance; // Singleton instance
    private static final long SIGNAL_CHANGE_INTERVAL = 5000; // Signal change interval in milliseconds (5 seconds)
    public static ArrayList<TrafficNode> trafficLights = new ArrayList<>(); // List of traffic lights
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // Scheduler for signal updates
    private static long nextSignalChangeTime; // Time when the next signal change will occur

    /**
     * Constructor initializes the traffic node map.
     */
    public TrafficManager() {
        //trafficNodeToCompletionNode = new HashMap<>();
    }

    /**
     * Retrieves the singleton instance of TrafficManager.
     * Initializes the instance and starts the traffic cycle if it does not already exist.
     *
     * @return The singleton instance of TrafficManager.
     */
    public static TrafficManager getInstance() {
        if (instance == null) {
            instance = new TrafficManager();
            instance.startTrafficCycle();
        }
        return instance;
    }

    /**
     * Adds a TrafficNode to the list of managed traffic lights.
     *
     * @param node The TrafficNode to be added.
     */
    public void addTrafficNode(TrafficNode node) {
        trafficLights.add(node);
    }

    /**
     * Updates the signals for all traffic nodes by changing their state.
     */
    public void updateSignals() {
        trafficLights.forEach(TrafficNode::changeSignal);
    }

    /**
     * Determines if an EV (Electric Vehicle) can safely move to a target position.
     * Checks traffic light signals and the state of the target position.
     *
     * @param ev The EV attempting to move.
     * @param targetX The x-coordinate of the target position.
     * @param targetY The y-coordinate of the target position.
     * @return True if the EV can move to the target position, false otherwise.
     */
    public boolean canMoveToPosition(EV ev, int targetX, int targetY) {
        PathNode currentPos = ev.getPath().get(ev.currentPathIndex);

        // Check if the current position is at a traffic node
        TrafficNode currentTrafficNode = GameMap.getInstance().getTrafficNode(
                currentPos.getX(),
                currentPos.getY());

        // If at or approaching a traffic node, verify timing for safe crossing
        if (currentTrafficNode != null || GameMap.getInstance().getTrafficNode(targetX, targetY) != null) {
            long currentTime = System.currentTimeMillis();
            long timeUntilChange = nextSignalChangeTime - currentTime;

            // Estimate time required to cross the intersection
            long timeNeededToCross = 2 * ev.getMoveInterval();

            if (timeUntilChange < timeNeededToCross) {
                return false;
            }
        }

        if (currentTrafficNode == null) {
            if (GameMap.getInstance().getRoadNode(targetX, targetY).isStalled()) {
                return false;
            } else {
                GameMap.getInstance().getRoadNode(ev.getCurrentX(), ev.getCurrentY()).setStalled(false); // Unmark current position
                GameMap.getInstance().getRoadNode(targetX, targetY).setStalled(true); // Mark target position as stalled
                return true;
            }
        }

        // Verify the target position's traffic signal status
        TrafficNode targetTrafficNode = GameMap.getInstance().getTrafficNode(targetX, targetY);
        if (targetTrafficNode != null && !targetTrafficNode.isGreen()) {
            return false;
        }

        // Verify the current position's traffic signal status
        if (!currentTrafficNode.isGreen()) {
            return false;
        }

        GameMap.getInstance().getRoadNode(ev.getCurrentX(), ev.getCurrentY()).setStalled(false); // Unmark current position
        GameMap.getInstance().getRoadNode(targetX, targetY).setStalled(true); // Mark target position as stalled
        return true;
    }

    /**
     * Starts the traffic signal cycle. Signals are updated at a fixed interval.
     */
    public void startTrafficCycle() {
        scheduler.scheduleAtFixedRate(() -> {
            changeSignals();
        }, 0, SIGNAL_CHANGE_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Changes the state of all traffic signals and updates the time for the next change.
     */
    public static void changeSignals() {
        long currentTime = System.currentTimeMillis();
        nextSignalChangeTime = currentTime + SIGNAL_CHANGE_INTERVAL;

        for (TrafficNode node : trafficLights) {
            node.changeSignal();
        }
    }

    /**
     * Shuts down the traffic signal scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
    }
}
