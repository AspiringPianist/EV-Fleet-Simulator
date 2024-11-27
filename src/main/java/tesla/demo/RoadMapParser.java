package tesla.demo;
// Import statements
import java.io.*;
import java.util.*;

/**
 * A parser class for generating a road map graph based on CSV files.
 * The class parses a map CSV and a signal CSV to create a network of nodes
 * and edges, where some nodes can be traffic nodes with associated signals.
 */
public class RoadMapParser {
    private Map<String, Node> nodes; // Key: "x,y", Value: Node
    private int rows; // Number of rows in the map grid
    private int cols; // Number of columns in the map grid

    /**
     * Constructor to initialize the RoadMapParser with an empty node map.
     */
    public RoadMapParser() {
        this.nodes = new HashMap<>();
    }

    /**
     * Gets the number of rows in the grid.
     * @return The number of rows.
     */
    public int getRows() {
        return this.rows;
    }

    /**
     * Gets the number of columns in the grid.
     * @return The number of columns.
     */
    public int getCols() {
        return this.cols;
    }

    /**
     * Parses the map and signal CSV files to construct the road network graph.
     * @param filePath Path to the map CSV file.
     * @param signalMapPath Path to the signal CSV file.
     * @throws IOException If an error occurs while reading the files.
     */
    public void parseCSV(String filePath, String signalMapPath) throws IOException {
        List<String[]> grid = new ArrayList<>();
        List<String[]> signalGrid = new ArrayList<>();
        
        // Read map CSV
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "").trim();
                }
                grid.add(row);
            }
        }
        
        // Read signal CSV
        try (BufferedReader br = new BufferedReader(new FileReader(signalMapPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "").trim();
                }
                signalGrid.add(row);
            }
        }
        
        // Initialize grid dimensions
        this.rows = grid.size();
        this.cols = grid.get(0).length;

        // Construct nodes and edges
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String cellValue = grid.get(i)[j];
                String signalCellValue = signalGrid.get(i)[j];
                if (!cellValue.equals("0")) {
                    Node currentNode;
                    if (!signalCellValue.equals("0")) {
                        currentNode = getOrCreateNode(i + 1, j + 1, "TrafficNode", Integer.parseInt(signalCellValue) - 1);
                    } else {
                        currentNode = getOrCreateNode(i + 1, j + 1, "Node", -1);
                    }
                    
                    // Parse and connect neighbors
                    List<int[]> coordinates = parseCoordinates(cellValue);
                    for (int[] coord : coordinates) {
                        String destSignalValue = signalGrid.get(coord[0] - 1)[coord[1] - 1];
                        Node destNode;
                        if (!destSignalValue.equals("0")) {
                            destNode = getOrCreateNode(coord[0], coord[1], "TrafficNode", Integer.parseInt(destSignalValue) - 1);
                        } else {
                            destNode = getOrCreateNode(coord[0], coord[1], "Node", -1);
                        }
                        currentNode.neighbors.add(destNode);
                    }
                }
            }
        }
    }

    /**
     * Parses coordinate strings and converts them into a list of integer arrays.
     * @param cellValue The cell value containing coordinates.
     * @return A list of coordinate pairs as integer arrays.
     */
    private List<int[]> parseCoordinates(String cellValue) {
        List<int[]> coordinates = new ArrayList<>();
        String[] parts = cellValue.split("\\),\\(|\\(|\\)");
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            String[] coords = part.split(",");
            if (coords.length >= 2) {
                try {
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    coordinates.add(new int[]{x, y});
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return coordinates;
    }

    /**
     * Retrieves or creates a node with the given coordinates and type.
     * @param x The x-coordinate of the node.
     * @param y The y-coordinate of the node.
     * @param type The type of the node ("Node" or "TrafficNode").
     * @param trafficType The traffic type for traffic nodes.
     * @return The retrieved or newly created node.
     */
    private Node getOrCreateNode(int x, int y, String type, int trafficType) {
        String key = x + "," + y;
        if (type.equals("TrafficNode")) {
            return nodes.computeIfAbsent(key, k -> {
                TrafficNode newNode = new TrafficNode(x, y, "TrafficNode", trafficType);
                TrafficManager.getInstance().addTrafficNode(newNode);
                return newNode;
            });
        }
        return nodes.computeIfAbsent(key, k -> new Node(x, y, type));
    }

    /**
     * Prints the entire road network graph with nodes and their neighbors.
     */
    public void printGraph() {
        System.out.println("Road Network Graph:");
        List<Node> sortedNodes = new ArrayList<>(nodes.values());
        sortedNodes.sort((a, b) -> {
            if (a.x != b.x) return a.x - b.x;
            return a.y - b.y;
        });
        
        for (Node node : sortedNodes) {
            System.out.print(node + " -> ");
            System.out.println(node.neighbors);
        }
    }

    /**
     * Retrieves a node at the given coordinates.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The node at the specified coordinates, or null if not found.
     */
    public Node getNode(int x, int y) {
        String key = x + "," + y;
        return nodes.get(key);
    }

    /**
     * Retrieves a traffic node at the given coordinates.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @return The traffic node, or null if not a traffic node.
     */
    public TrafficNode getTrafficNode(int x, int y) {
        Node node = getNode(x, y);
        return (node instanceof TrafficNode) ? (TrafficNode) node : null;
    }

    /**
     * Retrieves all nodes in the network.
     * @return A collection of all nodes.
     */
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    /**
     * Checks if a path exists between two nodes using DFS.
     * @param start The starting node.
     * @param end The destination node.
     * @return True if a path exists, false otherwise.
     */
    public boolean pathExists(Node start, Node end) {
        Set<Node> visited = new HashSet<>();
        return dfs(start, end, visited);
    }

    /**
     * Helper method for depth-first search to find a path.
     */
    private boolean dfs(Node current, Node end, Set<Node> visited) {
        if (current.equals(end)) return true;
        visited.add(current);
        
        for (Node neighbor : current.neighbors) {
            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, end, visited)) return true;
            }
        }
        return false;
    }

    /**
     * Finds all nodes reachable from a given starting node.
     * @param start The starting node.
     * @return A set of reachable nodes.
     */
    public Set<Node> findReachableNodes(Node start) {
        Set<Node> reachable = new HashSet<>();
        dfsReachable(start, reachable);
        return reachable;
    }

    /**
     * Helper method for depth-first search to find reachable nodes.
     */
    private void dfsReachable(Node current, Set<Node> visited) {
        visited.add(current);
        for (Node neighbor : current.neighbors) {
            if (!visited.contains(neighbor)) {
                dfsReachable(neighbor, visited);
            }
        }
    }

    /**
     * Gets the total number of nodes in the network.
     * @return The total node count.
     */
    public int totalNodes() {
        return nodes.size();
    }

    /**
     * Main method for testing the RoadMapParser.
     */
    public static void main(String[] args) {
        RoadMapParser parser = new RoadMapParser();
        try {
            parser.parseCSV("src/main/resources/static/map.csv", "src/main/resources/static/signal.csv");
            parser.printGraph();
            
            Node start = parser.getNode(2, 2);
            Node end = parser.getNode(2, 1);
            
            if (start != null && end != null) {
                System.out.println("Path exists between " + start + " and " + end + ": " + parser.pathExists(start, end));
                System.out.println("Reachable nodes from " + start + ": " + parser.findReachableNodes(start));
            }
            
            System.out.println("Total nodes in the map: " + parser.totalNodes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}