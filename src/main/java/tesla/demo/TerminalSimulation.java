
package tesla.demo;
import java.util.ArrayList;
import java.util.List;
public class TerminalSimulation {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";
    
    private GameMap gameMap;
    private TrafficManager trafficManager;
    private EVController evController;
    private volatile boolean running = true;

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

        evController.getEvMap().put("EV1", ev1);
        evController.getEvMap().put("EV2", ev2);
    }

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

    private void startEV(String evName) {
        EV ev = evController.getEvMap().get(evName);
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

    private List<PathNode> convertToPathNodes(long[] path) {
        List<PathNode> nodes = new ArrayList<>();
        for (int i = 0; i < path.length; i += 2) {
            nodes.add(new PathNode((int)path[i], (int)path[i+1]));
        }
        return nodes;
    }

    private void simulateEVMovement(String evName) {
        EV ev = evController.getEvMap().get(evName);
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
                for (EV ev : evController.getEvMap().values()) {
                    if (ev.getCurrentX() == i && ev.getCurrentY() == j) {
                        symbol = ANSI_BLUE + "[" + ev.getName().charAt(2) + "]" + ANSI_RESET;
                        break;
                    }
                }
                
                // If no EV, check for traffic nodes
                if (symbol.equals("   ")) {
                    TrafficNode trafficNode = gameMap.getTrafficNode(i, j);
                    if (trafficNode != null) {
                        //String queueInfo = trafficNode.getDeque().isEmpty() ? "" : 
                           // String.format("%d", trafficNode.getDeque().size());
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
        System.out.println("EV 1 position:" + evController.getEvMap().get("EV1").getCurrentX() + "," + evController.getEvMap().get("EV1").getCurrentY() + " EV 2 position:" + evController.getEvMap().get("EV2").getCurrentX() + "," + evController.getEvMap().get("EV2").getCurrentY());
        //System.out.println("EV 1 stalled status:" + gameMap.getRoadNode(evController.getEvMap().get("EV1").getCurrentX(),evController.getEvMap().get("EV1").getCurrentY()).isStalled());

        //System.out.flush();
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void main(String[] args) {
        new TerminalSimulation().start();
    }
}
