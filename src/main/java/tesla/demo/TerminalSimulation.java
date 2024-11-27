package tesla.demo;
import java.util.ArrayList;
import java.util.List;

/**
 * TerminalSimulation is a console-based simulation of electric vehicle (EV) movement 
 * and traffic management in a grid-based game map.
 * 
 * This class provides a terminal visualization of EV navigation, traffic signals, 
 * and movement constraints within a simulated environment.
 */
public class TerminalSimulation {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";
    
    private GameMap gameMap;
    private TrafficManager trafficManager;
    private EVController evController;
    private volatile boolean running = true;

    /**
     * Constructor for TerminalSimulation.
     * 
     * Initializes the game map, traffic manager, and EV controller.
     * Creates two test electric vehicles (EV1 and EV2) with predefined start and end locations.
     */
    public TerminalSimulation() {
        gameMap = GameMap.getInstance();
        trafficManager = TrafficManager.getInstance();
        evController = new EVController();
        
        // Add test EVs
        EV ev1 = new EV(4, 35, 1, 100, 10);
        ev1.setName("EV1");
        ev1.setEndLocation(35, 2);

        EV ev2 = new EV(2, 35, 2, 100, 10);
        ev2.setName("EV2");
        ev2.setEndLocation(35, 2);

        EVController.evMap.put("EV1", ev1);
        EVController.evMap.put("EV2", ev2);
    }

    /**
     * Starts the simulation by initiating movement for both EVs 
     * and continuously updating the map visualization.
     * 
     * The simulation runs in a loop, printing the map every 500 milliseconds
     * until manually stopped or interrupted.
     */
    public void start() {
        // Start both EVs immediately
        startEV("EV1");
        startEV("EV2");
    
        // Continue map updates
        while (running) {
            printMap();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Starts movement for a specific electric vehicle.
     * 
     * Performs the following steps:
     * 1. Retrieves the EV from the EV controller
     * 2. Uses PathfindingVisualizer to find the optimal path
     * 3. Converts path coordinates to PathNodes
     * 4. Sets the path for the EV
     * 5. Initiates movement simulation
     * 
     * @param evName The name of the electric vehicle to start (e.g., "EV1")
     */
    private void startEV(String evName) {
        EV ev = EVController.evMap.get(evName);
        PathfindingVisualizer pathfinder = new PathfindingVisualizer(GameMap.getInstance());
        
        long[] pathArray = pathfinder.findPath(
            ev.getStartX(),
            ev.getStartY(),
            ev.getEndX(),
            ev.getEndY()
        );
        
        List<PathNode> path = convertToPathNodes(pathArray);
        ev.setPath(path);
        ev.setMoving(true);
        simulateEVMovement(evName);
    }

    /**
     * Converts a raw path array of coordinates to a list of PathNodes.
     * 
     * Performs coordinate validation to ensure they are within the game map bounds.
     * Prints out each converted coordinate for debugging purposes.
     * 
     * @param path Array of long coordinates representing the path
     * @return List of PathNodes representing the validated path
     */
    private List<PathNode> convertToPathNodes(long[] path) {
        List<PathNode> nodes = new ArrayList<>();
        int maxDim = GameMap.getInstance().getWidth(); // Assuming square map
        
        for (int i = 0; i < path.length; i += 2) {
            // Convert coordinates to match game map bounds (1 to maxDim)
            int x = Math.min(Math.max((int)path[i], 1), maxDim);
            int y = Math.min(Math.max((int)path[i+1], 1), maxDim);
            
            System.out.println("Converting path coordinate: (" + x + "," + y + ")");
            nodes.add(new PathNode(x, y));
        }
        return nodes;
    }

    /**
     * Simulates the movement of a specific electric vehicle along its predetermined path.
     * 
     * Runs in a separate thread to allow concurrent movement.
     * Checks with TrafficManager for permission to move to the next position.
     * Moves the EV one step at a time with a 500ms delay between steps.
     * 
     * @param evName The name of the electric vehicle to simulate movement for
     */
    private void simulateEVMovement(String evName) {
        EV ev = EVController.evMap.get(evName);
        new Thread(() -> {
            while (ev.isMoving() && ev.currentPathIndex < ev.getPath().size() - 1) {
                PathNode nextPos = ev.getPath().get(ev.currentPathIndex + 1);
                if (trafficManager.canMoveToPosition(ev, nextPos.getX(), nextPos.getY())) {
                    ev.currentPathIndex++;
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
            ev.setMoving(false);
        }).start();
    }

    /**
     * Prints the current state of the game map to the console.
     * 
     * Renders the map with:
     * - Column and row numbers
     * - Electric vehicles in blue
     * - Traffic nodes in green (go) or red (stop)
     * - Empty walkable spaces
     * 
     * Also displays current positions of EVs at the bottom of the map.
     */
    private void printMap() {
        clearScreen();
        System.out.println("=== Traffic Map ===\n");
        
        // Column numbers
        System.out.print("   ");
        for (int j = 0; j < gameMap.getWidth(); j++) {
            System.out.printf("%2d ", j);
        }
        System.out.println();

        // Map with row numbers
        for (int i = 1; i <= gameMap.getHeight(); i++) {
            System.out.printf("%2d ", i);
            for (int j = 1; j <= gameMap.getWidth(); j++) {
                String symbol = "   ";
                
                // Check for EVs first
                for (EV ev : EVController.evMap.values()) {
                    if (ev.getCurrentX() == i && ev.getCurrentY() == j) {
                        symbol = ANSI_BLUE + "[" + ev.getName().charAt(2) + "]" + ANSI_RESET;
                        break;
                    }
                }
                
                // If no EV, check for traffic nodes
                if (symbol.equals("   ")) {
                    TrafficNode trafficNode = gameMap.getTrafficNode(i, j);
                    if (trafficNode != null) {
                        symbol = (trafficNode.isGreen() ? 
                            ANSI_GREEN + "[T" + "]" : 
                            ANSI_RED + "[T" + "]") + ANSI_RESET;
                    } else if (gameMap.isWalkable(i, j)) {
                        symbol = "[ ]";
                    }
                }
                
                System.out.print(symbol);
            }
            System.out.println();
        }
        System.out.println("EV 1 position:" + EVController.evMap.get("EV1").getCurrentX() + "," + EVController.evMap.get("EV1").getCurrentY() + " EV 2 position:" + EVController.evMap.get("EV2").getCurrentX() + "," + EVController.evMap.get("EV2").getCurrentY());
    }

    /**
     * Clears the console screen for a clean map rendering.
     * 
     * Uses ANSI escape codes to reset the cursor and clear the screen.
     */
    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Main method to launch the terminal simulation.
     * 
     * Creates a new TerminalSimulation instance and starts the simulation.
     * 
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        new TerminalSimulation().start();
    }
}