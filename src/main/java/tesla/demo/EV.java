package tesla.demo;

import java.util.Queue;
import java.util.LinkedList;

public class EV {
    private String name;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int type;
    private int charge;
    private int chargingRate;
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
    
    public void setName(String name) {
        this.name = name;
    }

    public void setEndLocation(int endX, int endY) {
        this.endX = endX;
        this.endY = endY;
    }

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
}
