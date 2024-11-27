package tesla.demo;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public Map<String, EV> evMap;
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
    
    // Calculate path and print raw coordinates
    long[] pathArray = pathfinder.findPath(
        ev.getStartX(),
        ev.getStartY(),
        ev.getEndX(),
        ev.getEndY()
    );
    
    // Print raw path array
    StringBuilder rawPath = new StringBuilder("Raw path array: ");
    for (int i = 0; i < pathArray.length; i += 2) {
        rawPath.append("(").append(pathArray[i]).append(",").append(pathArray[i+1]).append(") ");
    }
    System.out.println(rawPath.toString());
    
    // Convert and set path
    List<PathNode> path = convertToPathNodes(pathArray);
    
    // Print converted path
    StringBuilder convertedPath = new StringBuilder("Converted path: ");
    for (PathNode node : path) {
        convertedPath.append("(").append(node.getX()).append(",").append(node.getY()).append(") ");
    }
    System.out.println(convertedPath.toString());
    
    ev.setPath(path);
    evMap.put(ev.getName(), ev);
    return ResponseEntity.ok(ev);
}
    @PostMapping("/{evName}/canMoveToPosition/{x}/{y}")
    public ResponseEntity<Boolean> canMoveToPosition(
            @PathVariable String evName,
            @PathVariable int x,
            @PathVariable int y) {
        EV ev = evMap.get(evName);
        if (ev != null) {
            // Print path coordinates in a readable format
            StringBuilder pathStr = new StringBuilder("Path coordinates: ");
            for (PathNode node : ev.getPath()) {
                pathStr.append("(").append(node.getX()).append(",").append(node.getY()).append(") ");
            }
            System.out.println(pathStr.toString());
            System.out.println("Current index: " + ev.getCurrentPathIndex());
            System.out.println("Attempting to move to (" + x + "," + y + ")");
    
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
    @GetMapping("/all")
    public ResponseEntity<List<EV>> getAllEVs() {
        List<EV> evList = new ArrayList<>(evMap.values());
        return ResponseEntity.ok(evList);
    }
    

    @PostMapping("/{evName}/start")
public ResponseEntity<List<PathNode>> startEV(@PathVariable String evName) {
    EV ev = evMap.get(evName);
    if (ev != null) {
        ev.setMoving(true);
        return ResponseEntity.ok(ev.getPath());
    }
    return ResponseEntity.notFound().build();
}
//     @PostMapping("/new")
//     public ResponseEntity<EV> newEV(@RequestBody EVCreateRequest request) {
//         EV ev = new EV(
//             request.getStartX(), 
//             request.getStartY(), 
//             request.getType(),
//             request.getCharge(),
//             request.getChargingRate()
//         );
//         ev.setEndLocation(request.getEndX(), request.getEndY());
//         ev.setName(request.getName());
        
//         // Calculate and store path immediately
//         long[] pathArray = pathfinder.findPath(
//             ev.getStartX(),
//             ev.getStartY(),
//             ev.getEndX(),
//             ev.getEndY()
//         );
//         List<PathNode> path = convertToPathNodes(pathArray);
//         ev.setPath(path);
        
//         evMap.put(ev.getName(), ev);
//         return ResponseEntity.ok(ev);
//     }
    public Map<String, EV> getEvMap() {
        return this.evMap;
    }

    @DeleteMapping("/{evName}")
public ResponseEntity<Void> deleteEV(@PathVariable String evName) {
    if (evMap.containsKey(evName)) {
        evMap.remove(evName);
        return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
}

@PostMapping("/{evName}/stop")
public ResponseEntity<Void> stopEV(@PathVariable String evName) {
    EV ev = evMap.get(evName);
    if (ev != null) {
        // Add any stop logic here
        return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
}

@GetMapping("/{evName}/status")
public ResponseEntity<EVStatus> getEVStatus(@PathVariable String evName) {
    EV ev = evMap.get(evName);
    if (ev != null) {
        EVStatus status = new EVStatus(
            ev.getCharge(),
            ev.getCurrentX(),
            ev.getCurrentY()
        );
        return ResponseEntity.ok(status);
    }
    return ResponseEntity.notFound().build();
}


private List<PathNode> convertToPathNodes(long[] path) {
    List<PathNode> nodes = new ArrayList<>();
    GameMap gameMap = GameMap.getInstance();
    int maxY = gameMap.getHeight() - 1; // Should be 34 for 35x35 map
    int maxX = gameMap.getWidth() - 1;  // Should be 34 for 35x35 map
    
    System.out.println("Map dimensions: " + maxX + "x" + maxY);
    
    for (int i = 0; i < path.length; i += 2) {
        int originalX = (int)path[i];
        int originalY = (int)path[i+1];
        
        // Ensure coordinates are within bounds
        originalX = Math.min(Math.max(originalX, 0), maxX);
        originalY = Math.min(Math.max(originalY, 0), maxY);
        
        // Mirror coordinates within map bounds
        int mirroredX = maxX - originalX;
        int mirroredY = maxY - originalY;
        
        // Final bounds check
        mirroredX = Math.min(Math.max(mirroredX, 0), maxX);
        mirroredY = Math.min(Math.max(mirroredY, 0), maxY);
        
        nodes.add(new PathNode(mirroredX, mirroredY));
    }
    
    // Validate final path
    for (PathNode node : nodes) {
        if (node.getX() < 0 || node.getX() > maxX || 
            node.getY() < 0 || node.getY() > maxY) {
            System.out.println("Warning: Invalid coordinates detected: " + 
                             "(" + node.getX() + "," + node.getY() + ")");
        }
    }
    
    return nodes;
}
@PostMapping("/{evName}/updatePosition")
public ResponseEntity<Void> updateEVPosition(@PathVariable String evName) {
    EV ev = evMap.get(evName);
    if (ev != null && ev.isMoving() && ev.getCurrentPathIndex() < ev.getPath().size() - 1) {
        ev.currentPathIndex++;
        PathNode newPos = ev.getPath().get(ev.getCurrentPathIndex());
        System.out.println("Updated " + evName + " position to index: " + 
                          ev.getCurrentPathIndex() + " at (" + newPos.getX() + 
                          "," + newPos.getY() + ")");
        return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
}
@GetMapping("/traffic/signals")
public ResponseEntity<List<TrafficSignalState>> getTrafficSignals() {
    List<TrafficSignalState> states = TrafficManager.trafficLights.stream()
        .map(node -> new TrafficSignalState(node.x, node.y, node.isGreen()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(states);
}
    @PostMapping("/traffic/change")
    public ResponseEntity<Void> changeTrafficSignals() {
        TrafficManager.changeSignals();
        return ResponseEntity.ok().build();
    }
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



//     @PostMapping("/{evName}/start")
//     public List<PathNode> startEV(@PathVariable String evName) {
//         EV ev = evMap.get(evName);
//         if (ev == null) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "EV not found");
//         }
//         return ev.getPath(); // Return the pre-calculated path
//     }
//     @GetMapping("/all")
//     public ResponseEntity<List<EV>> getAllEVs() {
//         List<EV> evList = new ArrayList<>(evMap.values());
//         return ResponseEntity.ok(evList);
//     }

//     @PostMapping("/{evName}/canMoveToPosition/{targetX}/{targetY}")
// public ResponseEntity<Boolean> canMoveToPosition(
//     @PathVariable String evName,
//     @PathVariable int targetX, 
//     @PathVariable int targetY
// ) {
//     try {
//         EV ev = evMap.get(evName);
//         if (ev == null) {
//             return ResponseEntity.notFound().build();
//         }
        
//         System.out.println("Checking movement for EV: " + evName + " to position: (" + targetX + "," + targetY + ")");
//         boolean canMove = trafficManager.canMoveToPosition(ev, targetX, targetY);
//         System.out.println("Movement decision: " + canMove);
        
//         return ResponseEntity.ok(canMove);
//     } catch (Exception e) {
//         System.err.println("Error checking movement: " + e.getMessage());
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//     }
// }


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

