package tesla.demo;

import java.io.IOException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class GameMap {
    private static GameMap instance;
    private int width;
    private int height;
    private Map<String, Node> roadNetwork;
    private RoadMapParser roadMapParser;

    public GameMap() {
        roadMapParser = new RoadMapParser();
        roadNetwork = new HashMap<>();
        loadMap("src/main/resources/static/map.csv", "src/main/resources/static/signal.csv");
    }

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

    public static GameMap getInstance() {
        if (instance == null) {
            instance = new GameMap();
        }
        return instance;
    }

    // public GameObject getObject(int x, int y) {
    //     String key = (x + 1) + "," + (y + 1);
    //     if (roadNetwork.containsKey(key)) {
    //         return new GameObject(x, y);
    //     }
    //     return new Obstacle(x, y);
    // }

    public boolean isWalkable(int x, int y) {
        String key = x + "," + y;  // Already 1-based
        return roadNetwork.containsKey(key);
    }

    public boolean[][] getObstacleMap() {
        boolean[][] obstacleMap = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                obstacleMap[y][x] = !isWalkable(x, y);
            }
        }
        return obstacleMap;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void printMap() {
        for (int x = 1; x <= height; x++) {
            for (int y = 1; y <= width; y++) {
                String key = x + "," + y;
                System.out.print(roadNetwork.containsKey(key) ? "[_] " : "[X] ");
            }
            System.out.println();
        }
    }

    public Node getRoadNode(int x, int y) {
        return roadNetwork.get((x) + "," + (y));
    }

    public TrafficNode getTrafficNode(int x, int y) {
        String key = (x) + "," + (y);
        Node node = roadNetwork.get(key);
        return (node instanceof TrafficNode) ? (TrafficNode) node : null;
    }
    public List<Node> getValidMoves(int x, int y) {
        Node currentNode = getRoadNode(x, y);
        return currentNode != null ? currentNode.neighbors : Collections.emptyList();
    }

    public boolean isValidMove(int fromX, int fromY, int toX, int toY) {
        Node fromNode = getRoadNode(fromX, fromY);
        Node toNode = getRoadNode(toX, toY);
        return fromNode != null && fromNode.neighbors.contains(toNode);
    }

    public Map<String, Node> getRoadNetwork() {
        return roadNetwork;
    }

    public void printRoadNetwork() {
        roadMapParser.printGraph();
    }

    public Set<Node> findReachableNodes(int x, int y) {
        Node startNode = getRoadNode(x, y);
        return startNode != null ? roadMapParser.findReachableNodes(startNode) : Collections.emptySet();
    }
}
