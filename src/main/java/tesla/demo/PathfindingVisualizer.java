package tesla.demo;
import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * The PathfindingVisualizer class provides a visualization tool for pathfinding algorithms.
 * It uses JNI to call a native A* pathfinding implementation and visualizes the resulting path
 * on a grid-based map.
 */
public class PathfindingVisualizer {
    private static boolean libraryLoaded = false;

    // Static block to load the native library once
    static {
        if (!libraryLoaded) {
            System.loadLibrary("astar_jni");
            libraryLoaded = true;
        }
    }

    private GameMap map; // Reference to the game map

    /**
     * Native method to find a path in a road network using the A* algorithm.
     *
     * @param startX Starting point's x-coordinate (1-based).
     * @param startY Starting point's y-coordinate (1-based).
     * @param endX Ending point's x-coordinate (1-based).
     * @param endY Ending point's y-coordinate (1-based).
     * @param nodeCoords Array of node coordinates in the network.
     * @param neighborLists Array of neighbor lists corresponding to each node.
     * @return Array representing the path as a sequence of x, y coordinates.
     */
    private native long[] findPathInNetwork(int startX, int startY, int endX, int endY,
                                            int[][] nodeCoords, int[][] neighborLists);

    /**
     * Constructor for PathfindingVisualizer.
     *
     * @param map The GameMap object representing the road network and grid structure.
     */
    public PathfindingVisualizer(GameMap map) {
        this.map = map;
    }

    /**
     * Runs a simulation of pathfinding by taking user input for start and end points,
     * finding the path, and visualizing it on the map.
     */
    public void runPathfindingSimulation() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter start point (x y):");
            int startX = scanner.nextInt();  // 1-based input
            int startY = scanner.nextInt();  // 1-based input

            System.out.println("Enter end point (x y):");
            int endX = scanner.nextInt();    // 1-based input
            int endY = scanner.nextInt();    // 1-based input

            Map<String, Node> roadNetwork = map.getRoadNetwork();
            List<Node> nodes = new ArrayList<>(roadNetwork.values());

            // Prepare node coordinates and neighbor lists for the native method
            int[][] nodeCoords = new int[nodes.size()][2];
            int[][] neighborLists = new int[nodes.size()][];
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                nodeCoords[i] = new int[]{node.x, node.y}; // 1-based coordinates
                neighborLists[i] = node.neighbors.stream()
                        .mapToInt(nodes::indexOf)
                        .toArray();
            }

            // Call the native method and visualize the result
            long[] path = findPathInNetwork(startX, startY, endX, endY, nodeCoords, neighborLists);
            visualizePath(path);
        } finally {
            scanner.close();
        }
    }

    /**
     * Finds the path between two points using the A* algorithm.
     *
     * @param startX Starting point's x-coordinate (1-based).
     * @param startY Starting point's y-coordinate (1-based).
     * @param endX Ending point's x-coordinate (1-based).
     * @param endY Ending point's y-coordinate (1-based).
     * @return Array representing the path as a sequence of x, y coordinates.
     */
    public long[] findPath(int startX, int startY, int endX, int endY) {
        Map<String, Node> roadNetwork = map.getRoadNetwork();
        List<Node> nodes = new ArrayList<>(roadNetwork.values());

        // Prepare node coordinates and neighbor lists for the native method
        int[][] nodeCoords = new int[nodes.size()][2];
        int[][] neighborLists = new int[nodes.size()][];
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            nodeCoords[i] = new int[]{node.x, node.y}; // 1-based coordinates
            neighborLists[i] = node.neighbors.stream()
                    .mapToInt(nodes::indexOf)
                    .toArray();
        }

        // Call the native method and return the result
        return findPathInNetwork(startX, startY, endX, endY, nodeCoords, neighborLists);
    }

    /**
     * Visualizes the path on the map by marking start, end, and path nodes.
     *
     * @param path Array of x, y coordinates representing the path.
     */
    private void visualizePath(long[] path) {
        if (path == null || path.length == 0) {
            System.out.println("No valid path found!");
            return;
        }

        String[][] visualMap = new String[map.getHeight()][map.getWidth()];

        // Initialize the map visualization
        for (int x = 1; x <= map.getHeight(); x++) {
            for (int y = 1; y <= map.getWidth(); y++) {
                visualMap[x - 1][y - 1] = map.isWalkable(x, y) ? "[_] " : "[X] ";
            }
        }

        // Mark the path on the map
        for (int i = 0; i < path.length - 1; i += 2) {
            int x = (int) path[i];
            int y = (int) path[i + 1];
            visualMap[x - 1][y - 1] = "[P] ";
        }

        // Mark the start and end points
        if (path.length >= 4) {
            visualMap[(int) path[0] - 1][(int) path[1] - 1] = "[S] ";
            visualMap[(int) path[path.length - 2] - 1][(int) path[path.length - 1] - 1] = "[G] ";
        }

        // Print the visualized map
        for (String[] row : visualMap) {
            for (String cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }
}
