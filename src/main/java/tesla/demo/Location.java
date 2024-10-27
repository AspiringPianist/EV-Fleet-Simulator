package tesla.demo;

public class Location {
    // x and y coordinates on grid
    int x;
    int y;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Location other) {
        // Implement distance calculation logic
        // You can use the Euclidean distance formula or any other suitable method
        // based on your requirements
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        // TODO: replace this with actual distance calculations
    }
}
