package tesla.demo;

public class Task {
    public Location startLocation;
    public Location endLocation;
    public int capacityRequirement;
    public String customerName = "Dummy"; // we are using customerName so that at any point of time customer
    // can see how much of his tasks are done
    // TODO: customer task status dashboard
    public boolean isAssigned = false;
    public boolean inProgress = false;
    public boolean isCompleted = false;

    public Task(Location startLocation, Location endLocation, int capacityRequirement, String customerName) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.capacityRequirement = capacityRequirement;
        this.customerName = customerName;
    }

    // Getters and setters
    public Location getStartLocation() {
        return startLocation;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public int getCapacityRequirement() {
        return capacityRequirement;
    }
}
