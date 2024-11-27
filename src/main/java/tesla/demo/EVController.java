package tesla.demo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for managing Electric Vehicles (EVs).
 * Provides endpoints for creating, updating, and monitoring EVs.
 */
@RestController
@RequestMapping("/api/ev")
public class EVController {
    public static Map<String, EV> evMap;
    private PathfindingVisualizer pathfinder;
    private TrafficManager trafficManager;

    /**
     * Constructor for EVController.
     * Initializes the EV map, pathfinding visualizer, and traffic manager.
     */
    public EVController() {
        evMap = new HashMap<>();
        pathfinder = new PathfindingVisualizer(GameMap.getInstance());
        trafficManager = new TrafficManager();
    }
    

    /**
     * Creates a new EV and assigns it a path.
     *
     * @param request The request payload containing EV details.
     * @return ResponseEntity with the created EV object.
     */
    @PostMapping("/new")
    public ResponseEntity<EV> newEV(@RequestBody EVCreateRequest request) {
        EV ev = new EV(
            request.getStartX(),
            request.getStartY(),
            request.getType(),
            request.getCharge(),
            request.getChargingRate()
        );
        ev.setEndLocation(request.getEndX(), request.getEndY());
        ev.setName(request.getName());

        // Calculate and set the path for the EV.
        long[] pathArray = pathfinder.findPath(
            ev.getStartX(),
            ev.getStartY(),
            ev.getEndX(),
            ev.getEndY()
        );
        List<PathNode> path = convertToPathNodes(pathArray);
        ev.setPath(path);

        evMap.put(ev.getName(), ev);
        return ResponseEntity.ok(ev);
    }

    /**
     * Checks if an EV can move to a specified position.
     *
     * @param evName The name of the EV.
     * @param x      Target x-coordinate.
     * @param y      Target y-coordinate.
     * @return ResponseEntity with a boolean indicating if the EV can move.
     */
    @PostMapping("/{evName}/canMoveToPosition/{x}/{y}")
    public ResponseEntity<Boolean> canMoveToPosition(
            @PathVariable String evName,
            @PathVariable int x,
            @PathVariable int y) {
        EV ev = evMap.get(evName);
        if (ev != null) {
            if (ev.getCurrentPathIndex() + 1 < ev.getPath().size()) {
                PathNode nextNode = ev.getPath().get(ev.getCurrentPathIndex() + 1);
                if (nextNode.getX() == x && nextNode.getY() == y) {
                    boolean canMove = trafficManager.canMoveToPosition(ev, x, y);
                    return ResponseEntity.ok(canMove);
                }
            }
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves a list of all EVs.
     *
     * @return ResponseEntity containing a list of all EVs.
     */
    @GetMapping("/all")
    public ResponseEntity<List<EV>> getAllEVs() {
        return ResponseEntity.ok(new ArrayList<>(evMap.values()));
    }

    /**
     * Starts an EV's movement along its path.
     *
     * @param evName The name of the EV.
     * @return ResponseEntity containing the EV's path.
     */
    @PostMapping("/{evName}/start")
    public ResponseEntity<List<PathNode>> startEV(@PathVariable String evName) {
        EV ev = evMap.get(evName);
        if (ev != null) {
            ev.setMoving(true);
            return ResponseEntity.ok(ev.getPath());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes an EV.
     *
     * @param evName The name of the EV to delete.
     * @return ResponseEntity indicating the result of the operation.
     */
    @DeleteMapping("/{evName}")
    public ResponseEntity<Void> deleteEV(@PathVariable String evName) {
        if (evMap.containsKey(evName)) {
            evMap.remove(evName);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Updates an EV's position to the next point on its path.
     *
     * @param evName The name of the EV.
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/{evName}/updatePosition")
    public ResponseEntity<Void> updateEVPosition(@PathVariable String evName) {
        EV ev = evMap.get(evName);
        if (ev != null && ev.isMoving() && ev.getCurrentPathIndex() < ev.getPath().size() - 1) {
            ev.currentPathIndex++;
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves the status of an EV.
     *
     * @param evName The name of the EV.
     * @return ResponseEntity containing the EV's status.
     */
    @GetMapping("/{evName}/status")
    public ResponseEntity<EVStatus> getEVStatus(@PathVariable String evName) {
        EV ev = evMap.get(evName);
        if (ev != null) {
            return ResponseEntity.ok(new EVStatus(
                ev.getCharge(),
                ev.getCurrentX(),
                ev.getCurrentY()
            ));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Converts a raw path array to a list of PathNode objects.
     *
     * @param path The raw path array.
     * @return List of PathNode objects.
     */
    private List<PathNode> convertToPathNodes(long[] path) {
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 0; i < path.length; i += 2) {
            nodes.add(new PathNode((int) path[i], (int) path[i + 1]));
        }
        return nodes;
    }

    /**
     * Retrieves the traffic signal states.
     *
     * @return ResponseEntity containing a list of traffic signal states.
     */
    @GetMapping("/traffic/signals")
    public ResponseEntity<List<TrafficSignalState>> getTrafficSignals() {
        return ResponseEntity.ok(
            TrafficManager.trafficLights.stream()
                .map(node -> new TrafficSignalState(node.x, node.y, node.isGreen()))
                .collect(Collectors.toList())
        );
    }

    /**
     * Changes the state of traffic signals.
     *
     * @return ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/traffic/change")
    public ResponseEntity<Void> changeTrafficSignals() {
        TrafficManager.changeSignals();
        return ResponseEntity.ok().build();
    }
}

class EVCreateRequest {
    private String name;
    private int startX;
    private int startY;
    private int endX;
    private int endY;
    private int type;
    private int charge;
    private int chargingRate;

    // Getters
    public String getName() { return name; }
    public int getStartX() { return startX; }
    public int getStartY() { return startY; }
    public int getEndX() { return endX; }
    public int getEndY() { return endY; }
    public int getType() { return type; }
    public int getCharge() { return charge; }
    public int getChargingRate() { return chargingRate; }
}

class EVStatus {
    private int charge;
    private int currentX;
    private int currentY;

    public EVStatus(int charge, int currentX, int currentY) {
        this.charge = charge;
        this.currentX = currentX;
        this.currentY = currentY;
    }

    // Getters and setters
}


class TrafficSignalState {
    public int x;
    public int y;
    public boolean isGreen;
    //public int queueSize;
    
    public TrafficSignalState(int x, int y, boolean isGreen) {
        this.x = x;
        this.y = y;
        this.isGreen = isGreen;
        //this.queueSize = queueSize;
    }
}
