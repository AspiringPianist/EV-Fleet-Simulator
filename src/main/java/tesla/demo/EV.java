package tesla.demo;
import java.util.*;

public class EV {
    private String name;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int type;
    private int charge;
    private int chargingRate;
    private List<PathNode> path;
    public int currentPathIndex;

    Queue<Task> taskQueue = new LinkedList<Task>(); 
    
    public EV(int x, int y, int type, int charge, int chargingRate) {
        this.startX = x;
        this.startY = y;
        this.type = type;
        this.charge = charge;
        this.chargingRate = chargingRate;
    }

    // Getters
    public String getName() { return name; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getEndX() { return endX; }
    public int getEndY() { return endY; }
    public int getType() { return type; }
    public int getCharge() { return charge; }
    public void setPath(List<PathNode> path) {this.path = path;}
    public List<PathNode> getPath() {return this.path;}
    
    public void setName(String name) {
        this.name = name;
    }

    public void setEndLocation(int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
    }

    // public void addToPath(int x, int y) {
    //     path.add(new int[]{x, y});
    // }

    // public void clearPath() {
    //     path.clear();
    // }

    public boolean fullCharge() {
        if (this.charge >= 100) {
            this.charge = 100;
            return true;
        }
        return false;
    }

    public void charge() {
        this.charge += this.chargingRate;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    // public boolean canMove() {
    //     if (path == null || path.isEmpty()) {
    //         return false;
    //     }
        
    //     PathNode nextPosition = path.get(currentPathIndex + 1);
    //     return GameMap.getInstance().getTrafficManager().move(this, nextPosition);
    // }
    
    // public void moveToNextPosition() {
    //     if (currentPathIndex < path.size() - 1) {
    //         currentPathIndex++;
    //         PathNode newPos = path.get(currentPathIndex);
    //         this.startX = newPos.x;
    //         this.startY = newPos.y;
            
    //         // If we've reached destination
    //         if (currentPathIndex == path.size() - 1) {
    //             this.reachedDestination = true;
    //         }
    //     }
    // }
}