package tesla.demo;
import java.util.*;

/**
 * Represents an Electric Vehicle (EV) in the simulation. 
 * The EV has attributes like position, type, charge level, and movement behavior.
 * It can follow a path, charge its battery, and handle tasks assigned to it.
 */
public class EV {
    private String name; // Name of the EV
    private int startX; // Starting x-coordinate
    private int startY; // Starting y-coordinate
    private int endX; // Destination x-coordinate
    private int endY; // Destination y-coordinate
    private int type; // Type of the EV
    private int charge; // Current charge level (percentage)
    private int chargingRate; // Rate of charge per interval
    private List<PathNode> path; // Path the EV follows
    public int currentPathIndex; // Current index in the path
    private boolean moving = false; // Whether the EV is moving
    public Task task; // Current assigned task
    private static final long MOVE_INTERVAL = 500; // Interval between movements (in milliseconds)
    Queue<Task> taskQueue = new LinkedList<Task>(); // Queue of tasks for the EV

    /**
     * Constructor to initialize an EV with its starting position, type, charge level, and charging rate.
     *
     * @param x           The starting x-coordinate of the EV.
     * @param y           The starting y-coordinate of the EV.
     * @param type        The type of the EV.
     * @param charge      The initial charge level of the EV (percentage).
     * @param chargingRate The charging rate of the EV per interval.
     */
    public EV(int x, int y, int type, int charge, int chargingRate) {
        this.startX = x;
        this.startY = y;
        this.type = type;
        this.charge = charge;
        this.chargingRate = chargingRate;
        this.currentPathIndex = 0;
        this.task = null;
    }

    // Getters and Setters

    /**
     * @return The name of the EV.
     */
    public String getName() { 
        return name; 
    }

    /**
     * Sets the name of the EV.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The starting x-coordinate of the EV.
     */
    public int getStartX() { 
        return startX; 
    }

    /**
     * @return The starting y-coordinate of the EV.
     */
    public int getStartY() { 
        return startY;
     }

    /**
     * @return The destination x-coordinate of the EV.
     */
    public int getEndX() { 
        return endX; 
    }

    /**
     * @return The destination y-coordinate of the EV.
     */
    public int getEndY() { 
        return endY; 
    }

    /**
     * @return The type of the EV.
     */
    public int getType() { 
        return type; 
    }

    /**
     * @return The current charge level of the EV.
     */
    public int getCharge() { 
        return charge; 
    }

    /**
     * Sets the path for the EV to follow.
     *
     * @param path The list of PathNode objects representing the path.
     */
    public void setPath(List<PathNode> path) { 
        this.path = path;
     }

    /**
     * @return The path the EV is following.
     */
    public List<PathNode> getPath() { 
        return this.path; 
    }

    /**
     * @return The current index of the EV in its path.
     */
    public int getCurrentPathIndex() { 
        return currentPathIndex;
     }

    /**
     * @return The interval between movements in milliseconds.
     */
    public long getMoveInterval() { 
        return MOVE_INTERVAL;
     }

    // Movement

    /**
     * Moves the EV to the next position in its path if it has not reached the end.
     * Prints the EV's new position or a message if it has completed its path.
     */
    public void moveToNextPosition() {
        if (currentPathIndex < path.size() - 1) {
            currentPathIndex++;
            PathNode nextPosition = path.get(currentPathIndex);
            System.out.println("EV moved to position: (" + nextPosition.getX() + ", " + nextPosition.getY() + ")");
        } else {
            System.out.println("EV has reached the end of its path.");
        }
    }

    /**
     * Sets the end location (destination) of the EV.
     *
     * @param endX The destination x-coordinate.
     * @param endY The destination y-coordinate.
     */
    public void setEndLocation(int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
    }

    // Charging

    /**
     * Charges the EV. The charge level increases by the charging rate up to a maximum of 100%.
     */
    public void charge() {
        this.charge += this.chargingRate;
        if (this.charge > 100) {
            this.charge = 100;
        }
        try {
            Thread.sleep(1000); // Simulate charging interval
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the EV is fully charged.
     *
     * @return True if the charge is at or above 100%, false otherwise.
     */
    public boolean fullCharge() {
        if (this.charge >= 100) {
            this.charge = 100;
            return true;
        }
        return false;
    }

    // Position and Movement State

    /**
     * @return The x-coordinate of the EV's current position.
     */
    public int getCurrentX() { 
        return getPath().get(currentPathIndex).getX(); 
    }

    /**
     * @return The y-coordinate of the EV's current position.
     */
    public int getCurrentY() { 
        return getPath().get(currentPathIndex).getY(); 
    }

    /**
     * @return True if the EV is currently moving, false otherwise.
     */
    public boolean isMoving() { 
        return moving;
     }

    /**
     * Sets the movement state of the EV.
     *
     * @param moving True if the EV should start moving, false otherwise.
     */
    public void setMoving(boolean moving) {
         this.moving = moving; 
        }

    // Task Management

    /**
     * Assigns a task to the EV using the TaskAssigner and starts moving.
     */
    public void getTask() {
        task = TaskAssigner.assignTask();
        setMoving(true);
    }
}
