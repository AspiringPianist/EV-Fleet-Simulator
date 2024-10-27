package tesla.demo;

import java.util.Queue;
import java.util.LinkedList;

public class EV extends GameObject {
    /*
     * Electric Vehicle Class
     * 
     * @author - Unnath
     * Currently has functionality only to charge the EV and see the EVs current
     * charge and type of EV
     */

    // TODO: getDetails()s
    int type = 0;
    int charge = 100;
    int chargingRate = 0;
    int capacity;
    Queue<Task> taskQueue = new LinkedList<Task>();

    public EV(int x, int y, int t, int charge, int chargingRate) {
        super(x, y);
        this.type = t;
        this.charge = charge;
        this.chargingRate = chargingRate;
    }

    public boolean fullCharge() {
        if (this.charge >= 100) {
            this.charge = 100;
            return true;
        }
        return false;
    }

    public Location getLocation() {
        return this.location;
    }

    public void assignTask(Task task) {
        this.taskQueue.add(task);
        task.isAssigned = true;
        task.inProgress = false; // when we pop this task from the queue, we will set this to true
        task.isCompleted = false;
        System.out.println("Task assigned to EV");
    }

    public void charge() {
        // simulates slow charging of every 1 second
        this.charge += this.chargingRate;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
