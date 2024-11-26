package tesla.demo;

import org.springframework.boot.autoconfigure.sql.init.SqlR2dbcScriptDatabaseInitializer;

public class Task {
    int start_x;
    int start_y;
    int end_x;
    int end_y;
    String task;
    Task(String task,int start_x,int start_y,int end_x,int end_y){
        this.start_x=start_x;
        this.end_x=end_x;
        this.start_y=start_y;
        this.end_y=end_y;
        this.task=task;
    }
    public String getTask() {
        return task;
    }
    public int getStartX() {
        return start_x;
    }
    public int getStartY() {
        return start_y;
    }

    public int getEndX() {
        return end_x;
    }
    public int getEndY() {
        return end_y;
    }

    // public Location startLocation;
    // public Location endLocation;
    // public int capacityRequirement;
    // public String customerName = "Dummy"; // we are using customerName so that at any point of time customer
    // // can see how much of his tasks are done
    // // TODO: customer task status dashboard
    // public boolean isAssigned = false;
    // public boolean inProgress = false;
    // public boolean isCompleted = false;

    // public Task(Location startLocation, Location endLocation, int capacityRequirement, String customerName) {
    //     this.startLocation = startLocation;
    //     this.endLocation = endLocation;
    //     this.capacityRequirement = capacityRequirement;
    //     this.customerName = customerName;
    // }

    // // Getters and setters
    // public Location getStartLocation() {
    //     return startLocation;
    // }

    // public Location getEndLocation() {
    //     return endLocation;
    // }

    // public int getCapacityRequirement() {
    //     return capacityRequirement;
    // }
}
