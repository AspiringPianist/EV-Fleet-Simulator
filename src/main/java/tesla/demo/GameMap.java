package tesla.demo;
// Import Statements
import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * The GameMap class represents a singleton map for a game, managing the road network
 * and providing utility methods for map operations. It loads map data from CSV files
 * and supports operations such as checking walkability, retrieving nodes, and printing
 * the map. The class ensures 1-based indexing for coordinates and provides methods
 * to access map dimensions, obstacle maps, and valid moves between nodes.
 */
public class GameMap {
    private static GameMap instance;
    private int width;
    private int height;
    private Map<String, Node> roadNetwork;
    private RoadMapParser roadMapParser;

    //Constructor
    public GameMap() {
        roadMapParser = new RoadMapParser();
        roadNetwork = new HashMap<>();
        loadMap("src/main/resources/static/map.csv", "src/main/resources/static/signal.csv");
    }

    /**
     * Loads and initializes the game map from CSV files.
     * Parses road and signal data, sets map dimensions, and populates the road network
     * using 1-based coordinate indexing.
     *
     * @param filename Path to the main map CSV file
     * @param signalMapPath Path to the traffic signal map CSV file
     */
    private void loadMap(String filename, String signalMapPath) {
        try {
            roadMapParser.parseCSV(filename, signalMapPath);
            this.height = roadMapParser.getRows();
            this.width = roadMapParser.getCols();
            
            // Store all road nodes in our network using 1-based indexing
            for (Node node : roadMapParser.getAllNodes()) {
                // Ensure coordinates match map_editor.py's 1-based indexing
                roadNetwork.put(node.x + "," + node.y, node);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Returns the singleton instance of the GameMap class.
     * Creates a new instance if one doesn't exist yet.
     * 
     * @return The singleton GameMap instance
     */
    public static GameMap getInstance() {
        if (instance == null) {
            instance = new GameMap();
        }
        return instance;
    }
    /**
     * Checks if a given coordinate is walkable on the game map.
     * Uses 1-based coordinate indexing to check if the position exists in the road network.
     *
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @return true if the coordinate is walkable (exists in road network), false otherwise
     */
    public boolean isWalkable(int x, int y) {
        String key = x + "," + y;  // Already 1-based
        return roadNetwork.containsKey(key);
    }
    /**
     * Creates and returns a 2D boolean array representing the obstacle map of the game.
     * Each cell in the array is true if the corresponding position is an obstacle
     * (not walkable) and false if it is walkable.
     *
     * @return A 2D boolean array where true indicates obstacles and false indicates walkable paths
     */
    public boolean[][] getObstacleMap() {
        boolean[][] obstacleMap = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                obstacleMap[y][x] = !isWalkable(x, y);
            }
        }
        return obstacleMap;
    }

    // @return width of the map 
    public int getWidth() {
        return width;
    }

    // @return height of the map
    public int getHeight() {
        return height;
    }

    // @return road network of the map for debugging purposes
    public void printMap() {
        for (int x = 1; x <= height; x++) {
            for (int y = 1; y <= width; y++) {
                String key = x + "," + y;
                System.out.print(roadNetwork.containsKey(key) ? "[_] " : "[X] ");
            }
            System.out.println();
        }
    }

    /**
     * Retrieves a road node from the road network at the specified coordinates.
     *
     * @param x The x-coordinate of the road node
     * @param y The y-coordinate of the road node
     * @return The Node at the specified coordinates, or null if no node exists at that location
     */
    public Node getRoadNode(int x, int y) {
        return roadNetwork.get((x) + "," + (y));
    }
    /**
     * Retrieves a traffic node from the road network at the specified coordinates.
     * Returns null if no node exists at the location or if the node is not a traffic node.
     *
     * @param x The x-coordinate of the traffic node
     * @param y The y-coordinate of the traffic node
     * @return The TrafficNode at the specified coordinates, or null if no traffic node exists
     */
    public TrafficNode getTrafficNode(int x, int y) {
        String key = (x) + "," + (y);
        Node node = roadNetwork.get(key);
        return (node instanceof TrafficNode) ? (TrafficNode) node : null;
    }
    /**
     * Returns the complete road network of the game map.
     * 
     * @return A Map containing all road nodes, with coordinate strings as keys and Node objects as values
     */
    public Map<String, Node> getRoadNetwork() {
        return roadNetwork;
    }
    /**
     * Prints the road network graph structure using the road map parser.
     * Displays the connectivity and layout of all road nodes in the network.
     * This is NOT a map representation, it is representation of the graph
     */
    public void printRoadNetwork() {
        roadMapParser.printGraph();
    }
    /**
     * Function for debugging purposes.
     * Finds all nodes that can be reached from a given coordinate position.
     * Returns an empty set if no road node exists at the specified coordinates.
     *
     * @param x The x-coordinate of the starting position
     * @param y The y-coordinate of the starting position
     * @return A Set of Node objects that are reachable from the starting position
     */
    public Set<Node> findReachableNodes(int x, int y) {
        Node startNode = getRoadNode(x, y);
        return startNode != null ? roadMapParser.findReachableNodes(startNode) : Collections.emptySet();
    }
}
