package tesla.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MapController {

    private final GameMap gameMap;

    public MapController() {
        // Use the existing Map instance
        this.gameMap = GameMap.getInstance(); // You'll need to add this static method to Map.java
    }

    // @GetMapping("/api/map")
    // public boolean[][] getMap() {
    //     // rendering only obstacles
    //     return gameMap.getObstacleMap();
    // }
}
