public class GameObject {
    // This is an abstract class
    Location location;
    String symbol;
    public GameObject(int x, int y) {
        this.location = new Location(x, y);
        this.symbol = "[_]";
    }
    public Object getDetails() {
        return null; // Placeholder return, replace with actual implementation
        // used for rendering on the map
    }
}
