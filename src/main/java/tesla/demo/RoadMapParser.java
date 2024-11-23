package tesla.demo;
import java.io.*;
import java.util.*;

class Node {
    int x, y;

    List<Node> neighbors;
    public String type;
    //TODO
    // is stop location
    // is it red
    public Node(int x, int y,String type) {
        this.x = x;
        this.y = y;
        this.type=type;
        this.neighbors = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

public class RoadMapParser {
    private Map<String, Node> nodes; // Key: "x,y", Value: Node
    private int rows;
    private int cols;

    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }

    public RoadMapParser() {
        this.nodes = new HashMap<>();
    }

    public void parseCSV(String filePath, String signalMapPath) throws IOException {
        List<String[]> grid = new ArrayList<>();
        List<String[]> signalGrid = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by comma, but not commas within quotes
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                // Remove quotes and clean the data
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "").trim();
                }
                
                grid.add(row);
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(signalMapPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by comma, but not commas within quotes
                String[] row = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                // Remove quotes and clean the data
                for (int i = 0; i < row.length; i++) {
                    row[i] = row[i].replace("\"", "").trim();
                }
                
                signalGrid.add(row);
            }
        }
        
        this.rows = grid.size();
        this.cols = grid.get(0).length;

        // Process each cell in the grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                String cellValue = grid.get(i)[j];
                String signalCellValue = signalGrid.get(i)[j];
                if (!cellValue.equals("0") && !cellValue.equals("497")) {
                    // Create node for current position (i+1, j+1 for 1-based indexing)
                    Node currentNode = null;
                    if(signalCellValue.equals("1") || signalCellValue.equals("2")) {
                        currentNode = getOrCreateNode(i + 1, j + 1, "TrafficNode", Integer.parseInt(signalCellValue)-1);
                    }
                    else {
                        currentNode = getOrCreateNode(i + 1, j + 1, "Node", Integer.parseInt(signalCellValue)-1);
                    }
                    // Parse destinations
                    List<int[]> coordinates = parseCoordinates(cellValue);
                    
                    // Create edges for each destination
                    for (int[] coord : coordinates) {
                        Node destNode = getOrCreateNode(coord[0], coord[1], "Node", Integer.parseInt(signalCellValue)-1);
                        if (!currentNode.neighbors.contains(destNode)) {
                            currentNode.neighbors.add(destNode);
                        }
                    }
                }
            }
        }
    }

    private List<int[]> parseCoordinates(String cellValue) {
        List<int[]> coordinates = new ArrayList<>();
        
        // Split by parentheses if they exist, or treat as single coordinate
        String[] parts = cellValue.split("\\),\\(|\\(|\\)");
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            
            // Split the coordinate pair
            String[] coords = part.split(",");
            if (coords.length >= 2) {
                try {
                    int x = Integer.parseInt(coords[0].trim());
                    int y = Integer.parseInt(coords[1].trim());
                    coordinates.add(new int[]{x, y});
                } catch (NumberFormatException e) {
                    // Skip invalid number formats
                    continue;
                }
            }
        }
        
        return coordinates;
    }
    

    private Node getOrCreateNode(int x, int y,String type, int trafficType) {
        String key = x + "," + y;
        if(type.equals("TrafficNode")){
            TrafficNode newNode = new TrafficNode(x, y,"TrafficNode", trafficType);
            TrafficManager.getInstance().addTrafficNode(newNode);
            return nodes.computeIfAbsent(key, k -> newNode);
        }
        return nodes.computeIfAbsent(key, k -> new Node(x, y,type));
    }
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

    // Method to get a specific node
    public Node getNode(int x, int y) {
        return nodes.get(x + "," + y);
    }

    // Method to get all nodes
    public Collection<Node> getAllNodes() {
        return nodes.values();
    }

    // Method to verify if a path exists between two nodes
    public boolean pathExists(Node start, Node end) {
        Set<Node> visited = new HashSet<>();
        return dfs(start, end, visited);
    }

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

    // Method to find all reachable nodes from a starting node
    public Set<Node> findReachableNodes(Node start) {
        Set<Node> reachable = new HashSet<>();
        dfsReachable(start, reachable);
        return reachable;
    }

    public int totalNodes() {
        return nodes.size();
    }

    private void dfsReachable(Node current, Set<Node> visited) {
        visited.add(current);
        for (Node neighbor : current.neighbors) {
            if (!visited.contains(neighbor)) {
                dfsReachable(neighbor, visited);
            }
        }
    }

    public static void main(String[] args) {
        RoadMapParser parser = new RoadMapParser();
        try {
            parser.parseCSV("src/main/resources/static/map.csv", "src/main/resources/static/signal.csv");
            parser.printGraph();
            
            // Example of finding path between two nodes
            Node start = parser.getNode(2, 2);
            Node end = parser.getNode(34, 34);
            if (start != null && end != null) {
                boolean pathExists = parser.pathExists(start, end);
                System.out.println("\nPath exists from (2,2) to (34,34): " + pathExists);
                
                // Print reachable nodes from start
                Set<Node> reachable = parser.findReachableNodes(start);
                System.out.println("\nNumber of reachable nodes from (2,2): " + reachable.size());
            }
            // Print total number of nodes
            System.out.println("\nTotal number of nodes: " + parser.totalNodes());
            //check if all nodes are reachable from any node
            boolean allNodesReachable = true;
            for (Node node : parser.getAllNodes()) {
                Set<Node> reachableNodes = parser.findReachableNodes(node);
                    if(reachableNodes.size()!=parser.totalNodes()) {
                        allNodesReachable=false;
                    }
                }
            if(allNodesReachable)
            System.out.println("All nodes are reachable from any node: " + allNodesReachable);
            else{
                System.out.println("All nodes are not reachable from any node: " + allNodesReachable);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }
}