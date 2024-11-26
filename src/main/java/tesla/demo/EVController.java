package tesla.demo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.concurrent.*;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/ev")
public class EVController {
    private Map<String, EV> evMap;
    private PathfindingVisualizer pathfinder;
    private TrafficManager trafficManager;
    private ScheduledExecutorService scheduler;

    public EVController() {
        evMap = new HashMap<>();
        pathfinder = new PathfindingVisualizer(GameMap.getInstance());
        trafficManager = new TrafficManager();
    }
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
        
        // Calculate and store path immediately
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
    public Map<String, EV> getEvMap() {
        return this.evMap;
    }
    @PostMapping("/{evName}/updatePosition")
public ResponseEntity<?> updateEVPosition(@PathVariable String evName) {
    EV ev = evMap.get(evName);
    if (ev != null) {
        ev.currentPathIndex++;
        System.out.println("Updated " + evName + " position to index: " + ev.currentPathIndex);
        return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
}
//     @PostMapping("/{evName}/canMove")
// public ResponseEntity<?> canEVMove(@PathVariable String evName) {
//     try {
//         EV ev = evMap.get(evName);
//         System.out.println("Checking movement for EV: " + evName);
//         System.out.println("Current position: (" + ev.getPath().get(ev.currentPathIndex).getX() + 
//                           "," + ev.getPath().get(ev.currentPathIndex).getY() + ")");
//         System.out.println("Current path index: " + ev.currentPathIndex);

//         boolean canMove = trafficManager.canEVMove(ev);
//         System.out.println("Movement decision for " + evName + ": " + canMove);
//         return ResponseEntity.ok(canMove);
//     } catch (Exception e) {
//         System.err.println("Error in canMove endpoint: " + e.getMessage());
//         e.printStackTrace();
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//     }
// }

// @GetMapping("/traffic/signals")
// public ResponseEntity<List<TrafficSignalState>> getTrafficSignals() {
//     List<TrafficSignalState> states = TrafficManager.trafficLights.stream()
//         .map(node -> new TrafficSignalState(node.x, node.y, node.isGreen(), node.getDeque().size()))
//         .collect(Collectors.toList());
//     return ResponseEntity.ok(states);
// }


    @PostMapping("/{evName}/start")
    public List<PathNode> startEV(@PathVariable String evName) {
        EV ev = evMap.get(evName);
        if (ev == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "EV not found");
        }
        return ev.getPath(); // Return the pre-calculated path
    }
    @GetMapping("/all")
    public ResponseEntity<List<EV>> getAllEVs() {
        List<EV> evList = new ArrayList<>(evMap.values());
        return ResponseEntity.ok(evList);
    }
    @PostMapping("/traffic/change")
    public ResponseEntity<Void> changeTrafficSignals() {
        TrafficManager.changeSignals();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{evName}/canMoveToPosition/{targetX}/{targetY}")
public ResponseEntity<Boolean> canMoveToPosition(
    @PathVariable String evName,
    @PathVariable int targetX, 
    @PathVariable int targetY
) {
    try {
        EV ev = evMap.get(evName);
        if (ev == null) {
            return ResponseEntity.notFound().build();
        }
        
        System.out.println("Checking movement for EV: " + evName + " to position: (" + targetX + "," + targetY + ")");
        boolean canMove = trafficManager.canMoveToPosition(ev, targetX, targetY);
        System.out.println("Movement decision: " + canMove);
        
        return ResponseEntity.ok(canMove);
    } catch (Exception e) {
        System.err.println("Error checking movement: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}



    private List<PathNode> convertToPathNodes(long[] path) {
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 0; i < path.length; i += 2) {
            nodes.add(new PathNode((int)path[i], (int)path[i+1]));
        }
        return nodes;
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


class TrafficSignalState {
    public int x;
    public int y;
    public boolean isGreen;
    public int queueSize;
    
    public TrafficSignalState(int x, int y, boolean isGreen, int queueSize) {
        this.x = x;
        this.y = y;
        this.isGreen = isGreen;
        this.queueSize = queueSize;
    }
}

