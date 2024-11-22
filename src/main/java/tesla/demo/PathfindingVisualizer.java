package tesla.demo;

import java.util.Scanner;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
public class PathfindingVisualizer {
    private static boolean libraryLoaded = false;
    
    static {
        if (!libraryLoaded) {
            System.loadLibrary("astar_jni");
            libraryLoaded = true;
        }
    }
    
    private GameMap map;
    
    private native long[] findPathInNetwork(int startX, int startY, int endX, int endY, 
                                          int[][] nodeCoords, int[][] neighborLists);

    public PathfindingVisualizer(GameMap map) {
        this.map = map;
    }

    public void runPathfindingSimulation() {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("Enter start point (x y):");
            int startX = scanner.nextInt();  // Already 1-based input
            int startY = scanner.nextInt();  // Already 1-based input

            System.out.println("Enter end point (x y):");
            int endX = scanner.nextInt();    // Already 1-based input
            int endY = scanner.nextInt();    // Already 1-based input

            Map<String, Node> roadNetwork = map.getRoadNetwork();
            List<Node> nodes = new ArrayList<>(roadNetwork.values());
            
            int[][] nodeCoords = new int[nodes.size()][2];
            int[][] neighborLists = new int[nodes.size()][];
            
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                nodeCoords[i] = new int[]{node.x, node.y}; // Already 1-based from RoadMapParser
                neighborLists[i] = node.neighbors.stream()
                    .mapToInt(nodes::indexOf)
                    .toArray();
            }

            long[] path = findPathInNetwork(startX, startY, endX, endY, nodeCoords, neighborLists);
            visualizePath(path);
        } finally {
            scanner.close();
        }
    }

    public long [] findPath(int startX, int startY, int endX, int endY) {
            Map<String, Node> roadNetwork = map.getRoadNetwork();
            List<Node> nodes = new ArrayList<>(roadNetwork.values());
            
            int[][] nodeCoords = new int[nodes.size()][2];
            int[][] neighborLists = new int[nodes.size()][];
            
            for (int i = 0; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                nodeCoords[i] = new int[]{node.x, node.y}; // Already 1-based from RoadMapParser
                neighborLists[i] = node.neighbors.stream()
                    .mapToInt(nodes::indexOf)
                    .toArray();
            }

            long[] path = findPathInNetwork(startX, startY, endX, endY, nodeCoords, neighborLists);
            return path;
    }

    private void visualizePath(long[] path) {
        if (path == null || path.length == 0) {
            System.out.println("No valid path found!");
            return;
        }
    
        String[][] visualMap = new String[map.getHeight()][map.getWidth()];
    
        // Initialize the map with 1-based indexing
        for (int x = 1; x <= map.getHeight(); x++) {
            for (int y = 1; y <= map.getWidth(); y++) {
                visualMap[x-1][y-1] = map.isWalkable(x, y) ? "[_] " : "[X] ";
            }
        }
    
        // Mark the path (coordinates are 1-based)
        for (int i = 0; i < path.length - 1; i += 2) {
            int x = (int) path[i];
            int y = (int) path[i + 1];
            visualMap[x-1][y-1] = "[P] ";
        }
    
        // Mark start and end points
        if (path.length >= 4) {
            visualMap[(int)path[0]-1][(int)path[1]-1] = "[S] ";
            visualMap[(int)path[path.length-2]-1][(int)path[path.length-1]-1] = "[G] ";
        }
    
        // Print the map
        for (String[] row : visualMap) {
            for (String cell : row) {
                System.out.print(cell);
            }
            System.out.println();
        }
    }
    
}
