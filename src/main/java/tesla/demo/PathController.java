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
        //we need to do findPath for all the evs listed in the frontend
        return convertToPathNodes(path); //we need to return this list of paths
        //this should run when 
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

class PathNode {
    private int x;
    private int y;

    public PathNode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
}

class PathRequest {
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getEndX() { return endX; }
    public int getEndY() { return endY; }

    public void setStartX(int startX) { this.startX = startX; }
    public void setStartY(int startY) { this.startY = startY; }
    public void setEndX(int endX) { this.endX = endX; }
    public void setEndY(int endY) { this.endY = endY; }
}
class RoadNode {
    private int x;
    private int y;
    private boolean oneWay;

    public RoadNode(int x, int y, boolean oneWay) {
        this.x = x;
        this.y = y;
        this.oneWay = oneWay;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public boolean isOneWay() { return oneWay; }
}

class MapData {
    private List<RoadNode> roads;
    
    public MapData(Map<String, Node> roadNetwork) {
        this.roads = roadNetwork.values().stream()
            .map(node -> new RoadNode(node.x, node.y, !node.neighbors.isEmpty()))
            .collect(Collectors.toList());
    }

    public List<RoadNode> getRoads() {
        return roads;
    }
}
