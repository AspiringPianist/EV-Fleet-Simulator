package tesla.demo;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PathController {
    private final GameMap gameMap;
    private final PathfindingVisualizer pathfinder;

    public PathController() {
        this.gameMap = GameMap.getInstance();
        this.pathfinder = new PathfindingVisualizer(gameMap);
    }

    @PostMapping("/findPath")
    public List<PathNode> findPath(@RequestBody PathRequest request) {
        long[] path = pathfinder.findPath(
            request.getStartX(),
            request.getStartY(),
            request.getEndX(),
            request.getEndY()
        );
        return convertToPathNodes(path);
    }

    private List<PathNode> convertToPathNodes(long[] path) {
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 0; i < path.length; i += 2) {
            nodes.add(new PathNode((int)path[i], (int)path[i+1]));
        }
        return nodes;
    }

    @GetMapping("/map")
    public MapData getMapData() {
        return new MapData(gameMap.getRoadNetwork());
    }
}

/**
 * Represents a single node in a path, defined by its x and y coordinates.
 * 
 * This class is used to represent discrete locations in a grid-based navigation system,
 * typically used for pathfinding and movement tracking in a simulation environment.
 * 
 * @author Anonymous
 * @version 1.0
 * @since 2024-11-27
 */
class PathNode {
    /** The x-coordinate of the node in the grid system */
    private int x;
    
    /** The y-coordinate of the node in the grid system */
    private int y;

    /**
     * Constructs a new PathNode with specified x and y coordinates.
     * 
     * @param x The x-coordinate of the node
     * @param y The y-coordinate of the node
     */
    public PathNode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retrieves the x-coordinate of the node.
     * 
     * @return The x-coordinate
     */
    public int getX() { return x; }

    /**
     * Retrieves the y-coordinate of the node.
     * 
     * @return The y-coordinate
     */
    public int getY() { return y; }
}

/**
 * Represents a path finding request with start and end coordinates.
 * 
 * Used to encapsulate the parameters needed to define a path between two points
 * in a grid-based navigation system.
 * 
 * @author Anonymous
 * @version 1.0
 * @since 2024-11-27
 */
class PathRequest {
    /** Starting x-coordinate */
    private int startX;
    
    /** Starting y-coordinate */
    private int startY;
    
    /** Ending x-coordinate */
    private int endX;
    
    /** Ending y-coordinate */
    private int endY;

    /**
     * Retrieves the starting x-coordinate.
     * 
     * @return The start x-coordinate
     */
    public int getStartX() { return startX; }

    /**
     * Retrieves the starting y-coordinate.
     * 
     * @return The start y-coordinate
     */
    public int getStartY() { return startY; }

    /**
     * Retrieves the ending x-coordinate.
     * 
     * @return The end x-coordinate
     */
    public int getEndX() { return endX; }

    /**
     * Retrieves the ending y-coordinate.
     * 
     * @return The end y-coordinate
     */
    public int getEndY() { return endY; }

    /**
     * Sets the starting x-coordinate.
     * 
     * @param startX The x-coordinate to set as the start
     */
    public void setStartX(int startX) { this.startX = startX; }

    /**
     * Sets the starting y-coordinate.
     * 
     * @param startY The y-coordinate to set as the start
     */
    public void setStartY(int startY) { this.startY = startY; }

    /**
     * Sets the ending x-coordinate.
     * 
     * @param endX The x-coordinate to set as the end
     */
    public void setEndX(int endX) { this.endX = endX; }

    /**
     * Sets the ending y-coordinate.
     * 
     * @param endY The y-coordinate to set as the end
     */
    public void setEndY(int endY) { this.endY = endY; }
}

/**
 * Represents a node in a road network with positional and directional information.
 * 
 * Contains coordinates and a flag indicating whether the road is one-way or bidirectional.
 * 
 * @author Anonymous
 * @version 1.0
 * @since 2024-11-27
 */
class RoadNode {
    /** The x-coordinate of the road node */
    private int x;
    
    /** The y-coordinate of the road node */
    private int y;
    
    /** Indicates whether the road is one-way */
    private boolean oneWay;

    /**
     * Constructs a new RoadNode with specified coordinates and directionality.
     * 
     * @param x The x-coordinate of the road node
     * @param y The y-coordinate of the road node
     * @param oneWay Indicates if the road is one-way
     */
    public RoadNode(int x, int y, boolean oneWay) {
        this.x = x;
        this.y = y;
        this.oneWay = oneWay;
    }

    /**
     * Retrieves the x-coordinate of the road node.
     * 
     * @return The x-coordinate
     */
    public int getX() { return x; }

    /**
     * Retrieves the y-coordinate of the road node.
     * 
     * @return The y-coordinate
     */
    public int getY() { return y; }

    /**
     * Checks if the road is one-way.
     * 
     * @return true if the road is one-way, false otherwise
     */
    public boolean isOneWay() { return oneWay; }
}

/**
 * Encapsulates road network data for transmission or processing.
 * 
 * Converts the internal road network representation to a list of RoadNodes
 * that can be easily serialized or transferred.
 * 
 * @author Anonymous
 * @version 1.0
 * @since 2024-11-27
 */
class MapData {
    /** List of road nodes in the network */
    private List<RoadNode> roads;
    
    /**
     * Constructs MapData by converting a road network map to a list of RoadNodes.
     * 
     * Transforms each node in the network to a RoadNode, determining one-way status
     * based on the presence of neighboring nodes.
     * 
     * @param roadNetwork The original road network map to be converted
     */
    public MapData(Map<String, Node> roadNetwork) {
        this.roads = roadNetwork.values().stream()
            .map(node -> new RoadNode(node.x, node.y, !node.neighbors.isEmpty()))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves the list of road nodes.
     * 
     * @return List of RoadNodes representing the road network
     */
    public List<RoadNode> getRoads() {
        return roads;
    }
}