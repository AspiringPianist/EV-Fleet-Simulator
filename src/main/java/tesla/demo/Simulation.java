package tesla.demo;

import java.io.File;

public class Simulation {
    static {
        String absolutePath = new File("").getAbsolutePath();
        String dllPath = absolutePath + "\\src\\main\\resources\\lib\\astar_jni.dll";
        System.load(dllPath);    }

    private GameMap map;
    private PathfindingVisualizer pathfindingVisualizer;

    public Simulation() {
        initializeMap();
        pathfindingVisualizer = new PathfindingVisualizer(map);
    }

    private void initializeMap() {
        map = GameMap.getInstance();
        System.out.println("Map loaded with dimensions: " + map.getWidth() + "x" + map.getHeight());
        map.printMap();
    }

    public void displayMap() {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                GameObject obj = map.getObject(x, y);
                if (obj != null) {
                    System.out.print(obj.symbol + " ");
                } else {
                    System.out.print("[_] ");
                }
            }
            System.out.println();
        }
    }

    public void runPathfindingSimulation() {
        pathfindingVisualizer.runPathfindingSimulation();
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation();
        sim.runPathfindingSimulation();
    }
}
