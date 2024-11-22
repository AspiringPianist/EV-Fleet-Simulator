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
    public EVController() {
        evMap = new HashMap<>();
        pathfinder = new PathfindingVisualizer(GameMap.getInstance());
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
        
        evMap.put(ev.getName(), ev);
        return ResponseEntity.ok(ev);
    }

    @GetMapping("/all")
    public List<EV> getAllEVs() {
        return new ArrayList<>(evMap.values());
    }

    @PostMapping("/{evName}/start")
    public List<PathNode> startEV(@PathVariable String evName) {
        EV ev = evMap.get(evName);
        if (ev == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "EV not found");
        }

        long[] path = pathfinder.findPath(
            ev.getStartX(),
            ev.getStartY(),
            ev.getEndX(),
            ev.getEndY()
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
